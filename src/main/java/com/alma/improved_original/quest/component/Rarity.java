// 稀有度枚举：COMMON/RARE/EPIC/LEGENDARY，控制 UI 颜色和生成权重
// CODEC/STREAM_CODEC 用于网络传输和 NBT 持久化
package com.alma.improved_original.quest.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum Rarity {
    COMMON(0xFF888888),   // gray
    RARE(0xFF4488FF),     // blue
    EPIC(0xFFAA44FF),     // purple
    LEGENDARY(0xFFFFAA00); // gold

    private final int color;

    Rarity(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public static final Codec<Rarity> CODEC =
            Codec.STRING.xmap(name -> Rarity.valueOf(name.toUpperCase()), Rarity::name);

    public static final StreamCodec<FriendlyByteBuf, Rarity> STREAM_CODEC =
            StreamCodec.of(
                    (buf, val) -> buf.writeEnum(val),
                    buf -> buf.readEnum(Rarity.class)
            );
}
