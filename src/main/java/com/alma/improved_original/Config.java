// 模组配置文件：任务刷新间隔、绿宝石消耗、奖励范围等可配置项
package com.alma.improved_original;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    // Quest system configuration
    public static final ModConfigSpec.IntValue QUEST_REFRESH_INTERVAL_MINUTES = BUILDER
            .comment("How often (in minutes) quests refresh for all players")
            .defineInRange("questRefreshIntervalMinutes", 60, 1, 1440);

    public static final ModConfigSpec.IntValue EMERALD_LOCK_COST = BUILDER
            .comment("Number of emeralds consumed when locking a quest")
            .defineInRange("emeraldLockCost", 5, 1, 64);

    public static final ModConfigSpec.IntValue EMERALD_REFRESH_COST = BUILDER
            .comment("Number of emeralds consumed when manually refreshing quests")
            .defineInRange("emeraldRefreshCost", 10, 1, 64);

    public static final ModConfigSpec.IntValue QUEST_TARGET_COUNT_MIN = BUILDER
            .comment("Minimum target count for generated quests")
            .defineInRange("questTargetCountMin", 5, 1, 1024);

    public static final ModConfigSpec.IntValue QUEST_TARGET_COUNT_MAX = BUILDER
            .comment("Maximum target count for generated quests")
            .defineInRange("questTargetCountMax", 32, 1, 1024);

    public static final ModConfigSpec.IntValue QUEST_REWARD_MIN = BUILDER
            .comment("Minimum emerald reward for generated quests")
            .defineInRange("questRewardMin", 1, 1, 64);

    public static final ModConfigSpec.IntValue QUEST_REWARD_MAX = BUILDER
            .comment("Maximum emerald reward for generated quests")
            .defineInRange("questRewardMax", 10, 1, 64);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
