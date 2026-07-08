// 任务目标组件：类型 + 物品/实体/结构 ID + 数量
// 从 QuestDefinition.QuestTarget 提取，移除对 QuestDefinition 的嵌套依赖
package com.alma.improved_original.quest.component;

import com.alma.improved_original.quest.QuestType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record TargetComponent(QuestType type, ResourceLocation item, int count) {

    public static final Codec<TargetComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    QuestType.CODEC.fieldOf("type").forGetter(TargetComponent::type),
                    ResourceLocation.CODEC.fieldOf("item").forGetter(TargetComponent::item),
                    Codec.INT.fieldOf("count").forGetter(TargetComponent::count)
            ).apply(instance, TargetComponent::new)
    );

    public static final StreamCodec<FriendlyByteBuf, TargetComponent> STREAM_CODEC =
            StreamCodec.composite(
                    QuestType.STREAM_CODEC, TargetComponent::type,
                    ResourceLocation.STREAM_CODEC, TargetComponent::item,
                    ByteBufCodecs.VAR_INT, TargetComponent::count,
                    TargetComponent::new
            );
}
