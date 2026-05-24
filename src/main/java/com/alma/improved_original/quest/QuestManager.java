// 任务核心逻辑：JSON池加载、生成、刷新、进度追踪、锁定/解锁、完成奖励、手动刷新、结构检测、网络同步
package com.alma.improved_original.quest;

import com.alma.improved_original.Config;
import com.alma.improved_original.datagen.QuestPoolConfig;
import com.alma.improved_original.quest.network.S2CQuestSyncPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import net.minecraft.util.RandomSource;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class QuestManager {
    private static long lastRefreshBucket = -1;
    private static List<QuestPoolConfig.PoolEntry> questPool = null;

    private static List<QuestPoolConfig.PoolEntry> getQuestPool() {
        if (questPool == null) {
            questPool = QuestPoolConfig.loadFromConfig(FMLPaths.CONFIGDIR.get());
        }
        return questPool;
    }

    public static void reloadQuestPool() {
        questPool = QuestPoolConfig.loadFromConfig(FMLPaths.CONFIGDIR.get());
    }

    public static void onServerTick(MinecraftServer server) {
        long intervalTicks = 20L * 60 * Config.QUEST_REFRESH_INTERVAL_MINUTES.getAsInt();
        long gameTime = server.overworld().getGameTime();
        long currentBucket = gameTime / intervalTicks;

        if (lastRefreshBucket < 0) {
            lastRefreshBucket = currentBucket;
            return;
        }

        if (currentBucket > lastRefreshBucket) {
            lastRefreshBucket = currentBucket;
            refreshAllPlayersQuests(server);
        }
    }

    private static void refreshAllPlayersQuests(MinecraftServer server) {
        Component refreshMsg = Component.translatable("quest.improved_original.refresh_notify");
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
            refreshPlayerQuests(player, data);
            player.sendSystemMessage(refreshMsg);
        }
    }

    public static void refreshPlayerQuests(ServerPlayer player, QuestData data) {
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            if (!data.isSlotLocked(i)) {
                data.setQuest(i, generateRandomQuest(player.getRandom(), data));
            }
        }
        data.setLastRefreshTick(player.serverLevel().getGameTime());
        player.setData(ModAttachments.QUEST_DATA.get(), data);
        syncToPlayerSilent(player, data);
    }

    public static void ensureQuestsInitialized(ServerPlayer player, QuestData data) {
        boolean changed = false;
        if (!data.hasAnyQuest()) {
            for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
                data.setQuest(i, generateRandomQuest(player.getRandom(), data));
            }
            changed = true;
        }
        if (!data.isActive()) {
            data.setActive(true);
            changed = true;
        }
        if (changed) {
            player.setData(ModAttachments.QUEST_DATA.get(), data);
            syncToPlayerSilent(player, data);
        }
    }

    public static QuestDefinition generateRandomQuest(RandomSource random, QuestData existingData) {
        List<QuestPoolConfig.PoolEntry> pool = getQuestPool();
        if (pool.isEmpty()) return null;

        Set<ResourceLocation> existingTargets = new HashSet<>();
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            existingData.getQuest(i).ifPresent(q ->
                q.targets().forEach(t -> existingTargets.add(t.item())));
        }

        List<QuestPoolConfig.PoolEntry> available = pool.stream()
                .filter(e -> e.targets().stream().noneMatch(t -> existingTargets.contains(t.item())))
                .toList();

        if (available.isEmpty()) {
            available = pool;
        }

        int totalWeight = available.stream().mapToInt(QuestPoolConfig.PoolEntry::weight).sum();
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        QuestPoolConfig.PoolEntry chosen = available.get(0);
        for (QuestPoolConfig.PoolEntry entry : available) {
            cumulative += entry.weight();
            if (roll < cumulative) {
                chosen = entry;
                break;
            }
        }

        // 将目标数量范围解析为具体数值
        List<QuestDefinition.ItemCount> resolvedTargets = chosen.targets().stream().map(t ->
            new QuestDefinition.ItemCount(t.item(),
                t.countMin() + random.nextInt(t.countMax() - t.countMin() + 1))
        ).toList();

        // 将奖励数量范围解析为具体数值
        List<QuestDefinition.ItemCount> resolvedRewards = chosen.rewards().stream().map(r ->
            new QuestDefinition.ItemCount(r.item(),
                r.countMin() + random.nextInt(r.countMax() - r.countMin() + 1))
        ).toList();

        return new QuestDefinition(chosen.type(), resolvedTargets, resolvedRewards,
                chosen.name(), chosen.description());
    }

    // ---- 进度更新入口 ----
    public static void onBlockBroken(ServerPlayer player, Block block) {
        ResourceLocation blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
        updateProgress(player, QuestType.BREAK_BLOCK, blockId);
    }

    public static void onItemCrafted(ServerPlayer player, ItemStack result, int amount) {
        ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(result.getItem());
        updateProgress(player, QuestType.CRAFT_ITEM, itemId, amount);
    }

    public static void onEntityKilled(ServerPlayer player, net.minecraft.world.entity.Entity entity) {
        ResourceLocation entityId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        updateProgress(player, QuestType.KILL_ENTITY, entityId);
    }

    public static void onItemCollected(ServerPlayer player, ItemStack stack, int amount) {
        ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        updateProgress(player, QuestType.COLLECT_ITEM, itemId, amount);
    }

    public static void onStructureEntered(ServerPlayer player, ResourceLocation structureId) {
        updateProgress(player, QuestType.FIND_STRUCTURE, structureId);
    }

    private static void updateProgress(ServerPlayer player, QuestType type, ResourceLocation targetId) {
        updateProgress(player, type, targetId, 1);
    }

    private static void updateProgress(ServerPlayer player, QuestType type, ResourceLocation targetId, int amount) {
        QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
        if (!data.isActive()) return;
        boolean changed = false;

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            var questOpt = data.getQuest(i);
            if (questOpt.isEmpty()) continue;
            QuestDefinition quest = questOpt.get();
            if (quest.type() != type) continue;

            // 查找匹配的目标索引
            List<Integer> progress = new ArrayList<>(data.getSlot(i).perTargetProgress());
            for (int t = 0; t < quest.targets().size(); t++) {
                QuestDefinition.ItemCount target = quest.targets().get(t);
                if (target.item().equals(targetId)) {
                    int current = t < progress.size() ? progress.get(t) : 0;
                    int newProg = Math.min(current + amount, target.count());
                    progress.set(t, newProg);
                    data.setProgress(i, progress);
                    changed = true;
                }
            }

            if (data.getSlot(i).isComplete()) {
                completeQuest(player, data, i);
            }
        }

        if (changed) {
            player.setData(ModAttachments.QUEST_DATA.get(), data);
            syncToPlayerSilent(player, data);
        }
    }

    // 通过按键打开任务面板
    public static void handleOpenScreenPacket(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        QuestData data = serverPlayer.getData(ModAttachments.QUEST_DATA.get());
        ensureQuestsInitialized(serverPlayer, data);
        syncToPlayer(serverPlayer, data);
    }

    // 锁定 / 解锁任务槽位
    public static void handleLockPacket(Player player, int slot) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        QuestData data = serverPlayer.getData(ModAttachments.QUEST_DATA.get());
        if (slot < 0 || slot >= QuestData.SLOT_COUNT) return;
        if (data.isSlotLocked(slot)) {
            unlockSlot(serverPlayer, data, slot);
        } else {
            lockSlot(serverPlayer, data, slot);
        }
    }

    // 手动刷新任务
    public static void handleRefreshPacket(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        int refreshCost = Config.EMERALD_REFRESH_COST.getAsInt();
        if (!consumeEmeralds(serverPlayer, refreshCost)) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("quest.improved_original.refresh.no_emeralds", refreshCost));
            return;
        }
        manualRefreshPlayer(serverPlayer);
        serverPlayer.sendSystemMessage(
                Component.translatable("quest.improved_original.refresh.success", refreshCost));
    }

    public static void manualRefreshPlayer(ServerPlayer player) {
        QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            if (!data.isSlotLocked(i)) {
                data.setQuest(i, generateRandomQuest(player.getRandom(), data));
            }
        }
        data.setLastRefreshTick(player.serverLevel().getGameTime());
        player.setData(ModAttachments.QUEST_DATA.get(), data);
        syncToPlayerSilent(player, data);
    }

    public static boolean lockSlot(ServerPlayer player, QuestData data, int slot) {
        if (data.isSlotLocked(slot)) {
            player.sendSystemMessage(Component.translatable("quest.improved_original.lock.already_locked"));
            return false;
        }
        if (data.getQuest(slot).isEmpty()) {
            player.sendSystemMessage(Component.translatable("quest.improved_original.lock.no_quest"));
            return false;
        }
        if (data.getSlot(slot).isComplete()) {
            player.sendSystemMessage(Component.translatable("quest.improved_original.lock.completed"));
            return false;
        }
        int lockCost = Config.EMERALD_LOCK_COST.getAsInt();
        if (!consumeEmeralds(player, lockCost)) {
            player.sendSystemMessage(Component.translatable("quest.improved_original.lock.no_emeralds", lockCost));
            return false;
        }
        data.setSlotLocked(slot, true);
        player.setData(ModAttachments.QUEST_DATA.get(), data);
        syncToPlayerSilent(player, data);
        player.sendSystemMessage(Component.translatable("quest.improved_original.lock.success", slot + 1));
        return true;
    }

    public static boolean unlockSlot(ServerPlayer player, QuestData data, int slot) {
        if (!data.isSlotLocked(slot)) {
            player.sendSystemMessage(Component.translatable("quest.improved_original.unlock.not_locked"));
            return false;
        }
        data.setSlotLocked(slot, false);
        player.setData(ModAttachments.QUEST_DATA.get(), data);
        syncToPlayerSilent(player, data);
        player.sendSystemMessage(Component.translatable("quest.improved_original.unlock.success", slot + 1));
        return true;
    }

    private static void completeQuest(ServerPlayer player, QuestData data, int slot) {
        var questOpt = data.getQuest(slot);
        if (questOpt.isEmpty()) return;
        QuestDefinition quest = questOpt.get();

        List<ItemStack> rewards = quest.createRewards();
        for (ItemStack reward : rewards) {
            if (!player.getInventory().add(reward)) {
                player.drop(reward, false);
            }
        }

        String targetNames = quest.getTargetDisplayNames().stream()
                .map(Component::getString).collect(Collectors.joining(", "));
        String rewardNames = quest.getRewardDisplayNames().stream()
                .map(Component::getString).collect(Collectors.joining(", "));
        data.clearSlot(slot);

        syncToPlayerWithCompletion(player, data, targetNames, rewardNames);
    }

    private static boolean consumeEmeralds(ServerPlayer player, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.EMERALD)) {
                int toRemove = Math.min(stack.getCount(), remaining);
                stack.shrink(toRemove);
                remaining -= toRemove;
                if (remaining <= 0) return true;
            }
        }
        return false;
    }

    public static void syncToPlayerSilent(ServerPlayer player, QuestData data) {
        PacketDistributor.sendToPlayer(player, S2CQuestSyncPayload.syncOnly(data));
    }

    public static void syncToPlayer(ServerPlayer player, QuestData data) {
        PacketDistributor.sendToPlayer(player, S2CQuestSyncPayload.openScreen(data));
    }

    public static void syncToPlayerWithCompletion(ServerPlayer player, QuestData data, String questDescription, String rewardText) {
        PacketDistributor.sendToPlayer(player,
                S2CQuestSyncPayload.withCompletion(data,
                        new S2CQuestSyncPayload.QuestCompletion(questDescription, rewardText)));
    }
}
