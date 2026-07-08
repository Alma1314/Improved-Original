// 统一 C2S 任务操作包：替代 C2SQuestOpenPayload / C2SQuestLockPayload / C2SQuestRefreshPayload
// ActionType 枚举区分操作类型，slot 参数仅 LOCK 操作有效
package com.alma.improved_original.quest.network;

import com.alma.improved_original.ImprovedOriginal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record QuestActionPayload(ActionType action, int slot) implements CustomPacketPayload {

    public enum ActionType {
        OPEN,
        LOCK,
        REFRESH,
        CHAIN_PANEL
    }

    public static final CustomPacketPayload.Type<QuestActionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ImprovedOriginal.MOD_ID, "quest_action"));

    public static final StreamCodec<FriendlyByteBuf, QuestActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(
                            (buf, val) -> buf.writeEnum(val),
                            buf -> buf.readEnum(ActionType.class)
                    ), QuestActionPayload::action,
                    ByteBufCodecs.VAR_INT, QuestActionPayload::slot,
                    QuestActionPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static QuestActionPayload open() {
        return new QuestActionPayload(ActionType.OPEN, -1);
    }

    public static QuestActionPayload lock(int slot) {
        return new QuestActionPayload(ActionType.LOCK, slot);
    }

    public static QuestActionPayload refresh() {
        return new QuestActionPayload(ActionType.REFRESH, -1);
    }

    public static QuestActionPayload chainPanel() {
        return new QuestActionPayload(ActionType.CHAIN_PANEL, -1);
    }
}
