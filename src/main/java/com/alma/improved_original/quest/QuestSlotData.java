// 单个任务槽位状态：可选任务（空槽位）、每目标分别的进度列表、锁定标记
// 完成状态在构造时预计算，避免每次检测都遍历所有target
package com.alma.improved_original.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public record QuestSlotData(
        Optional<QuestDefinition> quest,
        List<Integer> perTargetProgress,
        boolean locked,
        boolean complete
) {
    // Compact constructor: 自动计算完成状态
    public QuestSlotData {
        if (quest.isPresent()
                && perTargetProgress.size() == quest.get().targets().size()
                && IntStream.range(0, perTargetProgress.size())
                        .allMatch(i -> perTargetProgress.get(i) >= quest.get().targets().get(i).count())) {
            complete = true;
        }
    }

    public boolean isComplete() {
        return complete;
    }

    public static final Codec<QuestSlotData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    QuestDefinition.CODEC.optionalFieldOf("quest").forGetter(QuestSlotData::quest),
                    Codec.list(Codec.INT).fieldOf("progress").forGetter(QuestSlotData::perTargetProgress),
                    Codec.BOOL.fieldOf("locked").forGetter(QuestSlotData::locked),
                    Codec.BOOL.optionalFieldOf("complete", false).forGetter(QuestSlotData::isComplete)
            ).apply(instance, QuestSlotData::new)
    );

    public static final StreamCodec<FriendlyByteBuf, QuestSlotData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(QuestDefinition.STREAM_CODEC), QuestSlotData::quest,
                    ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), QuestSlotData::perTargetProgress,
                    ByteBufCodecs.BOOL, QuestSlotData::locked,
                    ByteBufCodecs.BOOL, QuestSlotData::isComplete,
                    QuestSlotData::new
            );

    public static QuestSlotData empty() {
        return new QuestSlotData(Optional.empty(), List.of(), false, false);
    }

    // 所有目标的聚合进度（用于UI概览）
    public int progress() {
        return perTargetProgress.stream().mapToInt(Integer::intValue).sum();
    }
}