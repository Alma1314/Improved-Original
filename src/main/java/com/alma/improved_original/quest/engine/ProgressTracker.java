// 统一进度追踪管道：所有游戏事件（挖块/合成/击杀/拾取/结构）通过 handle() 进入
// 匹配 TargetComponent（类型+物品），推送进度，检测完成，委托 QuestEngine 发放奖励
package com.alma.improved_original.quest.engine;

import com.alma.improved_original.quest.ModAttachments;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestType;
import com.alma.improved_original.quest.task.IQuestTask;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ProgressTracker {

    public static void handle(ServerPlayer player, QuestType type, ResourceLocation targetId, int amount) {
        QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
        if (!data.isActive()) return;
        boolean changed = false;

        for (int i = 0; i < data.getSlots().size(); i++) {
            var questOpt = data.getQuest(i);
            if (questOpt.isEmpty()) continue;
            IQuestTask quest = questOpt.get();

            IntArrayList progress = data.getSlot(i).perTargetProgress();
            IntArrayList modified = null;
            for (int t = 0; t < quest.targets().size(); t++) {
                var target = quest.targets().get(t);
                if (target.type() != type) continue;
                if (!target.item().equals(targetId)) continue;

                int current = t < progress.size() ? progress.getInt(t) : 0;
                int newProg = Math.min(current + amount, target.count());
                if (newProg == current) continue;

                if (modified == null) {
                    modified = progress.clone();
                }
                modified.set(t, newProg);
                changed = true;
            }

            if (modified != null) {
                data.setProgress(i, modified);
                if (data.isSlotComplete(i)) {
                    QuestEngine.get().completeQuest(player, data, i);
                }
            }
        }

        if (changed) {
            QuestEngine.get().markPlayerDirty(player);
        }
    }
}
