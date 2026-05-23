// 任务定义（不可变记录）：类型、目标ID、目标数量、奖励物品+数量、描述文本，含Codec/StreamCodec序列化
package com.alma.improved_original.quest;

import com.alma.improved_original.ImprovedOriginal;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record QuestDefinition(
        QuestType type,
        ResourceLocation targetId,
        int targetCount,
        ResourceLocation rewardItem,
        int rewardCount,
        String name,
        String description
) {
    public static final Codec<QuestDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    QuestType.CODEC.fieldOf("type").forGetter(QuestDefinition::type),
                    ResourceLocation.CODEC.fieldOf("target").forGetter(QuestDefinition::targetId),
                    Codec.INT.fieldOf("count").forGetter(QuestDefinition::targetCount),
                    ResourceLocation.CODEC.fieldOf("rewardItem").forGetter(QuestDefinition::rewardItem),
                    Codec.INT.fieldOf("rewardCount").forGetter(QuestDefinition::rewardCount),
                    Codec.STRING.optionalFieldOf("name", "").forGetter(QuestDefinition::name),
                    Codec.STRING.optionalFieldOf("description", "").forGetter(QuestDefinition::description)
            ).apply(instance, QuestDefinition::new)
    );

    public static final StreamCodec<FriendlyByteBuf, QuestDefinition> STREAM_CODEC =
            StreamCodec.composite(
                    QuestType.STREAM_CODEC, QuestDefinition::type,
                    ResourceLocation.STREAM_CODEC, QuestDefinition::targetId,
                    ByteBufCodecs.VAR_INT, QuestDefinition::targetCount,
                    ResourceLocation.STREAM_CODEC, QuestDefinition::rewardItem,
                    ByteBufCodecs.VAR_INT, QuestDefinition::rewardCount,
                    ByteBufCodecs.STRING_UTF8, QuestDefinition::name,
                    ByteBufCodecs.STRING_UTF8, QuestDefinition::description,
                    QuestDefinition::new
            );

    public ItemStack createReward() {
        Item item = BuiltInRegistries.ITEM.get(rewardItem);
        return new ItemStack(item, rewardCount);
    }

    public String getDescriptionKey() {
        return "quest." + ImprovedOriginal.MOD_ID + ".desc." + type.getTranslationKeySuffix();
    }

    public Component getTargetDisplayName() {
        return switch (type) {
            case BREAK_BLOCK, CRAFT_ITEM, COLLECT_ITEM ->
                    BuiltInRegistries.ITEM.getOptional(targetId)
                            .map(Item::getDescription)
                            .orElse(Component.literal(targetId.toString()));
            case KILL_ENTITY ->
                    BuiltInRegistries.ENTITY_TYPE.getOptional(targetId)
                            .map(EntityType::getDescription)
                            .orElse(Component.literal(targetId.toString()));
            case FIND_STRUCTURE ->
                    BuiltInRegistries.STRUCTURE_TYPE.getOptional(
                            ResourceKey.create(BuiltInRegistries.STRUCTURE_TYPE.key(), targetId))
                            .map(s -> Component.translatable("structure." + targetId.getNamespace() + "." + targetId.getPath()))
                            .orElse(Component.literal(targetId.getPath()));
        };
    }

    public Component getRewardDisplayName() {
        return BuiltInRegistries.ITEM.getOptional(rewardItem)
                .map(Item::getDescription)
                .orElse(Component.literal(rewardItem.toString()));
    }
}
