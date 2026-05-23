package com.alma.improved_original.quest;

import com.alma.improved_original.Config;
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

import java.util.*;
import java.util.stream.Collectors;

public class QuestManager {
    private static long lastRefreshBucket = -1;

    private static final List<PoolEntry> QUEST_POOL = buildQuestPool();

    private record PoolEntry(QuestType type, ResourceLocation targetId, int weight) {}

    private static List<PoolEntry> buildQuestPool() {
        List<PoolEntry> pool = new ArrayList<>();

        // BREAK_BLOCK
        pool.add(new PoolEntry(QuestType.BREAK_BLOCK, ResourceLocation.parse("minecraft:stone"), 20));
        pool.add(new PoolEntry(QuestType.BREAK_BLOCK, ResourceLocation.parse("minecraft:oak_log"), 15));
        pool.add(new PoolEntry(QuestType.BREAK_BLOCK, ResourceLocation.parse("minecraft:coal_ore"), 10));
        pool.add(new PoolEntry(QuestType.BREAK_BLOCK, ResourceLocation.parse("minecraft:iron_ore"), 8));
        pool.add(new PoolEntry(QuestType.BREAK_BLOCK, ResourceLocation.parse("minecraft:dirt"), 20));
        pool.add(new PoolEntry(QuestType.BREAK_BLOCK, ResourceLocation.parse("minecraft:deepslate"), 18));
        pool.add(new PoolEntry(QuestType.BREAK_BLOCK, ResourceLocation.parse("minecraft:sand"), 15));

        // KILL_ENTITY
        pool.add(new PoolEntry(QuestType.KILL_ENTITY, ResourceLocation.parse("minecraft:zombie"), 20));
        pool.add(new PoolEntry(QuestType.KILL_ENTITY, ResourceLocation.parse("minecraft:skeleton"), 20));
        pool.add(new PoolEntry(QuestType.KILL_ENTITY, ResourceLocation.parse("minecraft:spider"), 15));
        pool.add(new PoolEntry(QuestType.KILL_ENTITY, ResourceLocation.parse("minecraft:creeper"), 15));
        pool.add(new PoolEntry(QuestType.KILL_ENTITY, ResourceLocation.parse("minecraft:enderman"), 8));

        // CRAFT_ITEM
        pool.add(new PoolEntry(QuestType.CRAFT_ITEM, ResourceLocation.parse("minecraft:crafting_table"), 10));
        pool.add(new PoolEntry(QuestType.CRAFT_ITEM, ResourceLocation.parse("minecraft:furnace"), 10));
        pool.add(new PoolEntry(QuestType.CRAFT_ITEM, ResourceLocation.parse("minecraft:iron_pickaxe"), 12));
        pool.add(new PoolEntry(QuestType.CRAFT_ITEM, ResourceLocation.parse("minecraft:iron_sword"), 10));
        pool.add(new PoolEntry(QuestType.CRAFT_ITEM, ResourceLocation.parse("minecraft:torch"), 15));
        pool.add(new PoolEntry(QuestType.CRAFT_ITEM, ResourceLocation.parse("minecraft:bread"), 12));
        pool.add(new PoolEntry(QuestType.CRAFT_ITEM, ResourceLocation.parse("minecraft:stick"), 8));

        // COLLECT_ITEM
        pool.add(new PoolEntry(QuestType.COLLECT_ITEM, ResourceLocation.parse("minecraft:coal"), 15));
        pool.add(new PoolEntry(QuestType.COLLECT_ITEM, ResourceLocation.parse("minecraft:iron_ingot"), 10));
        pool.add(new PoolEntry(QuestType.COLLECT_ITEM, ResourceLocation.parse("minecraft:wheat"), 12));
        pool.add(new PoolEntry(QuestType.COLLECT_ITEM, ResourceLocation.parse("minecraft:apple"), 8));
        pool.add(new PoolEntry(QuestType.COLLECT_ITEM, ResourceLocation.parse("minecraft:rotten_flesh"), 10));
        pool.add(new PoolEntry(QuestType.COLLECT_ITEM, ResourceLocation.parse("minecraft:bone"), 10));

        return pool;
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
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
            refreshPlayerQuests(player, data);
        }
    }

    public static void refreshPlayerQuests(ServerPlayer player, QuestData data) {
        // Ensure player has quests (first login case)
        if (!data.hasAnyQuest()) {
            for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
                if (!data.isSlotLocked(i)) {
                    data.setQuest(i, generateRandomQuest(player.getRandom(), data));
                }
            }
        } else {
            for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
                if (!data.isSlotLocked(i)) {
                    data.setQuest(i, generateRandomQuest(player.getRandom(), data));
                }
            }
        }

        data.setLastRefreshTick(player.serverLevel().getGameTime());
        player.setData(ModAttachments.QUEST_DATA.get(), data);
        syncToPlayer(player, data);
    }

    public static void ensureQuestsInitialized(ServerPlayer player, QuestData data) {
        if (!data.hasAnyQuest()) {
            for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
                data.setQuest(i, generateRandomQuest(player.getRandom(), data));
            }
            player.setData(ModAttachments.QUEST_DATA.get(), data);
            syncToPlayer(player, data);
        }
    }

    public static QuestDefinition generateRandomQuest(RandomSource random, QuestData existingData) {
        Set<ResourceLocation> existingTargets = new HashSet<>();
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            existingData.getQuest(i).ifPresent(q -> existingTargets.add(q.targetId()));
        }

        List<PoolEntry> available = QUEST_POOL.stream()
                .filter(e -> !existingTargets.contains(e.targetId()))
                .toList();

        if (available.isEmpty()) {
            available = QUEST_POOL;
        }

        int totalWeight = available.stream().mapToInt(PoolEntry::weight).sum();
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        PoolEntry chosen = available.get(0);
        for (PoolEntry entry : available) {
            cumulative += entry.weight();
            if (roll < cumulative) {
                chosen = entry;
                break;
            }
        }

        int minCount = Config.QUEST_TARGET_COUNT_MIN.getAsInt();
        int maxCount = Config.QUEST_TARGET_COUNT_MAX.getAsInt();
        int targetCount = minCount + random.nextInt(maxCount - minCount + 1);

        int minReward = Config.QUEST_REWARD_MIN.getAsInt();
        int maxReward = Config.QUEST_REWARD_MAX.getAsInt();
        int reward = minReward + random.nextInt(maxReward - minReward + 1);

        return new QuestDefinition(chosen.type(), chosen.targetId(), targetCount, reward);
    }

    // Progress updates
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

    private static void updateProgress(ServerPlayer player, QuestType type, ResourceLocation targetId) {
        updateProgress(player, type, targetId, 1);
    }

    private static void updateProgress(ServerPlayer player, QuestType type, ResourceLocation targetId, int amount) {
        QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
        boolean changed = false;

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            var questOpt = data.getQuest(i);
            if (questOpt.isEmpty()) continue;
            QuestDefinition quest = questOpt.get();
            if (quest.type() == type && quest.targetId().equals(targetId)) {
                int newProgress = Math.min(data.getProgress(i) + amount, quest.targetCount());
                data.setProgress(i, newProgress);
                changed = true;

                if (newProgress >= quest.targetCount()) {
                    completeQuest(player, data, i);
                }
            }
        }

        if (changed) {
            player.setData(ModAttachments.QUEST_DATA.get(), data);
            syncToPlayer(player, data);
        }
    }

    // Lock / unlock
    public static void handleLockPacket(Player player, int slot) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        QuestData data = serverPlayer.getData(ModAttachments.QUEST_DATA.get());
        if (slot < 0 || slot >= QuestData.SLOT_COUNT) return;

        if (data.isSlotLocked(slot)) {
            // Unlock
            unlockSlot(serverPlayer, data, slot);
        } else {
            // Lock
            lockSlot(serverPlayer, data, slot);
        }
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
        syncToPlayer(player, data);
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
        syncToPlayer(player, data);
        player.sendSystemMessage(Component.translatable("quest.improved_original.unlock.success", slot + 1));
        return true;
    }

    private static void completeQuest(ServerPlayer player, QuestData data, int slot) {
        var questOpt = data.getQuest(slot);
        if (questOpt.isEmpty()) return;
        QuestDefinition quest = questOpt.get();

        // Give reward
        ItemStack reward = quest.createReward();
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false);
        }

        data.clearSlot(slot);

        player.sendSystemMessage(Component.translatable("quest.improved_original.complete_success",
                quest.getTargetDisplayName(), quest.rewardEmeralds()));
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

    public static void syncToPlayer(ServerPlayer player, QuestData data) {
        PacketDistributor.sendToPlayer(player, new S2CQuestSyncPayload(data));
    }
}
