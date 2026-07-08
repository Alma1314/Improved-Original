// 单个任务槽位状态：可选任务（空槽位）、每目标分别的进度列表、锁定标记、槽位类型与链ID
// 完成状态在构造时预计算，避免每次检测都遍历所有target
package com.alma.improved_original.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.stream.IntStream;

enum QuestSlotType {
    DAILY,
    CHAIN;

    public static final Codec<QuestSlotType> CODEC =
            Codec.STRING.xmap(name -> QuestSlotType.valueOf(name.toUpperCase()), QuestSlotType::name);

    public static final StreamCodec<FriendlyByteBuf, QuestSlotType> STREAM_CODEC =
            StreamCodec.of(
                    (buf, val) -> buf.writeEnum(val),
                    buf -> buf.readEnum(QuestSlotType.class)
            );
}

public record QuestSlotData(
        Optional<com.alma.improved_original.quest.task.IQuestTask> quest,
        IntArrayList perTargetProgress,
        boolean locked,
        boolean complete,
        QuestSlotType slotType,
        Optional<String> chainId
) {
    // Compact constructor: 自动计算完成状态
    public QuestSlotData {
        if (quest.isPresent()
                && perTargetProgress.size() == quest.get().targets().size()
                && IntStream.range(0, perTargetProgress.size())
                        .allMatch(i -> perTargetProgress.getInt(i) >= quest.get().targets().get(i).count())) {
            complete = true;
        }
    }

    public boolean isComplete() {
        return complete;
    }

    public static QuestSlotData empty() {
        return new QuestSlotData(Optional.empty(), new IntArrayList(), false, false,
                QuestSlotType.DAILY, Optional.empty());
    }

    public static QuestSlotData emptyChain() {
        return new QuestSlotData(Optional.empty(), new IntArrayList(), false, false,
                QuestSlotType.CHAIN, Optional.empty());
    }

    // 所有目标的聚合进度（用于UI概览）
    public int progress() {
        int sum = 0;
        for (int i = 0; i < perTargetProgress.size(); i++) {
            sum += perTargetProgress.getInt(i);
        }
        return sum;
    }

    // CODEC: .optionalFieldOf() for new fields ensures backward-compatible NBT loading
    public static final Codec<QuestSlotData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    com.alma.improved_original.quest.task.IQuestTask.CODEC.optionalFieldOf("quest").forGetter(QuestSlotData::quest),
                    Codec.INT.listOf().xmap(IntArrayList::new, java.util.ArrayList::new)
                            .fieldOf("progress").forGetter(QuestSlotData::perTargetProgress),
                    Codec.BOOL.optionalFieldOf("locked", false).forGetter(QuestSlotData::locked),
                    Codec.BOOL.optionalFieldOf("complete", false).forGetter(QuestSlotData::isComplete),
                    QuestSlotType.CODEC.optionalFieldOf("slotType", QuestSlotType.DAILY).forGetter(QuestSlotData::slotType),
                    Codec.STRING.optionalFieldOf("chainId").forGetter(QuestSlotData::chainId)
            ).apply(instance, QuestSlotData::new)
    );

    public static final StreamCodec<FriendlyByteBuf, QuestSlotData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(com.alma.improved_original.quest.task.IQuestTask.STREAM_CODEC),
                            QuestSlotData::quest,
                    StreamCodec.of(
                            (buf, list) -> {
                                buf.writeVarInt(list.size());
                                for (int i = 0; i < list.size(); i++) {
                                    buf.writeVarInt(list.getInt(i));
                                }
                            },
                            buf -> {
                                int size = buf.readVarInt();
                                IntArrayList list = new IntArrayList(size);
                                for (int i = 0; i < size; i++) {
                                    list.add(buf.readVarInt());
                                }
                                return list;
                            }
                    ), QuestSlotData::perTargetProgress,
                    ByteBufCodecs.BOOL, QuestSlotData::locked,
                    ByteBufCodecs.BOOL, QuestSlotData::isComplete,
                    QuestSlotType.STREAM_CODEC, QuestSlotData::slotType,
                    ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), QuestSlotData::chainId,
                    QuestSlotData::new
            );
}
