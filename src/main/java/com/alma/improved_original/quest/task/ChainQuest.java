// 链式任务实现：带前置条件和/或解锁后续的任务
// 不参与自动刷新，由 ChainResolver 在条件满足时分配到链槽位
// 条件列表可能为空（链的第一步），unlock 列表非空则完成时触发后续
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

public record ChainQuest(
        String id,
        List<TargetComponent> targets,
        List<RewardComponent> rewards,
        List<ConditionComponent> conditions,
        List<String> unlocks,
        Rarity rarity,
        String name,
        String description
) implements IQuestTask {

    // 辅助记录：将 conditions + unlocks 合并为一个传输单元以适配 StreamCodec 字段数限制
    private record ChainConditions(List<ConditionComponent> conditions, List<String> unlocks) {
        static final StreamCodec<FriendlyByteBuf, ChainConditions> STREAM_CODEC =
                StreamCodec.composite(
                        ConditionComponent.STREAM_CODEC.apply(ByteBufCodecs.list()), ChainConditions::conditions,
                        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ChainConditions::unlocks,
                        ChainConditions::new
                );
    }

    // 辅助记录：将 name + description 合并为一个传输单元
    private record QuestInfo(String name, String description) {
        static final StreamCodec<FriendlyByteBuf, QuestInfo> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, QuestInfo::name,
                        ByteBufCodecs.STRING_UTF8, QuestInfo::description,
                        QuestInfo::new
                );
    }

    public static final Codec<ChainQuest> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(ChainQuest::id),
                    Codec.list(TargetComponent.CODEC).fieldOf("targets").forGetter(ChainQuest::targets),
                    Codec.list(RewardComponent.CODEC).fieldOf("rewards").forGetter(ChainQuest::rewards),
                    Codec.list(ConditionComponent.CODEC).optionalFieldOf("conditions", List.of()).forGetter(ChainQuest::conditions),
                    Codec.list(Codec.STRING).optionalFieldOf("unlocks", List.of()).forGetter(ChainQuest::unlocks),
                    Rarity.CODEC.optionalFieldOf("rarity", Rarity.COMMON).forGetter(ChainQuest::rarity),
                    Codec.STRING.optionalFieldOf("name", "").forGetter(ChainQuest::name),
                    Codec.STRING.optionalFieldOf("description", "").forGetter(ChainQuest::description)
            ).apply(instance, ChainQuest::new)
    );

    public static final StreamCodec<FriendlyByteBuf, ChainQuest> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ChainQuest::id,
                    TargetComponent.STREAM_CODEC.apply(ByteBufCodecs.list()), ChainQuest::targets,
                    RewardComponent.STREAM_CODEC.apply(ByteBufCodecs.list()), ChainQuest::rewards,
                    ChainConditions.STREAM_CODEC, cc -> new ChainConditions(cc.conditions(), cc.unlocks()),
                    Rarity.STREAM_CODEC, ChainQuest::rarity,
                    QuestInfo.STREAM_CODEC, q -> new QuestInfo(q.name(), q.description()),
                    (id, targets, rewards, cc, rarity, info) ->
                            new ChainQuest(id, targets, rewards, cc.conditions(), cc.unlocks(), rarity, info.name(), info.description())
            );
}
