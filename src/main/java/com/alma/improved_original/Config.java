// 模组配置文件：任务刷新间隔、货币物品、消耗数量等可配置项
// ModConfigSpec 构建器模式：BUILDER.comment().define() / defineInRange() 链式定义
// 配置文件自动生成到 config/improved_original-common.toml
// QUEST_REFRESH_INTERVAL_MINUTES: 任务自动刷新间隔（分钟）
// CURRENCY_ITEM: 锁定/刷新消耗的货币物品ID，默认绿宝石
// LOCK_COST: 锁定单个槽位消耗的货币数量
// REFRESH_COST: 手动刷新消耗的货币数量
package com.alma.improved_original;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Quest system configuration
    public static final ModConfigSpec.IntValue QUEST_REFRESH_INTERVAL_MINUTES = BUILDER
            .comment("How often (in minutes) quests refresh for all players")
            .defineInRange("questRefreshIntervalMinutes", 60, 1, 1440);

    public static final ModConfigSpec.ConfigValue<String> CURRENCY_ITEM = BUILDER
            .comment("The item ID used as currency for locking and refreshing quests",
                     "Change this to use a modded coin item instead of emeralds",
                     "Example: \"thererelics:silver_coin\" or \"minecraft:diamond\"")
            .define("currencyItem", "minecraft:emerald");

    public static final ModConfigSpec.IntValue LOCK_COST = BUILDER
            .comment("Number of currency items consumed when locking a quest")
            .defineInRange("lockCost", 5, 1, 64);

    public static final ModConfigSpec.IntValue REFRESH_COST = BUILDER
            .comment("Number of currency items consumed when manually refreshing quests")
            .defineInRange("refreshCost", 10, 1, 64);

    public static final ModConfigSpec.IntValue DAILY_SLOTS = BUILDER
            .comment("Number of daily quest slots")
            .defineInRange("dailySlots", 3, 1, 5);

    public static final ModConfigSpec.IntValue CHAIN_SLOTS = BUILDER
            .comment("Number of chain quest slots. Set to 0 to disable chain quests")
            .defineInRange("chainSlots", 2, 0, 5);

    public static final ModConfigSpec.DoubleValue COMMON_WEIGHT = BUILDER
            .comment("Generation weight multiplier for COMMON rarity quests")
            .defineInRange("commonWeight", 1.0, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue RARE_WEIGHT = BUILDER
            .comment("Generation weight multiplier for RARE rarity quests")
            .defineInRange("rareWeight", 0.5, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue EPIC_WEIGHT = BUILDER
            .comment("Generation weight multiplier for EPIC rarity quests")
            .defineInRange("epicWeight", 0.2, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue LEGENDARY_WEIGHT = BUILDER
            .comment("Generation weight multiplier for LEGENDARY rarity quests")
            .defineInRange("legendaryWeight", 0.05, 0.0, 10.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static ResourceLocation getCurrencyItem() {
        return ResourceLocation.parse(CURRENCY_ITEM.get());
    }

    public static double getRarityWeight(com.alma.improved_original.quest.component.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> COMMON_WEIGHT.get();
            case RARE -> RARE_WEIGHT.get();
            case EPIC -> EPIC_WEIGHT.get();
            case LEGENDARY -> LEGENDARY_WEIGHT.get();
        };
    }

    public static int getTotalSlotCount() {
        return DAILY_SLOTS.getAsInt() + CHAIN_SLOTS.getAsInt();
    }

    public static int getDailySlotCount() {
        return DAILY_SLOTS.getAsInt();
    }

    public static int getChainSlotCount() {
        return CHAIN_SLOTS.getAsInt();
    }
}