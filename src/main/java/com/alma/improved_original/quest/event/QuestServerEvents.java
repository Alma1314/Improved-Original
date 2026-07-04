// 服务端Tick事件：检测任务刷新计时器 + 批量同步脏数据 + 结构探索检测
// 统一在 ServerTickEvent.Post 中处理，合并分散的tick订阅到单一路径
package com.alma.improved_original.quest.event;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.quest.ModAttachments;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = ImprovedOriginal.MOD_ID)
public class QuestServerEvents {

    private static int structureCheckCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // 1. 任务刷新计时器检查
        QuestManager.onServerTick(event.getServer());

        // 2. 结构探索检测（每20 tick，约1秒一次）
        structureCheckCounter++;
        if (structureCheckCounter >= 20) {
            structureCheckCounter = 0;
            checkStructureProximity(event);
        }
    }

    private static void checkStructureProximity(ServerTickEvent.Post event) {
        var registry = event.getServer().registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
            // 跳过无 FIND_STRUCTURE 目标的玩家，避免无意义的 getAllStructuresAt 调用
            if (!data.hasStructureTargets()) continue;

            var structuresAt = player.serverLevel().structureManager()
                    .getAllStructuresAt(player.blockPosition());
            if (structuresAt.isEmpty()) continue;

            Set<ResourceLocation> foundIds = new HashSet<>();
            for (var entry : structuresAt.entrySet()) {
                var key = registry.getResourceKey(entry.getKey());
                key.ifPresent(k -> foundIds.add(k.location()));
            }

            for (ResourceLocation id : foundIds) {
                QuestManager.onStructureEntered(player, id);
            }
        }
    }
}
