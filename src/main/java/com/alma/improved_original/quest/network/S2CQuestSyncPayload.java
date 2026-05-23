package com.alma.improved_original.quest.network;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.quest.QuestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record S2CQuestSyncPayload(QuestData data, Optional<QuestCompletion> completion, boolean openScreen) implements CustomPacketPayload {
    public static final Type<S2CQuestSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ImprovedOriginal.MOD_ID, "quest_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CQuestSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    QuestData.STREAM_CODEC, S2CQuestSyncPayload::data,
                    QuestCompletion.STREAM_CODEC, S2CQuestSyncPayload::completion,
                    ByteBufCodecs.BOOL, S2CQuestSyncPayload::openScreen,
                    S2CQuestSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static S2CQuestSyncPayload syncOnly(QuestData data) {
        return new S2CQuestSyncPayload(data, Optional.empty(), false);
    }

    public static S2CQuestSyncPayload withCompletion(QuestData data, QuestCompletion completion) {
        return new S2CQuestSyncPayload(data, Optional.of(completion), false);
    }

    public static S2CQuestSyncPayload openScreen(QuestData data) {
        return new S2CQuestSyncPayload(data, Optional.empty(), true);
    }

    public record QuestCompletion(String questDescription, int rewardEmeralds) {
        public static final StreamCodec<FriendlyByteBuf, Optional<QuestCompletion>> STREAM_CODEC =
                StreamCodec.of(
                        (buf, opt) -> {
                            buf.writeBoolean(opt.isPresent());
                            opt.ifPresent(c -> {
                                buf.writeUtf(c.questDescription);
                                buf.writeVarInt(c.rewardEmeralds);
                            });
                        },
                        buf -> {
                            if (!buf.readBoolean()) return Optional.empty();
                            return Optional.of(new QuestCompletion(buf.readUtf(), buf.readVarInt()));
                        }
                );
    }
}
