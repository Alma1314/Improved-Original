// 游戏事件监听：方块破坏、实体击杀、物品合成、物品拾取、结构探索，触发任务进度更新
// 所有事件独立处理，仅对生存模式的在线玩家生效
// 结构检测采用每20tick（约1秒）轮询，避免每tick昂贵的结构查找
package com.alma.improved_original.quest.event;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.quest.ModAttachments;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.world.level.GameType;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = ImprovedOriginal.MOD_ID)
public class QuestEventHandlers {

    private static int structureCheckCounter = 0;

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            if (player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) return;
            QuestManager.onBlockBroken(player, event.getState().getBlock());
        }
    }

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            QuestManager.onEntityKilled(player, event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestManager.onItemCrafted(player, event.getCrafting(), event.getCrafting().getCount());
        }
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            QuestManager.onItemCollected(player, event.getItemEntity().getItem(),
                    event.getItemEntity().getItem().getCount());
        }
    }

    // Check player position for structure quests every 20 ticks (~1 second)
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        structureCheckCounter++;
        if (structureCheckCounter < 20) return;
        structureCheckCounter = 0;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
            if (!data.isActive()) continue;

            var structuresAt = player.serverLevel().structureManager()
                    .getAllStructuresAt(player.blockPosition());
            if (structuresAt.isEmpty()) {
                continue;
            }

            Set<ResourceLocation> foundIds = new HashSet<>();
            for (var entry : structuresAt.entrySet()) {
                var registry = player.serverLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
                var key = registry.getResourceKey(entry.getKey());
                key.ifPresent(k -> foundIds.add(k.location()));
            }

            for (ResourceLocation id : foundIds) {
                QuestManager.onStructureEntered(player, id);
            }
        }
    }
}
