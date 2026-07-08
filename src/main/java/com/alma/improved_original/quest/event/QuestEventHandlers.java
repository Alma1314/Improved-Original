// 游戏事件监听：方块破坏、实体击杀、物品合成、物品拾取 → QuestEngine
package com.alma.improved_original.quest.event;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.quest.engine.QuestEngine;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.world.level.GameType;

@EventBusSubscriber(modid = ImprovedOriginal.MOD_ID)
public class QuestEventHandlers {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            if (player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) return;
            QuestEngine.get().onBlockBroken(player, event.getState().getBlock());
        }
    }

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            QuestEngine.get().onEntityKilled(player, event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestEngine.get().onItemCrafted(player, event.getCrafting(), event.getCrafting().getCount());
        }
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            QuestEngine.get().onItemCollected(player, event.getItemEntity().getItem(),
                    event.getItemEntity().getItem().getCount());
        }
    }
}
