package com.alma.improved_original.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record QuestSlotData(
        Optional<QuestDefinition> quest,
        int progress,
        boolean locked
) {
    public static final Codec<QuestSlotData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    QuestDefinition.CODEC.optionalFieldOf("quest").forGetter(QuestSlotData::quest),
                    Codec.INT.fieldOf("progress").forGetter(QuestSlotData::progress),
                    Codec.BOOL.fieldOf("locked").forGetter(QuestSlotData::locked)
            ).apply(instance, QuestSlotData::new)
    );

    public static final StreamCodec<FriendlyByteBuf, QuestSlotData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(QuestDefinition.STREAM_CODEC), QuestSlotData::quest,
                    ByteBufCodecs.VAR_INT, QuestSlotData::progress,
                    ByteBufCodecs.BOOL, QuestSlotData::locked,
                    QuestSlotData::new
            );

    public static QuestSlotData empty() {
        return new QuestSlotData(Optional.empty(), 0, false);
    }

    public boolean isComplete() {
        return quest.isPresent() && progress >= quest.get().targetCount();
    }
}
