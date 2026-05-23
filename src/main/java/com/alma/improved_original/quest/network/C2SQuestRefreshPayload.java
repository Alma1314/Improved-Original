package com.alma.improved_original.quest.network;

import com.alma.improved_original.ImprovedOriginal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SQuestRefreshPayload() implements CustomPacketPayload {
    public static final Type<C2SQuestRefreshPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ImprovedOriginal.MOD_ID, "quest_refresh"));

    public static final StreamCodec<FriendlyByteBuf, C2SQuestRefreshPayload> STREAM_CODEC =
            StreamCodec.unit(new C2SQuestRefreshPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
