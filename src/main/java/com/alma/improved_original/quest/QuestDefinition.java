package com.alma.improved_original.quest;

import com.alma.improved_original.ImprovedOriginal;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record QuestDefinition(
        QuestType type,
        ResourceLocation targetId,
        int targetCount,
        int rewardEmeralds
) {
    public static final Codec<QuestDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    QuestType.CODEC.fieldOf("type").forGetter(QuestDefinition::type),
                    ResourceLocation.CODEC.fieldOf("target").forGetter(QuestDefinition::targetId),
                    Codec.INT.fieldOf("count").forGetter(QuestDefinition::targetCount),
                    Codec.INT.fieldOf("reward").forGetter(QuestDefinition::rewardEmeralds)
            ).apply(instance, QuestDefinition::new)
    );

    public static final StreamCodec<FriendlyByteBuf, QuestDefinition> STREAM_CODEC =
            StreamCodec.composite(
                    QuestType.STREAM_CODEC, QuestDefinition::type,
                    ResourceLocation.STREAM_CODEC, QuestDefinition::targetId,
                    ByteBufCodecs.VAR_INT, QuestDefinition::targetCount,
                    ByteBufCodecs.VAR_INT, QuestDefinition::rewardEmeralds,
                    QuestDefinition::new
            );

    public ItemStack createReward() {
        return new ItemStack(Items.EMERALD, rewardEmeralds);
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
        };
    }
}
