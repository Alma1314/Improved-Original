// 任务抽象接口：所有任务类型（每日/链式）的统一入口
// 组件化设计：任务行为由 TargetComponent/RewardComponent/ConditionComponent 组合定义
package com.alma.improved_original.quest.task;

import com.alma.improved_original.quest.component.ConditionComponent;
import com.alma.improved_original.quest.component.Rarity;
import com.alma.improved_original.quest.component.RewardComponent;
import com.alma.improved_original.quest.component.TargetComponent;
import com.alma.improved_original.quest.QuestType;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IQuestTask {
    String id();
    List<TargetComponent> targets();
    List<RewardComponent> rewards();
    List<ConditionComponent> conditions();
    List<String> unlocks();
    Rarity rarity();
    String name();
    String description();

    default List<ItemStack> createRewards(RandomSource random) {
        return rewards().stream().map(r -> {
            int count = r.countMin() + random.nextInt(r.countMax() - r.countMin() + 1);
            net.minecraft.world.item.Item item =
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(r.item());
            return new ItemStack(item, count);
        }).toList();
    }

    // 检查该任务是否有链行为（有前置条件或解锁后续）
    default boolean isChainTask() {
        return !conditions().isEmpty() || !unlocks().isEmpty();
    }

    enum TaskType {
        DAILY((byte) 0, "daily"),
        CHAIN((byte) 1, "chain");

        final byte streamId;
        final String codecKey;

        TaskType(byte streamId, String codecKey) {
            this.streamId = streamId;
            this.codecKey = codecKey;
        }

        static TaskType from(IQuestTask task) {
            return task instanceof ChainQuest ? CHAIN : DAILY;
        }

        static TaskType fromStreamId(byte id) {
            for (TaskType t : values()) {
                if (t.streamId == id) return t;
            }
            return DAILY;
        }
    }

    Codec<IQuestTask> CODEC = Codec.STRING.dispatch(
            task -> TaskType.from(task).codecKey,
            type -> {
                for (TaskType t : TaskType.values()) {
                    if (t.codecKey.equals(type)) {
                        return switch (t) {
                            case CHAIN -> ChainQuest.CODEC.fieldOf("task");
                            case DAILY -> DailyQuest.CODEC.fieldOf("task");
                        };
                    }
                }
                return DailyQuest.CODEC.fieldOf("task");
            }
    );

    StreamCodec<FriendlyByteBuf, IQuestTask> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public IQuestTask decode(FriendlyByteBuf buf) {
            byte typeId = buf.readByte();
            TaskType type = TaskType.fromStreamId(typeId);
            return switch (type) {
                case CHAIN -> ChainQuest.STREAM_CODEC.decode(buf);
                case DAILY -> DailyQuest.STREAM_CODEC.decode(buf);
            };
        }

        @Override
        public void encode(FriendlyByteBuf buf, IQuestTask task) {
            TaskType type = TaskType.from(task);
            buf.writeByte(type.streamId);
            switch (type) {
                case CHAIN -> ChainQuest.STREAM_CODEC.encode(buf, (ChainQuest) task);
                case DAILY -> DailyQuest.STREAM_CODEC.encode(buf, (DailyQuest) task);
            }
        }
    };

    default Component getTargetDisplayName(TargetComponent target) {
        return switch (target.type()) {
            case BREAK_BLOCK, CRAFT_ITEM, COLLECT_ITEM ->
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(target.item())
                            .map(net.minecraft.world.item.Item::getDescription)
                            .orElse(Component.literal(target.item().toString()));
            case KILL_ENTITY ->
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(target.item())
                            .map(net.minecraft.world.entity.EntityType::getDescription)
                            .orElse(Component.literal(target.item().toString()));
            case FIND_STRUCTURE ->
                    net.minecraft.core.registries.BuiltInRegistries.STRUCTURE_TYPE.getOptional(
                            net.minecraft.resources.ResourceKey.create(
                                    net.minecraft.core.registries.BuiltInRegistries.STRUCTURE_TYPE.key(), target.item()))
                            .map(s -> Component.translatable(
                                    "structure." + target.item().getNamespace() + "." + target.item().getPath()))
                            .orElse(Component.literal(target.item().getPath()));
        };
    }

    default List<Component> getTargetDisplayNames() {
        return targets().stream().map(this::getTargetDisplayName).toList();
    }

    default Component getRewardDisplayName(ResourceLocation rewardItem) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(rewardItem)
                .map(net.minecraft.world.item.Item::getDescription)
                .orElse(Component.literal(rewardItem.toString()));
    }

    default List<Component> getRewardDisplayNames() {
        return rewards().stream().map(r -> getRewardDisplayName(r.item())).toList();
    }
}
