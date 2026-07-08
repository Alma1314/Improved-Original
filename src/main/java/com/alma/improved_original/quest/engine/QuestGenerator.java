// 任务生成器：从 JSON 配置池加载任务，加权随机选择，去重，数量范围解析
// 链任务初始生成只选 COMMON（无前置条件的链第一步），每日任务按配置权重全池随机
package com.alma.improved_original.quest.engine;

import com.alma.improved_original.Config;
import com.alma.improved_original.datagen.QuestPoolConfig;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.component.Rarity;
import com.alma.improved_original.quest.task.ChainQuest;
import com.alma.improved_original.quest.task.DailyQuest;
import com.alma.improved_original.quest.task.IQuestTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.fml.loading.FMLPaths;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class QuestGenerator {

    private static List<QuestPoolConfig.PoolEntry> questPool = null;
    private static Map<String, Integer> chainStepCounts = Map.of();

    public static List<QuestPoolConfig.PoolEntry> getPool() {
        if (questPool == null) {
            questPool = QuestPoolConfig.loadFromConfig(FMLPaths.CONFIGDIR.get());
            chainStepCounts = computeChainStepCounts(questPool);
        }
        return questPool;
    }

    public static void reloadPool() {
        questPool = QuestPoolConfig.loadFromConfig(FMLPaths.CONFIGDIR.get());
        chainStepCounts = computeChainStepCounts(questPool);
    }

    // 为每日刷新和首次初始化生成随机的每日任务
    public static IQuestTask generateDailyQuest(RandomSource random, QuestData existingData) {
        List<QuestPoolConfig.PoolEntry> pool = getPool();
        if (pool.isEmpty()) return null;

        // 过滤：排除有条件的（链任务不应该出现在每日刷新中）
        List<QuestPoolConfig.PoolEntry> dailyPool = pool.stream()
                .filter(e -> e.conditions().isEmpty() && e.unlocks().isEmpty())
                .toList();

        if (dailyPool.isEmpty()) {
            dailyPool = pool; // fallback: use all entries
        }

        return selectWeighted(dailyPool, random, existingData);
    }

    // 为链任务生成特定 ID 的任务
    public static IQuestTask generateChainStep(String questId, RandomSource random) {
        List<QuestPoolConfig.PoolEntry> pool = getPool();
        if (pool.isEmpty()) return null;

        QuestPoolConfig.PoolEntry entry = pool.stream()
                .filter(e -> e.id().equals(questId))
                .findFirst()
                .orElse(null);

        if (entry == null) return null;

        return createChainQuest(entry, random);
    }

    // 加权随机选择并实例化
    private static IQuestTask selectWeighted(List<QuestPoolConfig.PoolEntry> pool, RandomSource random,
                                              QuestData existingData) {
        Set<ResourceLocation> existingTargets = existingData.getExistingTargetItems();
        List<QuestPoolConfig.PoolEntry> available = pool.stream()
                .filter(e -> e.targets().stream().noneMatch(t -> existingTargets.contains(t.item())))
                .toList();

        if (available.isEmpty()) {
            available = pool;
        }

        // 加权选择
        double totalWeight = available.stream()
                .mapToDouble(e -> e.weight() * Config.getRarityWeight(e.rarity()))
                .sum();
        if (totalWeight <= 0) return null;

        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;
        QuestPoolConfig.PoolEntry chosen = available.getFirst();
        for (QuestPoolConfig.PoolEntry entry : available) {
            cumulative += entry.weight() * Config.getRarityWeight(entry.rarity());
            if (roll < cumulative) {
                chosen = entry;
                break;
            }
        }

        return createDailyQuest(chosen, random);
    }

    private static DailyQuest createDailyQuest(QuestPoolConfig.PoolEntry entry, RandomSource random) {
        var resolvedTargets = entry.targets().stream().map(t ->
                new com.alma.improved_original.quest.component.TargetComponent(t.type(), t.item(),
                        t.countMin() + random.nextInt(t.countMax() - t.countMin() + 1))
        ).toList();

        return new DailyQuest(
                entry.id().isEmpty() ? "daily_" + UUID.randomUUID() : entry.id(),
                resolvedTargets,
                entry.rewards().stream()
                        .map(r -> new com.alma.improved_original.quest.component.RewardComponent(
                                r.item(), r.countMin(), r.countMax()))
                        .toList(),
                entry.rarity(),
                entry.name(),
                entry.description()
        );
    }

    private static ChainQuest createChainQuest(QuestPoolConfig.PoolEntry entry, RandomSource random) {
        var resolvedTargets = entry.targets().stream().map(t ->
                new com.alma.improved_original.quest.component.TargetComponent(t.type(), t.item(),
                        t.countMin() + random.nextInt(t.countMax() - t.countMin() + 1))
        ).toList();

        return new ChainQuest(
                entry.id(),
                resolvedTargets,
                entry.rewards().stream()
                        .map(r -> new com.alma.improved_original.quest.component.RewardComponent(
                                r.item(), r.countMin(), r.countMax()))
                        .toList(),
                entry.conditions(),
                entry.unlocks(),
                entry.rarity(),
                entry.name(),
                entry.description()
        );
    }

    private static Map<String, Integer> computeChainStepCounts(List<QuestPoolConfig.PoolEntry> pool) {
        Map<String, Integer> counts = new java.util.HashMap<>();
        for (var entry : pool) {
            if (entry.unlocks().isEmpty() && entry.conditions().isEmpty()) continue;
            // Determine root: if entry has no conditions, it IS a root
            String rootId;
            if (entry.conditions().isEmpty()) {
                rootId = entry.id();
            } else {
                rootId = entry.conditions().getFirst().requiredQuestId();
            }
            counts.merge(rootId, 1, Integer::sum);
        }
        return counts;
    }

    public static int getChainStepCount(String rootId) {
        return chainStepCounts.getOrDefault(rootId, 0);
    }
}
