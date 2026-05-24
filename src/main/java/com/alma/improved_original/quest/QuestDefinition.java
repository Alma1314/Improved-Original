// 任务定义：类型、多目标物品列表、多奖励物品列表、名称、简介
// 每个目标和奖励由 ItemCount 记录表示（物品ID + 数量），支持多目标/多奖励
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

import java.util.List;

public record QuestDefinition(
        QuestType type,
        List<ItemCount> targets,
        List<ItemCount> rewards,
        String name,
        String description
) {
    // 物品+数量对：用于目标列表和奖励列表，含Codec/StreamCodec序列化
    public record ItemCount(ResourceLocation item, int count) {
        public static final Codec<ItemCount> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("item").forGetter(ItemCount::item),
                        Codec.INT.fieldOf("count").forGetter(ItemCount::count)
                ).apply(instance, ItemCount::new)
        );

        public static final StreamCodec<FriendlyByteBuf, ItemCount> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, ItemCount::item,
                        ByteBufCodecs.VAR_INT, ItemCount::count,
                        ItemCount::new
                );
    }

    // 名称+简介辅助记录：合并为单个网络传输单元以适配StreamCodec字段数限制
    private record QuestInfo(String name, String description) {
        static final StreamCodec<FriendlyByteBuf, QuestInfo> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, QuestInfo::name,
                        ByteBufCodecs.STRING_UTF8, QuestInfo::description,
                        QuestInfo::new
                );
    }

    public static final Codec<QuestDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    QuestType.CODEC.fieldOf("type").forGetter(QuestDefinition::type),
                    Codec.list(ItemCount.CODEC).fieldOf("targets").forGetter(QuestDefinition::targets),
                    Codec.list(ItemCount.CODEC).fieldOf("rewards").forGetter(QuestDefinition::rewards),
                    Codec.STRING.optionalFieldOf("name", "").forGetter(QuestDefinition::name),
                    Codec.STRING.optionalFieldOf("description", "").forGetter(QuestDefinition::description)
            ).apply(instance, QuestDefinition::new)
    );

    public static final StreamCodec<FriendlyByteBuf, QuestDefinition> STREAM_CODEC =
            StreamCodec.composite(
                    QuestType.STREAM_CODEC, QuestDefinition::type,
                    ItemCount.STREAM_CODEC.apply(ByteBufCodecs.list()), QuestDefinition::targets,
                    ItemCount.STREAM_CODEC.apply(ByteBufCodecs.list()), QuestDefinition::rewards,
                    QuestInfo.STREAM_CODEC, q -> new QuestInfo(q.name, q.description),
                    (type, targets, rewards, info) ->
                            new QuestDefinition(type, targets, rewards, info.name, info.description)
            );

    // 创建所有奖励物品的ItemStack列表
    public List<ItemStack> createRewards() {
        return rewards.stream().map(ic -> {
            Item item = BuiltInRegistries.ITEM.get(ic.item());
            return new ItemStack(item, ic.count());
        }).toList();
    }

    // 所有目标的总数量（用于UI聚合显示）
    public int totalTargetCount() {
        return targets.stream().mapToInt(ItemCount::count).sum();
    }

    // 获取任务类型描述键（如 "quest.improved_original.desc.break"）
    public String getDescriptionKey() {
        return "quest." + ImprovedOriginal.MOD_ID + ".desc." + type.getTranslationKeySuffix();
    }

    // 根据任务类型查找单个目标的显示名称（支持方块、物品、实体、结构）
    public Component getTargetDisplayName(ResourceLocation targetId) {
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

    // 获取所有目标的显示名称列表
    public List<Component> getTargetDisplayNames() {
        return targets.stream().map(t -> getTargetDisplayName(t.item())).toList();
    }

    // 查找单个奖励物品的显示名称
    public Component getRewardDisplayName(ResourceLocation rewardItem) {
        return BuiltInRegistries.ITEM.getOptional(rewardItem)
                .map(Item::getDescription)
                .orElse(Component.literal(rewardItem.toString()));
    }

    // 获取所有奖励物品的显示名称列表
    public List<Component> getRewardDisplayNames() {
        return rewards.stream().map(r -> getRewardDisplayName(r.item())).toList();
    }
}
