// 任务类型枚举：BREAK_BLOCK(破坏方块)、CRAFT_ITEM(合成物品)、KILL_ENTITY(击杀实体)、COLLECT_ITEM(收集物品)
package com.alma.improved_original.quest;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum QuestType {
    BREAK_BLOCK,
    CRAFT_ITEM,
    KILL_ENTITY,
    COLLECT_ITEM;

    public static final Codec<QuestType> CODEC =
            Codec.STRING.xmap(name -> QuestType.valueOf(name.toUpperCase()), QuestType::name);

    public static final StreamCodec<FriendlyByteBuf, QuestType> STREAM_CODEC =
            StreamCodec.of(
                    (buf, type) -> buf.writeEnum(type),
                    buf -> buf.readEnum(QuestType.class)
            );

    public String getTranslationKeySuffix() {
        return switch (this) {
            case BREAK_BLOCK -> "break";
            case CRAFT_ITEM -> "craft";
            case KILL_ENTITY -> "kill";
            case COLLECT_ITEM -> "collect";
        };
    }
}
