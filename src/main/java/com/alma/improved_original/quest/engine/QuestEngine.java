// 任务引擎单例：替代原 QuestManager，委托给 ProgressTracker / QuestGenerator / ChainResolver / DailyRefreshScheduler
// 对外暴露与原 QuestManager 兼容的 API，内部通过四个协作者实现职责分离
// get() 获取单例，在 ImprovedOriginal 构造器中初始化
package com.alma.improved_original.quest.engine;

import com.alma.improved_original.Config;
import com.alma.improved_original.quest.ModAttachments;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestSlotData;
import com.alma.improved_original.quest.QuestType;
import com.alma.improved_original.quest.network.QuestSyncPayload;
import com.alma.improved_original.quest.task.IQuestTask;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestEngine {

    private static QuestEngine INSTANCE;

    private final java.util.Set<ServerPlayer> dirtyPlayers =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    public static QuestEngine get() {
        if (INSTANCE == null) {
            INSTANCE = new QuestEngine();
        }
        return INSTANCE;
    }

    public void markPlayerDirty(ServerPlayer player) {
        dirtyPlayers.add(player);
    }

    java.util.Set<ServerPlayer> getDirtyPlayers() {
        return java.util.Set.copyOf(dirtyPlayers);
    }

    void clearDirtyPlayers() {
        dirtyPlayers.clear();
    }

    // ── Engine lifecycle ──

    public void onServerTick(MinecraftServer server) {
        DailyRefreshScheduler.onServerTick(server);
    }

    public void reloadQuestPool() {
        QuestGenerator.reloadPool();
    }

    // ── Event entry points (delegate to ProgressTracker) ──

    public void onBlockBroken(ServerPlayer player, Block block) {
        ResourceLocation blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
        ProgressTracker.handle(player, QuestType.BREAK_BLOCK, blockId, 1);
    }

    public void onItemCrafted(ServerPlayer player, ItemStack result, int amount) {
        ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(result.getItem());
        ProgressTracker.handle(player, QuestType.CRAFT_ITEM, itemId, amount);
    }

    public void onEntityKilled(ServerPlayer player, net.minecraft.world.entity.Entity entity) {
        ResourceLocation entityId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        ProgressTracker.handle(player, QuestType.KILL_ENTITY, entityId, 1);
    }

    public void onItemCollected(ServerPlayer player, ItemStack stack, int amount) {
        ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        ProgressTracker.handle(player, QuestType.COLLECT_ITEM, itemId, amount);
    }

    public void onStructureEntered(ServerPlayer player, ResourceLocation structureId) {
        ProgressTracker.handle(player, QuestType.FIND_STRUCTURE, structureId, 1);
    }

    // ── Quest completion ──

    public void completeQuest(ServerPlayer player, QuestData data, int slot) {
        var questOpt = data.getQuest(slot);
        if (questOpt.isEmpty()) return;
        IQuestTask quest = questOpt.get();

        // 发放奖励
        for (ItemStack reward : quest.createRewards(player.getRandom())) {
            if (!player.getInventory().add(reward)) {
                player.drop(reward, false);
            }
        }

        // 构建完成通知字符串
        String targetNames = quest.getTargetDisplayNames().stream()
                .map(Component::getString).collect(Collectors.joining(", "));
        String rewardNames = quest.getRewardDisplayNames().stream()
                .map(Component::getString).collect(Collectors.joining(", "));

        // 先处理链解锁（需要在清空槽位前获取 quest 引用）
        ChainResolver.resolve(player, data, quest);

        data.clearSlot(slot);
        markPlayerDirty(player);
        syncToPlayerWithCompletion(player, data, targetNames, rewardNames);
    }

    // ── Packet handlers ──

    public void handleOpenScreenPacket(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        QuestData data = serverPlayer.getData(ModAttachments.QUEST_DATA.get());
        ensureQuestsInitialized(serverPlayer, data);
        syncToPlayer(serverPlayer, data);
    }

    public void handleLockPacket(Player player, int slot) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        QuestData data = serverPlayer.getData(ModAttachments.QUEST_DATA.get());
        if (slot < 0 || slot >= data.getSlots().size()) return;
        if (data.isSlotLocked(slot)) {
            unlockSlot(serverPlayer, data, slot);
        } else {
            lockSlot(serverPlayer, data, slot);
        }
    }

    public void handleRefreshPacket(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.isCreative()) {
            manualRefreshPlayer(serverPlayer);
            return;
        }
        int refreshCost = Config.REFRESH_COST.getAsInt();
        if (!consumeCurrency(serverPlayer, refreshCost)) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("quest.improved_original.refresh.no_emeralds", refreshCost));
            return;
        }
        manualRefreshPlayer(serverPlayer);
        serverPlayer.sendSystemMessage(
                Component.translatable("quest.improved_original.refresh.success", refreshCost));
    }

    public void handleChainPanelPacket(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        QuestData data = serverPlayer.getData(ModAttachments.QUEST_DATA.get());
        ensureQuestsInitialized(serverPlayer, data);

        // 加载已完成链历史
        List<QuestData.ChainProgress> completedChains =
                CompletedChainStore.loadCompletedChains(serverPlayer.getUUID());

        // Sync data + completed chains to client
        PacketDistributor.sendToPlayer(serverPlayer,
                QuestSyncPayload.chainPanel(data, completedChains));
    }

    // ── Slot management ──

    public boolean lockSlot(ServerPlayer player, QuestData data, int slot) {
        if (data.isSlotLocked(slot)) {
            player.sendSystemMessage(Component.translatable("quest.improved_original.lock.already_locked"));
            return false;
        }
        if (data.getQuest(slot).isEmpty()) {
            player.sendSystemMessage(Component.translatable("quest.improved_original.lock.no_quest"));
            return false;
        }
        if (data.isSlotComplete(slot)) {
            player.sendSystemMessage(Component.translatable("quest.improved_original.lock.completed"));
            return false;
        }
        int lockCost = Config.LOCK_COST.getAsInt();
        if (!player.isCreative() && !consumeCurrency(player, lockCost)) {
            player.sendSystemMessage(Component.translatable("quest.improved_original.lock.no_emeralds", lockCost));
            return false;
        }
        data.setSlotLocked(slot, true);
        markPlayerDirty(player);
        saveAndSync(player, data);
        player.sendSystemMessage(Component.translatable("quest.improved_original.lock.success", slot + 1));
        return true;
    }

    public boolean unlockSlot(ServerPlayer player, QuestData data, int slot) {
        if (!data.isSlotLocked(slot)) {
            player.sendSystemMessage(Component.translatable("quest.improved_original.unlock.not_locked"));
            return false;
        }
        data.setSlotLocked(slot, false);
        markPlayerDirty(player);
        saveAndSync(player, data);
        player.sendSystemMessage(Component.translatable("quest.improved_original.unlock.success", slot + 1));
        return true;
    }

    // ── Refresh ──

    public void manualRefreshPlayer(ServerPlayer player) {
        QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
        DailyRefreshScheduler.refreshPlayerSlots(player, data);
    }

    public void ensureQuestsInitialized(ServerPlayer player, QuestData data) {
        boolean changed = false;
        if (!data.hasAnyQuest()) {
            data.refreshUnlockedSlots(QuestGenerator::generateDailyQuest, player.getRandom());
            changed = true;
        }
        if (!data.isActive()) {
            data.setActive(true);
            changed = true;
        }
        if (changed) {
            markPlayerDirty(player);
            saveAndSync(player, data);
        }
    }

    // ── Sync ──

    public void syncToPlayerSilent(ServerPlayer player, QuestData data) {
        PacketDistributor.sendToPlayer(player, QuestSyncPayload.syncOnly(data));
    }

    public void syncToPlayer(ServerPlayer player, QuestData data) {
        PacketDistributor.sendToPlayer(player, QuestSyncPayload.openScreen(data));
    }

    public void syncToPlayerWithCompletion(ServerPlayer player, QuestData data,
                                                    String questDescription, String rewardText) {
        PacketDistributor.sendToPlayer(player,
                QuestSyncPayload.withCompletion(data,
                        new QuestSyncPayload.QuestCompletion(questDescription, rewardText)));
    }

    private void saveAndSync(ServerPlayer player, QuestData data) {
        player.setData(ModAttachments.QUEST_DATA.get(), data);
        syncToPlayerSilent(player, data);
    }

    // ── Currency ──

    private boolean consumeCurrency(ServerPlayer player, int amount) {
        ResourceLocation currencyId = Config.getCurrencyItem();
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : player.getInventory().items) {
            if (isCurrencyItem(stack, currencyId)) {
                stacks.add(stack);
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (isCurrencyItem(offhand, currencyId)) {
            stacks.add(offhand);
        }

        int totalCount = stacks.stream().mapToInt(ItemStack::getCount).sum();
        if (totalCount < amount) return false;

        int remaining = amount;
        for (ItemStack stack : stacks) {
            int toRemove = Math.min(stack.getCount(), remaining);
            stack.shrink(toRemove);
            remaining -= toRemove;
            if (remaining <= 0) return true;
        }
        return false;
    }

    private boolean isCurrencyItem(ItemStack stack, ResourceLocation currencyId) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(currencyId);
    }
}
