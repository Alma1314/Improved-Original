// 客户端→服务端：请求打开任务面板（按键触发）
package com.alma.improved_original.quest.network;

import com.alma.improved_original.ImprovedOriginal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SQuestOpenPayload() implements CustomPacketPayload {
    public static final Type<C2SQuestOpenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ImprovedOriginal.MOD_ID, "quest_open"));

    public static final StreamCodec<FriendlyByteBuf, C2SQuestOpenPayload> STREAM_CODEC =
            StreamCodec.unit(new C2SQuestOpenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
