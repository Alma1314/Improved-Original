// 客户端→服务端：切换锁定/解锁指定槽位的任务
package com.alma.improved_original.quest.network;

import com.alma.improved_original.ImprovedOriginal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SQuestLockPayload(int slot) implements CustomPacketPayload {
    public static final Type<C2SQuestLockPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ImprovedOriginal.MOD_ID, "quest_lock"));

    public static final StreamCodec<FriendlyByteBuf, C2SQuestLockPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, C2SQuestLockPayload::slot,
                    C2SQuestLockPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
