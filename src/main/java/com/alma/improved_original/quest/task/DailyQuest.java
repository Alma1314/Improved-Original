// 每日任务实现：无前置条件、无解锁后续的独立任务
// 参与自动刷新调度，由 DailyRefreshScheduler 管理生命周期
// CODEC + STREAM_CODEC 用于网络传输（via QuestSlotData 的 Optional<IQuestTask>）
package com.alma.improved_original.quest.task;

import com.alma.improved_original.quest.component.ConditionComponent;
import com.alma.improved_original.quest.component.Rarity;
import com.alma.improved_original.quest.component.RewardComponent;
import com.alma.improved_original.quest.component.TargetComponent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record DailyQuest(
        String id,
        List<TargetComponent> targets,
        List<RewardComponent> rewards,
        Rarity rarity,
        String name,
        String description
) implements IQuestTask {

    @Override
    public List<ConditionComponent> conditions() { return List.of(); }

    @Override
    public List<String> unlocks() { return List.of(); }

    public static final Codec<DailyQuest> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(DailyQuest::id),
                    Codec.list(TargetComponent.CODEC).fieldOf("targets").forGetter(DailyQuest::targets),
                    Codec.list(RewardComponent.CODEC).fieldOf("rewards").forGetter(DailyQuest::rewards),
                    Rarity.CODEC.optionalFieldOf("rarity", Rarity.COMMON).forGetter(DailyQuest::rarity),
                    Codec.STRING.optionalFieldOf("name", "").forGetter(DailyQuest::name),
                    Codec.STRING.optionalFieldOf("description", "").forGetter(DailyQuest::description)
            ).apply(instance, DailyQuest::new)
    );

    public static final StreamCodec<FriendlyByteBuf, DailyQuest> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, DailyQuest::id,
                    TargetComponent.STREAM_CODEC.apply(ByteBufCodecs.list()), DailyQuest::targets,
                    RewardComponent.STREAM_CODEC.apply(ByteBufCodecs.list()), DailyQuest::rewards,
                    Rarity.STREAM_CODEC, DailyQuest::rarity,
                    ByteBufCodecs.STRING_UTF8, DailyQuest::name,
                    ByteBufCodecs.STRING_UTF8, DailyQuest::description,
                    DailyQuest::new
            );
}
