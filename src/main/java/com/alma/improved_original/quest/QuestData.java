// 玩家完整任务数据：N个槽位（可配置）+ 活跃链进度 + 上次刷新时间 + 激活状态
// 支持 NBT 持久化（向后兼容）和网络传输
// activeChains 跟踪每个链的全局进度，ChainResolver 用它判断解锁
package com.alma.improved_original.quest;

import com.alma.improved_original.Config;
import com.alma.improved_original.quest.task.IQuestTask;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class QuestData {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<QuestSlotData> slots;
    private long lastRefreshTick;
    private boolean active;
    private final Map<String, ChainProgress> activeChains;

    public enum StepStatus { LOCKED, ACTIVE, DONE }

    public record StepInfo(String questId, StepStatus status) {
        public static final Codec<StepInfo> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("questId").forGetter(StepInfo::questId),
                        Codec.STRING.xmap(name -> StepStatus.valueOf(name.toUpperCase()), StepStatus::name)
                                .fieldOf("status").forGetter(StepInfo::status)
                ).apply(instance, StepInfo::new)
        );

        public static final StreamCodec<FriendlyByteBuf, StepInfo> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, StepInfo::questId,
                        StreamCodec.of(
                                (buf, val) -> buf.writeEnum(val),
                                buf -> buf.readEnum(StepStatus.class)
                        ), StepInfo::status,
                        StepInfo::new
                );
    }

    public record ChainProgress(String chainId, int completedStepCount, int totalStepCount,
                                String lastCompletedTaskId, List<StepInfo> steps) {
        public static final Codec<ChainProgress> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("chainId").forGetter(ChainProgress::chainId),
                        Codec.INT.fieldOf("completedStepCount").forGetter(ChainProgress::completedStepCount),
                        Codec.INT.fieldOf("totalStepCount").forGetter(ChainProgress::totalStepCount),
                        Codec.STRING.optionalFieldOf("lastCompletedTaskId", "").forGetter(ChainProgress::lastCompletedTaskId),
                        Codec.list(StepInfo.CODEC).optionalFieldOf("steps", List.of()).forGetter(ChainProgress::steps)
                ).apply(instance, ChainProgress::new)
        );

        public static final StreamCodec<FriendlyByteBuf, ChainProgress> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, ChainProgress::chainId,
                        ByteBufCodecs.VAR_INT, ChainProgress::completedStepCount,
                        ByteBufCodecs.VAR_INT, ChainProgress::totalStepCount,
                        ByteBufCodecs.STRING_UTF8, ChainProgress::lastCompletedTaskId,
                        StepInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), ChainProgress::steps,
                        ChainProgress::new
                );
    }

    public QuestData(List<QuestSlotData> slots, long lastRefreshTick, boolean active,
                     Map<String, ChainProgress> activeChains) {
        int totalSlots = Config.getTotalSlotCount();
        this.slots = new ArrayList<>(slots);
        // Ensure we have the right number of slots
        while (this.slots.size() < totalSlots) {
            // If we're adding slots beyond the daily count, they're chain slots
            if (this.slots.size() >= Config.getDailySlotCount()) {
                this.slots.add(QuestSlotData.emptyChain());
            } else {
                this.slots.add(QuestSlotData.empty());
            }
        }
        // Trim excess slots (config reduced)
        while (this.slots.size() > totalSlots) {
            QuestSlotData removed = this.slots.removeLast();
            if (removed.quest().isPresent()) {
                LOGGER.warn("Config downgrade: removed active quest slot '{}' (slot count reduced from {} to {})",
                        removed.quest().get().id(), this.slots.size() + 1, totalSlots);
            }
        }
        this.lastRefreshTick = lastRefreshTick;
        this.active = active;
        this.activeChains = new HashMap<>(activeChains);
    }

    // Backward-compatible constructor (old NBT without activeChains)
    public QuestData(List<QuestSlotData> slots, long lastRefreshTick, boolean active) {
        this(slots, lastRefreshTick, active, Map.of());
    }

    // Backward-compatible constructor (old NBT 2-arg)
    public QuestData(List<QuestSlotData> slots, long lastRefreshTick) {
        this(slots, lastRefreshTick, false, Map.of());
    }

    private QuestData() {
        this.slots = new ArrayList<>();
        int totalSlots = Config.getTotalSlotCount();
        int dailyCount = Config.getDailySlotCount();
        for (int i = 0; i < totalSlots; i++) {
            if (i < dailyCount) {
                this.slots.add(QuestSlotData.empty());
            } else {
                this.slots.add(QuestSlotData.emptyChain());
            }
        }
        this.lastRefreshTick = -1;
        this.active = false;
        this.activeChains = new HashMap<>();
    }

    public static QuestData createFresh() {
        return new QuestData();
    }

    public List<QuestSlotData> getSlots() { return slots; }
    public QuestSlotData getSlot(int index) { return slots.get(index); }
    public Optional<IQuestTask> getQuest(int index) { return slots.get(index).quest(); }
    public boolean isSlotLocked(int index) { return slots.get(index).locked(); }
    public boolean isSlotComplete(int index) { return slots.get(index).isComplete(); }
    public long getLastRefreshTick() { return lastRefreshTick; }
    public boolean isActive() { return active; }
    public void setSlotLocked(int index, boolean locked) {
        QuestSlotData old = slots.get(index);
        slots.set(index, new QuestSlotData(old.quest(), old.perTargetProgress(), locked, old.isComplete(),
                old.slotType(), old.chainId()));
    }

    public void clearSlot(int index) {
        QuestSlotData old = slots.get(index);
        QuestSlotType type = old.slotType();
        slots.set(index, type == QuestSlotType.CHAIN
                ? QuestSlotData.emptyChain() : QuestSlotData.empty());
    }

    public void setLastRefreshTick(long tick) { this.lastRefreshTick = tick; }
    public void setActive(boolean active) { this.active = active; }

    public Map<String, ChainProgress> getActiveChains() { return Collections.unmodifiableMap(activeChains); }

    public ChainProgress getChainProgress(String chainId) {
        return activeChains.get(chainId);
    }

    public void updateChainProgress(String chainId, ChainProgress progress) {
        activeChains.put(chainId, progress);
    }

    public boolean hasActiveChain(String chainId) {
        return activeChains.containsKey(chainId);
    }

    public void setQuest(int index, IQuestTask quest) {
        QuestSlotData old = slots.get(index);
        IntArrayList initProgress = new IntArrayList(quest.targets().size());
        for (int i = 0; i < quest.targets().size(); i++) initProgress.add(0);
        Optional<String> chainId = quest.isChainTask() ? Optional.of(quest.id()) : old.chainId();
        QuestSlotType type = chainId.isPresent()
                ? QuestSlotType.CHAIN : QuestSlotType.DAILY;
        slots.set(index, new QuestSlotData(Optional.of(quest), initProgress, old.locked(), false, type, chainId));
    }

    public void setProgress(int index, List<Integer> progress) {
        QuestSlotData old = slots.get(index);
        IntArrayList ial = progress instanceof IntArrayList ia ? ia : new IntArrayList(progress);
        slots.set(index, new QuestSlotData(old.quest(), ial, old.locked(), old.isComplete(),
                old.slotType(), old.chainId()));
    }

    public boolean hasAnyQuest() {
        return slots.stream().anyMatch(s -> s.quest().isPresent());
    }

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

    public Set<ResourceLocation> getExistingTargetItems() {
        Set<ResourceLocation> result = new HashSet<>();
        for (QuestSlotData slot : slots) {
            slot.quest().ifPresent(q ->
                    q.targets().forEach(t -> result.add(t.item())));
        }
        return result;
    }

    public void refreshUnlockedSlots(BiFunction<RandomSource, QuestData, IQuestTask> supplier, RandomSource random) {
        int dailyCount = Config.getDailySlotCount();
        for (int i = 0; i < dailyCount; i++) {
            if (!isSlotLocked(i) && slots.get(i).slotType() == QuestSlotType.DAILY) {
                IQuestTask quest = supplier.apply(random, this);
                if (quest != null) {
                    setQuest(i, quest);
                }
            }
        }
    }

    // CODEC with backward-compatible optional fields
    public static final Codec<QuestData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(QuestSlotData.CODEC).fieldOf("slots").forGetter(QuestData::getSlots),
                    Codec.LONG.fieldOf("last_refresh").forGetter(QuestData::getLastRefreshTick),
                    Codec.BOOL.optionalFieldOf("active", false).forGetter(QuestData::isActive),
                    Codec.unboundedMap(Codec.STRING, ChainProgress.CODEC)
                            .optionalFieldOf("activeChains", Map.of()).forGetter(QuestData::getActiveChains)
            ).apply(instance, QuestData::new)
    );

    public static final StreamCodec<FriendlyByteBuf, QuestData> STREAM_CODEC =
            StreamCodec.composite(
                    QuestSlotData.STREAM_CODEC.apply(ByteBufCodecs.list()), QuestData::getSlots,
                    ByteBufCodecs.VAR_LONG, QuestData::getLastRefreshTick,
                    ByteBufCodecs.BOOL, QuestData::isActive,
                    ByteBufCodecs.map(
                            HashMap::new,
                            ByteBufCodecs.STRING_UTF8,
                            ChainProgress.STREAM_CODEC
                    ), QuestData::getActiveChains,
                    QuestData::new
            );
}
