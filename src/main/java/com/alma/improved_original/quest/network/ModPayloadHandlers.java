// 网络包注册器：注册统一 S2C 同步包和 C2S 操作包
package com.alma.improved_original.quest.network;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.quest.client.QuestClientEvents;
import com.alma.improved_original.quest.engine.QuestEngine;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPayloadHandlers {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ImprovedOriginal.MOD_ID)
                .versioned("2.0.0")
                .optional();

        registrar.playToClient(
                QuestSyncPayload.TYPE,
                QuestSyncPayload.STREAM_CODEC,
                (payload, context) -> QuestClientEvents.onQuestSyncReceived(payload)
        );

        registrar.playToServer(
                QuestActionPayload.TYPE,
                QuestActionPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() != null) {
                        switch (payload.action()) {
                            case OPEN -> QuestEngine.get().handleOpenScreenPacket(context.player());
                            case LOCK -> QuestEngine.get().handleLockPacket(context.player(), payload.slot());
                            case REFRESH -> QuestEngine.get().handleRefreshPacket(context.player());
                            case CHAIN_PANEL -> QuestEngine.get().handleChainPanelPacket(context.player());
                        }
                    }
                }
        );
    }
}
