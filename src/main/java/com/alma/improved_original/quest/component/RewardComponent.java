// 奖励组件：物品ID + 数量范围（生成时随机化到具体值）
// 从 QuestDefinition.ItemCount 提取，添加数量范围支持
package com.alma.improved_original.quest.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record RewardComponent(ResourceLocation item, int countMin, int countMax) {

    public static final Codec<RewardComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("item").forGetter(RewardComponent::item),
                    Codec.INT.fieldOf("countMin").forGetter(RewardComponent::countMin),
                    Codec.INT.fieldOf("countMax").forGetter(RewardComponent::countMax)
            ).apply(instance, RewardComponent::new)
    );

    public static final StreamCodec<FriendlyByteBuf, RewardComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, RewardComponent::item,
                    ByteBufCodecs.VAR_INT, RewardComponent::countMin,
                    ByteBufCodecs.VAR_INT, RewardComponent::countMax,
                    RewardComponent::new
            );
}
