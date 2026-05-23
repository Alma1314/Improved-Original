// 网络包注册器：注册S2C同步包和C2S锁定包、刷新包的处理函数
package com.alma.improved_original.quest.network;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.quest.QuestManager;
import com.alma.improved_original.quest.client.QuestClientEvents;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPayloadHandlers {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ImprovedOriginal.MOD_ID)
                .versioned("1.0.0")
                .optional();

        registrar.playToClient(
                S2CQuestSyncPayload.TYPE,
                S2CQuestSyncPayload.STREAM_CODEC,
                (payload, context) -> QuestClientEvents.onQuestSyncReceived(payload)
        );

        registrar.playToServer(
                C2SQuestLockPayload.TYPE,
                C2SQuestLockPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() != null) {
                        QuestManager.handleLockPacket(context.player(), payload.slot());
                    }
                }
        );

        registrar.playToServer(
                C2SQuestRefreshPayload.TYPE,
                C2SQuestRefreshPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() != null) {
                        QuestManager.handleRefreshPacket(context.player());
                    }
                }
        );

        registrar.playToServer(
                C2SQuestOpenPayload.TYPE,
                C2SQuestOpenPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() != null) {
                        QuestManager.handleOpenScreenPacket(context.player());
                    }
                }
        );
    }
}
