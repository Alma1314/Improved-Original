// 前置条件组件：需要完成的任务 ID + 完成次数
// 用于 ChainQuest：所有条件满足后才能解锁该任务
package com.alma.improved_original.quest.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ConditionComponent(String requiredQuestId, int requiredCount) {

    public ConditionComponent {
        if (requiredCount < 1) {
            requiredCount = 1;
        }
    }

    public static final Codec<ConditionComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("requiredQuestId").forGetter(ConditionComponent::requiredQuestId),
                    Codec.INT.optionalFieldOf("requiredCount", 1).forGetter(ConditionComponent::requiredCount)
            ).apply(instance, ConditionComponent::new)
    );

    public static final StreamCodec<FriendlyByteBuf, ConditionComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ConditionComponent::requiredQuestId,
                    ByteBufCodecs.VAR_INT, ConditionComponent::requiredCount,
                    ConditionComponent::new
            );
}
