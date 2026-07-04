// 任务池 JSON 构建工具 — ModQuestPoolProvider 和 QuestPoolConfig 共享
// 消除两个文件中 ~150 行重复的 addEntry / addMultiEntry / addGemExchangeEntry / buildDefaultEntries 代码
package com.alma.improved_original.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

final class QuestsPoolBuilder {

    static JsonArray buildDefaultEntries() {
        JsonArray entries = new JsonArray();

        // 破坏方块
        addEntry(entries, "BREAK_BLOCK", "minecraft:stone", 10, 64, "minecraft:emerald", 1, 10, 20);
        addEntry(entries, "BREAK_BLOCK", "minecraft:oak_log", 10, 48, "minecraft:emerald", 1, 8, 15);
        addEntry(entries, "BREAK_BLOCK", "minecraft:coal_ore", 8, 32, "minecraft:emerald", 1, 5, 10);
        addEntry(entries, "BREAK_BLOCK", "minecraft:iron_ore", 5, 24, "minecraft:emerald", 1, 7, 8);
        addEntry(entries, "BREAK_BLOCK", "minecraft:dirt", 20, 80, "minecraft:emerald", 1, 8, 20);
        addEntry(entries, "COLLECT_ITEM", "minecraft:dirt", 1, 1, "minecraft:grass_block", 1, 1, 5);
        addEntry(entries, "BREAK_BLOCK", "minecraft:deepslate", 16, 64, "minecraft:emerald", 1, 8, 18);
        addEntry(entries, "BREAK_BLOCK", "minecraft:sand", 12, 48, "minecraft:emerald", 1, 6, 15);
        addEntry(entries, "BREAK_BLOCK", "minecraft:gravel", 10, 32, "minecraft:emerald", 1, 3, 12);
        addEntry(entries, "BREAK_BLOCK", "minecraft:netherrack", 20, 80, "minecraft:emerald", 1, 5, 15);

        // 击杀实体
        addEntry(entries, "KILL_ENTITY", "minecraft:zombie", 5, 20, "minecraft:emerald", 1, 5, 20);
        addEntry(entries, "KILL_ENTITY", "minecraft:skeleton", 5, 20, "minecraft:emerald", 1, 5, 20);
        addEntry(entries, "KILL_ENTITY", "minecraft:spider", 3, 15, "minecraft:emerald", 1, 5, 15);
        addEntry(entries, "KILL_ENTITY", "minecraft:creeper", 3, 15, "minecraft:emerald", 1, 6, 15);
        addEntry(entries, "KILL_ENTITY", "minecraft:enderman", 3, 10, "minecraft:emerald", 2, 8, 8);
        addEntry(entries, "KILL_ENTITY", "minecraft:witch", 3, 10, "minecraft:emerald", 2, 8, 8);
        addEntry(entries, "KILL_ENTITY", "minecraft:drowned", 3, 15, "minecraft:emerald", 1, 4, 12);
        addEntry(entries, "KILL_ENTITY", "minecraft:husk", 3, 15, "minecraft:emerald", 1, 4, 12);

        // CRAFT_ITEM
        addEntry(entries, "CRAFT_ITEM", "minecraft:crafting_table", 3, 10, "minecraft:emerald", 1, 3, 10);
        addEntry(entries, "CRAFT_ITEM", "minecraft:furnace", 3, 8, "minecraft:emerald", 1, 3, 10);
        addEntry(entries, "CRAFT_ITEM", "minecraft:iron_pickaxe", 1, 5, "minecraft:emerald", 2, 6, 12);
        addEntry(entries, "CRAFT_ITEM", "minecraft:iron_sword", 1, 3, "minecraft:emerald", 2, 6, 10);
        addEntry(entries, "CRAFT_ITEM", "minecraft:torch", 16, 64, "minecraft:emerald", 1, 4, 15);
        addEntry(entries, "CRAFT_ITEM", "minecraft:bread", 5, 20, "minecraft:emerald", 1, 3, 12);
        addEntry(entries, "CRAFT_ITEM", "minecraft:stick", 8, 32, "minecraft:emerald", 1, 2, 8);
        addEntry(entries, "CRAFT_ITEM", "minecraft:iron_chestplate", 1, 3, "minecraft:emerald", 3, 8, 8);

        // COLLECT_ITEM
        addEntry(entries, "COLLECT_ITEM", "minecraft:coal", 5, 32, "minecraft:emerald", 1, 5, 15);
        addEntry(entries, "COLLECT_ITEM", "minecraft:iron_ingot", 3, 16, "minecraft:emerald", 1, 5, 10);
        addEntry(entries, "COLLECT_ITEM", "minecraft:wheat", 8, 32, "minecraft:emerald", 1, 4, 12);
        addEntry(entries, "COLLECT_ITEM", "minecraft:apple", 3, 12, "minecraft:emerald", 1, 4, 8);
        addEntry(entries, "COLLECT_ITEM", "minecraft:rotten_flesh", 5, 20, "minecraft:emerald", 1, 3, 10);
        addEntry(entries, "COLLECT_ITEM", "minecraft:bone", 5, 20, "minecraft:emerald", 1, 3, 10);
        addEntry(entries, "COLLECT_ITEM", "minecraft:gunpowder", 3, 16, "minecraft:emerald", 1, 5, 10);
        addEntry(entries, "COLLECT_ITEM", "minecraft:ender_pearl", 1, 5, "minecraft:emerald", 3, 8, 8);

        // FIND_STRUCTURE
        addEntry(entries, "FIND_STRUCTURE", "minecraft:village_plains", 1, 1, "minecraft:emerald", 5, 10, 10);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:village_desert", 1, 1, "minecraft:emerald", 5, 10, 8);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:village_savanna", 1, 1, "minecraft:emerald", 5, 10, 8);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:village_taiga", 1, 1, "minecraft:emerald", 5, 10, 8);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:village_snowy", 1, 1, "minecraft:emerald", 5, 10, 8);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:desert_pyramid", 1, 1, "minecraft:diamond", 2, 5, 8);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:jungle_pyramid", 1, 1, "minecraft:diamond", 2, 5, 8);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:pillager_outpost", 1, 1, "minecraft:emerald", 8, 15, 8);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:mineshaft", 1, 1, "minecraft:iron_ingot", 5, 10, 8);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:stronghold", 1, 1, "minecraft:diamond", 5, 10, 5);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:ruined_portal", 1, 1, "minecraft:gold_ingot", 3, 8, 8);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:ocean_ruin_cold", 1, 1, "minecraft:iron_ingot", 3, 6, 6);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:shipwreck", 1, 1, "minecraft:iron_ingot", 3, 6, 6);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:buried_treasure", 1, 1, "minecraft:diamond", 3, 8, 6);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:swamp_hut", 1, 1, "minecraft:emerald", 5, 10, 6);
        addEntry(entries, "FIND_STRUCTURE", "minecraft:igloo", 1, 1, "minecraft:emerald", 3, 6, 5);

        // Gem exchange: 6 gems for 1 diamond
        addGemExchangeEntry(entries);

        return entries;
    }

    // ── 单目标任务（每个 entry 一个 target + 一个 reward）──
    static void addEntry(JsonArray entries, String targetType, String target,
                         int countMin, int countMax, String rewardItem,
                         int rewardCountMin, int rewardCountMax, int weight) {
        addEntryInternal(entries, targetType, target, countMin, countMax,
                rewardItem, rewardCountMin, rewardCountMax, weight,
                nameKey(target), descKey(target));
    }

    static void addEntryInternal(JsonArray entries, String targetType, String target,
                                 int countMin, int countMax, String rewardItem,
                                 int rewardCountMin, int rewardCountMax, int weight,
                                 String name, String description) {
        JsonObject entry = new JsonObject();

        JsonArray targetsArr = new JsonArray();
        JsonObject tObj = new JsonObject();
        tObj.addProperty("type", targetType);
        tObj.addProperty("item", target);
        tObj.addProperty("countMin", countMin);
        tObj.addProperty("countMax", countMax);
        targetsArr.add(tObj);
        entry.add("targets", targetsArr);

        JsonArray rewardsArr = new JsonArray();
        JsonObject rObj = new JsonObject();
        rObj.addProperty("item", rewardItem);
        rObj.addProperty("countMin", rewardCountMin);
        rObj.addProperty("countMax", rewardCountMax);
        rewardsArr.add(rObj);
        entry.add("rewards", rewardsArr);

        entry.addProperty("weight", weight);
        entry.addProperty("name", name);
        entry.addProperty("description", description);
        entries.add(entry);
    }

    // ── 多目标任务（每个 entry 多个 target + 多个 reward）──
    static void addMultiEntry(JsonArray entries, String[][] targets, String[][] rewards,
                              int weight, String name, String description) {
        JsonObject entry = new JsonObject();
        entry.add("targets", buildTargetsArray(targets));
        entry.add("rewards", buildRewardsArray(rewards));
        entry.addProperty("weight", weight);
        entry.addProperty("name", name);
        entry.addProperty("description", description);
        entries.add(entry);
    }

    // ── 宝石兑换钻石 ──
    static void addGemExchangeEntry(JsonArray entries) {
        String[][] targets = {
            {"COLLECT_ITEM", "improved_original:ruby", "1", "1"},
            {"COLLECT_ITEM", "improved_original:sapphire", "1", "1"},
            {"COLLECT_ITEM", "improved_original:topaz", "1", "1"},
            {"COLLECT_ITEM", "improved_original:amethyst", "1", "1"},
            {"COLLECT_ITEM", "improved_original:onyx", "1", "1"},
            {"COLLECT_ITEM", "minecraft:emerald", "1", "1"}
        };
        String[][] rewards = {{"minecraft:diamond", "1", "1"}};
        addMultiEntry(entries, targets, rewards, 8,
                "quest.improved_original.name.gem_exchange",
                "quest.improved_original.desc_text.gem_exchange");
    }

    // ── 翻译键 ──
    static String nameKey(String target) {
        return "quest.improved_original.name." + target.replace(':', '.');
    }

    static String descKey(String target) {
        return "quest.improved_original.desc_text." + target.replace(':', '.');
    }

    // ── JSON 数组构建 ──
    private static JsonArray buildTargetsArray(String[][] targets) {
        JsonArray arr = new JsonArray();
        for (String[] t : targets) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", t[0]);
            obj.addProperty("item", t[1]);
            obj.addProperty("countMin", Integer.parseInt(t[2]));
            obj.addProperty("countMax", Integer.parseInt(t[3]));
            arr.add(obj);
        }
        return arr;
    }

    private static JsonArray buildRewardsArray(String[][] rewards) {
        JsonArray arr = new JsonArray();
        for (String[] r : rewards) {
            JsonObject obj = new JsonObject();
            obj.addProperty("item", r[0]);
            obj.addProperty("countMin", Integer.parseInt(r[1]));
            obj.addProperty("countMax", Integer.parseInt(r[2]));
            arr.add(obj);
        }
        return arr;
    }
}
