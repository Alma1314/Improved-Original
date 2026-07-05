// 玩家完整任务数据：3个槽位、上次刷新时间、激活状态，支持NBT持久化和网络传输
// active 标记控制"首次打开面板前不追踪进度"行为
// 通过 AttachmentType 自动附加到每个玩家，退出时保存、重进时恢复
// CODEC 用于 NBT 持久化，STREAM_CODEC 用于网络包同步
package com.alma.improved_original.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public class QuestData {
    public static final int SLOT_COUNT = 3;

    private final List<QuestSlotData> slots;
    private long lastRefreshTick;
    private boolean active;
    private transient boolean dirty;

    public QuestData(List<QuestSlotData> slots, long lastRefreshTick) {
        this(slots, lastRefreshTick, false);
    }

    public QuestData(List<QuestSlotData> slots, long lastRefreshTick, boolean active) {
        this.slots = new ArrayList<>(slots);
        while (this.slots.size() < SLOT_COUNT) {
            this.slots.add(QuestSlotData.empty());
        }
        this.lastRefreshTick = lastRefreshTick;
        this.active = active;
    }

    private QuestData() {
        this.slots = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.slots.add(QuestSlotData.empty());
        }
        this.lastRefreshTick = -1;
        this.active = false;
    }

    public static QuestData createFresh() {
        return new QuestData();
    }

    public List<QuestSlotData> getSlots() {
        return slots;
    }

    public QuestSlotData getSlot(int index) {
        return slots.get(index);
    }

    public Optional<QuestDefinition> getQuest(int index) {
        return slots.get(index).quest();
    }

    public int getProgress(int index) {
        return slots.get(index).progress();
    }

    public boolean isSlotLocked(int index) {
        return slots.get(index).locked();
    }

    public long getLastRefreshTick() {
        return lastRefreshTick;
    }

    public void setQuest(int index, QuestDefinition quest) {
        QuestSlotData old = slots.get(index);
        List<Integer> initProgress = new ArrayList<>();
        for (int i = 0; i < quest.targets().size(); i++) initProgress.add(0);
        slots.set(index, new QuestSlotData(Optional.of(quest), initProgress, old.locked(), false));
    }

    public void setProgress(int index, List<Integer> progress) {
        QuestSlotData old = slots.get(index);
        slots.set(index, new QuestSlotData(old.quest(), progress, old.locked(), false));
    }

    public boolean isSlotComplete(int index) {
        return slots.get(index).isComplete();
    }

    public void setSlotLocked(int index, boolean locked) {
        QuestSlotData old = slots.get(index);
        slots.set(index, new QuestSlotData(old.quest(), old.perTargetProgress(), locked, false));
    }

    public void clearSlot(int index) {
        slots.set(index, QuestSlotData.empty());
    }

    public void setLastRefreshTick(long tick) {
        this.lastRefreshTick = tick;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean hasAnyQuest() {
        return slots.stream().anyMatch(s -> s.quest().isPresent());
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    // 检查是否有 FIND_STRUCTURE 类型目标，用于跳过结构检查
    public boolean hasStructureTargets() {
        if (!active) return false;
        for (QuestSlotData slot : slots) {
            if (slot.quest().isPresent() && !slot.isComplete()) {
                for (var target : slot.quest().get().targets()) {
                    if (target.type() == QuestType.FIND_STRUCTURE) return true;
                }
            }
        }
        return false;
    }

    // 收集当前所有任务的target item列表，用于去重
    public Set<ResourceLocation> getExistingTargetItems() {
        Set<ResourceLocation> result = new HashSet<>();
        for (QuestSlotData slot : slots) {
            slot.quest().ifPresent(q ->
                q.targets().forEach(t -> result.add(t.item())));
        }
        return result;
    }

    // 刷新所有未锁定槽位，传入 quest supplier
    public void refreshUnlockedSlots(BiFunction<RandomSource, QuestData, QuestDefinition> supplier, RandomSource random) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!isSlotLocked(i)) {
                QuestDefinition quest = supplier.apply(random, this);
                if (quest != null) {
                    setQuest(i, quest);
                }
            }
        }
    }

    public static final Codec<QuestData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(QuestSlotData.CODEC).fieldOf("slots").forGetter(QuestData::getSlots),
                    Codec.LONG.fieldOf("last_refresh").forGetter(QuestData::getLastRefreshTick),
                    Codec.BOOL.optionalFieldOf("active", false).forGetter(QuestData::isActive)
            ).apply(instance, QuestData::new)
    );

    public static final StreamCodec<FriendlyByteBuf, QuestData> STREAM_CODEC =
            StreamCodec.composite(
                    QuestSlotData.STREAM_CODEC.apply(ByteBufCodecs.list()), QuestData::getSlots,
                    ByteBufCodecs.VAR_LONG, QuestData::getLastRefreshTick,
                    ByteBufCodecs.BOOL, QuestData::isActive,
                    QuestData::new
            );
}
