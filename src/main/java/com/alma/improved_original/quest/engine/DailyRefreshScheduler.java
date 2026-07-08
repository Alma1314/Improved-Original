// 每日刷新调度器：基于游戏时间的刷新桶，批量刷新所有在线玩家的未锁定槽位
// 从 QuestManager.onServerTick() 提取，实例化后支持更清晰的测试
package com.alma.improved_original.quest.engine;

import com.alma.improved_original.Config;
import com.alma.improved_original.quest.ModAttachments;
import com.alma.improved_original.quest.QuestData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class DailyRefreshScheduler {

    private static long lastRefreshBucket = -1;

    public static void onServerTick(MinecraftServer server) {
        long intervalTicks = 20L * 60 * Config.QUEST_REFRESH_INTERVAL_MINUTES.getAsInt();
        var overworld = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld == null) return;
        long gameTime = overworld.getGameTime();
        long currentBucket = (intervalTicks > 0) ? gameTime / intervalTicks : 0;

        if (lastRefreshBucket < 0) {
            lastRefreshBucket = currentBucket;
            return;
        }

        if (currentBucket > lastRefreshBucket && intervalTicks > 0) {
            lastRefreshBucket = currentBucket;
            refreshAllPlayers(server);
        }

        flushDirtyPlayers(server);
    }

    private static void refreshAllPlayers(MinecraftServer server) {
        Component refreshMsg = Component.translatable("quest.improved_original.refresh_notify");
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
            refreshPlayerSlots(player, data);
            player.sendSystemMessage(refreshMsg);
        }
    }

    public static void refreshPlayerSlots(ServerPlayer player, QuestData data) {
        data.refreshUnlockedSlots(QuestGenerator::generateDailyQuest, player.getRandom());
        data.setLastRefreshTick(player.serverLevel().getGameTime());
        QuestEngine.get().markPlayerDirty(player);
    }

    private static void flushDirtyPlayers(MinecraftServer server) {
        QuestEngine engine = QuestEngine.get();
        for (ServerPlayer player : engine.getDirtyPlayers()) {
            QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
            engine.syncToPlayerSilent(player, data);
        }
        engine.clearDirtyPlayers();
    }
}
