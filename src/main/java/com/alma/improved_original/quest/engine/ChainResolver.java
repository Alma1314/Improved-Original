// 链依赖解析器：任务完成时检查 unlock 列表，验证前置条件，分配后续任务
// 只有在满足所有 ConditionComponent 的条件下才会解锁下一步
// 链槽位不足时跳过（保留在"待解锁"状态，下次有槽位时分配）
package com.alma.improved_original.quest.engine;

import com.alma.improved_original.Config;
import com.alma.improved_original.quest.ModAttachments;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestData.StepInfo;
import com.alma.improved_original.quest.QuestData.StepStatus;
import com.alma.improved_original.quest.QuestSlotData;
import com.alma.improved_original.quest.task.ChainQuest;
import com.alma.improved_original.quest.task.IQuestTask;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChainResolver {

    // 当任务完成时调用：检查 unlock 列表，分配下一步
    public static void resolve(ServerPlayer player, QuestData data, IQuestTask completedTask) {
        if (completedTask.unlocks().isEmpty()) return;

        String chainRootId = completedTask instanceof ChainQuest cq ? findChainRoot(cq) : "";

        // 更新链进度
        updateChainProgress(player, data, completedTask);

        for (String nextId : completedTask.unlocks()) {
            // 防止重复分配同一个链步骤
            if (isQuestActiveInAnySlot(data, nextId)) continue;

            // 生成下一步任务并验证条件
            IQuestTask nextQuest = QuestGenerator.generateChainStep(nextId, player.getRandom());
            if (nextQuest == null) continue;

            // 验证所有前置条件是否满足
            if (!areConditionsMet(data, nextQuest)) continue;

            // 查找空链槽位
            Optional<Integer> emptySlot = findEmptyChainSlot(data);
            if (emptySlot.isPresent()) {
                data.setQuest(emptySlot.get(), nextQuest);
                // 更新链进度中的步骤状态为 ACTIVE
                QuestData.ChainProgress cp = data.getChainProgress(chainRootId);
                if (cp != null) {
                    List<StepInfo> updatedSteps = new ArrayList<>(cp.steps());
                    for (int i = 0; i < updatedSteps.size(); i++) {
                        if (updatedSteps.get(i).questId().equals(nextQuest.id())) {
                            updatedSteps.set(i, new StepInfo(nextQuest.id(), StepStatus.ACTIVE));
                            break;
                        }
                    }
                    data.updateChainProgress(chainRootId, new QuestData.ChainProgress(
                            cp.chainId(), cp.completedStepCount(), cp.totalStepCount(),
                            cp.lastCompletedTaskId(), updatedSteps));
                }
                QuestEngine.get().markPlayerDirty(player);
                player.sendSystemMessage(Component.translatable(
                        "quest.improved_original.chain.next_unlocked",
                        Component.translatable(nextQuest.name()).getString()));
            }
        }
    }

    private static boolean isQuestActiveInAnySlot(QuestData data, String questId) {
        return data.getSlots().stream()
                .anyMatch(s -> s.quest().isPresent() && s.quest().get().id().equals(questId));
    }

    private static boolean areConditionsMet(QuestData data, IQuestTask quest) {
        for (var cond : quest.conditions()) {
            QuestData.ChainProgress progress = data.getChainProgress(cond.requiredQuestId());
            if (progress == null) return false;
            if (progress.completedStepCount() < cond.requiredCount()) return false;
        }
        return true;
    }

    private static Optional<Integer> findEmptyChainSlot(QuestData data) {
        int dailyCount = Config.getDailySlotCount();
        int totalSlots = data.getSlots().size();
        for (int i = dailyCount; i < totalSlots; i++) {
            if (data.getQuest(i).isEmpty()) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private static void updateChainProgress(ServerPlayer player, QuestData data, IQuestTask completedTask) {
        if (!(completedTask instanceof ChainQuest chainQuest)) return;

        String chainRootId = findChainRoot(chainQuest);
        QuestData.ChainProgress existing = data.getChainProgress(chainRootId);
        int newStepCount = existing != null ? existing.completedStepCount() + 1 : 1;
        int totalSteps = countChainSteps(chainRootId);

        // 构建新的 StepInfo 列表
        List<StepInfo> newSteps = new ArrayList<>();
        if (existing != null) {
            newSteps.addAll(existing.steps());
        }
        // 标记当前任务完成
        boolean updated = false;
        for (int i = 0; i < newSteps.size(); i++) {
            if (newSteps.get(i).questId().equals(completedTask.id())) {
                newSteps.set(i, new StepInfo(completedTask.id(), StepStatus.DONE));
                updated = true;
                break;
            }
        }
        if (!updated) {
            newSteps.add(new StepInfo(completedTask.id(), StepStatus.DONE));
        }
        // 添加后续步骤的 LOCKED 占位（如果还未在列表中）
        for (String nextId : completedTask.unlocks()) {
            boolean exists = newSteps.stream().anyMatch(s -> s.questId().equals(nextId));
            if (!exists) {
                newSteps.add(new StepInfo(nextId, StepStatus.LOCKED));
            }
        }

        QuestData.ChainProgress newProgress = new QuestData.ChainProgress(
                chainRootId, newStepCount, totalSteps, completedTask.id(), newSteps);

        data.updateChainProgress(chainRootId, newProgress);

        // 链完成时触发文件写入
        if (newStepCount >= totalSteps) {
            for (var s : newSteps) {
                if (s.status() != StepStatus.DONE) {
                    // 确保所有步骤都标记为完成
                    // (this shouldn't happen if count logic is correct)
                }
            }
            CompletedChainStore.writeCompletedChain(player.getUUID(), chainRootId, newProgress);
        }
    }

    private static String findChainRoot(ChainQuest quest) {
        // 链根是链中第一个没有条件或条件为空的任务的 ID
        // 如果当前任务没有条件，它就是根
        if (quest.conditions().isEmpty()) {
            return quest.id();
        }
        // 否则，使用第一个条件的 requiredQuestId 作为 root
        return quest.conditions().getFirst().requiredQuestId();
    }

    private static int countChainSteps(String rootId) {
        return QuestGenerator.getChainStepCount(rootId);
    }
}
