package com.alma.improved_original.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuestData {
    public static final int SLOT_COUNT = 3;

    private final List<QuestSlotData> slots;
    private long lastRefreshTick;

    public QuestData(List<QuestSlotData> slots, long lastRefreshTick) {
        this.slots = new ArrayList<>(slots);
        while (this.slots.size() < SLOT_COUNT) {
            this.slots.add(QuestSlotData.empty());
        }
        this.lastRefreshTick = lastRefreshTick;
    }

    private QuestData() {
        this.slots = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.slots.add(QuestSlotData.empty());
        }
        this.lastRefreshTick = -1;
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
        slots.set(index, new QuestSlotData(Optional.of(quest), 0, old.locked()));
    }

    public void setProgress(int index, int progress) {
        QuestSlotData old = slots.get(index);
        slots.set(index, new QuestSlotData(old.quest(), progress, old.locked()));
    }

    public void setSlotLocked(int index, boolean locked) {
        QuestSlotData old = slots.get(index);
        slots.set(index, new QuestSlotData(old.quest(), old.progress(), locked));
    }

    public void clearSlot(int index) {
        slots.set(index, QuestSlotData.empty());
    }

    public void setLastRefreshTick(long tick) {
        this.lastRefreshTick = tick;
    }

    public boolean hasAnyQuest() {
        return slots.stream().anyMatch(s -> s.quest().isPresent());
    }

    public static final Codec<QuestData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(QuestSlotData.CODEC).fieldOf("slots").forGetter(QuestData::getSlots),
                    Codec.LONG.fieldOf("last_refresh").forGetter(QuestData::getLastRefreshTick)
            ).apply(instance, QuestData::new)
    );

    public static final StreamCodec<FriendlyByteBuf, QuestData> STREAM_CODEC =
            StreamCodec.composite(
                    QuestSlotData.STREAM_CODEC.apply(ByteBufCodecs.list()), QuestData::getSlots,
                    ByteBufCodecs.VAR_LONG, QuestData::getLastRefreshTick,
                    QuestData::new
            );
}
