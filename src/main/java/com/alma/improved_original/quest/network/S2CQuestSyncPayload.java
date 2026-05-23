package com.alma.improved_original.quest.network;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.quest.QuestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CQuestSyncPayload(QuestData data) implements CustomPacketPayload {
    public static final Type<S2CQuestSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ImprovedOriginal.MOD_ID, "quest_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CQuestSyncPayload> STREAM_CODEC =
            QuestData.STREAM_CODEC.map(S2CQuestSyncPayload::new, S2CQuestSyncPayload::data);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
