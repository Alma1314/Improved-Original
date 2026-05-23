// JSON任务池配置：加载/保存/生成默认任务定义文件
package com.alma.improved_original.datagen;

import com.alma.improved_original.quest.QuestType;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class QuestPoolConfig {

    public record PoolEntry(
            QuestType type,
            ResourceLocation target,
            int countMin, int countMax,
            ResourceLocation rewardItem, int rewardCountMin, int rewardCountMax,
            int weight,
            String name,
            String description
    ) {}

    public static List<PoolEntry> loadFromConfig(Path configDir) {
        Path questsDir = configDir.resolve("quests");
        List<PoolEntry> allEntries = new ArrayList<>();

        try {
            Files.createDirectories(questsDir);
        } catch (IOException e) {
            e.printStackTrace();
            return allEntries;
        }

        // scan for JSON files
        File[] files = questsDir.toFile().listFiles(f -> f.getName().endsWith(".json"));
        if (files == null || files.length == 0) {
            generateDefaults(questsDir);
            files = questsDir.toFile().listFiles(f -> f.getName().endsWith(".json"));
        }

        if (files != null) {
            Gson gson = new Gson();
            for (File file : files) {
                try (Reader reader = new FileReader(file)) {
                    JsonObject obj = gson.fromJson(reader, JsonObject.class);
                    if (obj.has("entries")) {
                        for (JsonElement elem : obj.getAsJsonArray("entries")) {
                            JsonObject entry = elem.getAsJsonObject();
                            QuestType type = QuestType.valueOf(entry.get("type").getAsString().toUpperCase());
                            ResourceLocation target = ResourceLocation.parse(entry.get("target").getAsString());
                            int countMin = entry.has("countMin") ? entry.get("countMin").getAsInt() : 1;
                            int countMax = entry.has("countMax") ? entry.get("countMax").getAsInt() : 1;
                            JsonObject reward = entry.getAsJsonObject("reward");
                            ResourceLocation rewardItem = ResourceLocation.parse(reward.get("item").getAsString());
                            int rewardCountMin = reward.has("countMin") ? reward.get("countMin").getAsInt() : 1;
                            int rewardCountMax = reward.has("countMax") ? reward.get("countMax").getAsInt() : 1;
                            int weight = entry.has("weight") ? entry.get("weight").getAsInt() : 10;
                            String name = entry.has("name") ? entry.get("name").getAsString() : "";
                            String description = entry.has("description") ? entry.get("description").getAsString() : "";
                            allEntries.add(new PoolEntry(type, target, countMin, countMax,
                                    rewardItem, rewardCountMin, rewardCountMax, weight, name, description));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return allEntries;
    }

    private static void generateDefaults(Path questsDir) {
        Path defaultFile = questsDir.resolve("default_pool.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject root = new JsonObject();
        JsonArray entries = new JsonArray();

        addEntry(entries, "BREAK_BLOCK", "minecraft:stone", 10, 64, "minecraft:emerald", 1, 10, 20);
        addEntry(entries, "BREAK_BLOCK", "minecraft:oak_log", 10, 48, "minecraft:emerald", 1, 8, 15);
        addEntry(entries, "BREAK_BLOCK", "minecraft:coal_ore", 8, 32, "minecraft:emerald", 1, 5, 10);
        addEntry(entries, "BREAK_BLOCK", "minecraft:iron_ore", 5, 24, "minecraft:emerald", 1, 7, 8);
        addEntry(entries, "BREAK_BLOCK", "minecraft:dirt", 20, 80, "minecraft:emerald", 1, 8, 20);
        addEntry(entries, "BREAK_BLOCK", "minecraft:deepslate", 16, 64, "minecraft:emerald", 1, 8, 18);
        addEntry(entries, "BREAK_BLOCK", "minecraft:sand", 12, 48, "minecraft:emerald", 1, 6, 15);
        addEntry(entries, "BREAK_BLOCK", "minecraft:gravel", 10, 32, "minecraft:emerald", 1, 3, 12);
        addEntry(entries, "BREAK_BLOCK", "minecraft:netherrack", 20, 80, "minecraft:emerald", 1, 5, 15);

        addEntry(entries, "KILL_ENTITY", "minecraft:zombie", 5, 20, "minecraft:emerald", 1, 5, 20);
        addEntry(entries, "KILL_ENTITY", "minecraft:skeleton", 5, 20, "minecraft:emerald", 1, 5, 20);
        addEntry(entries, "KILL_ENTITY", "minecraft:spider", 3, 15, "minecraft:emerald", 1, 5, 15);
        addEntry(entries, "KILL_ENTITY", "minecraft:creeper", 3, 15, "minecraft:emerald", 1, 6, 15);
        addEntry(entries, "KILL_ENTITY", "minecraft:enderman", 3, 10, "minecraft:emerald", 2, 8, 8);
        addEntry(entries, "KILL_ENTITY", "minecraft:witch", 3, 10, "minecraft:emerald", 2, 8, 8);
        addEntry(entries, "KILL_ENTITY", "minecraft:drowned", 3, 15, "minecraft:emerald", 1, 4, 12);
        addEntry(entries, "KILL_ENTITY", "minecraft:husk", 3, 15, "minecraft:emerald", 1, 4, 12);

        addEntry(entries, "CRAFT_ITEM", "minecraft:crafting_table", 3, 10, "minecraft:emerald", 1, 3, 10);
        addEntry(entries, "CRAFT_ITEM", "minecraft:furnace", 3, 8, "minecraft:emerald", 1, 3, 10);
        addEntry(entries, "CRAFT_ITEM", "minecraft:iron_pickaxe", 1, 5, "minecraft:emerald", 2, 6, 12);
        addEntry(entries, "CRAFT_ITEM", "minecraft:iron_sword", 1, 3, "minecraft:emerald", 2, 6, 10);
        addEntry(entries, "CRAFT_ITEM", "minecraft:torch", 16, 64, "minecraft:emerald", 1, 4, 15);
        addEntry(entries, "CRAFT_ITEM", "minecraft:bread", 5, 20, "minecraft:emerald", 1, 3, 12);
        addEntry(entries, "CRAFT_ITEM", "minecraft:stick", 8, 32, "minecraft:emerald", 1, 2, 8);
        addEntry(entries, "CRAFT_ITEM", "minecraft:iron_chestplate", 1, 3, "minecraft:emerald", 3, 8, 8);

        addEntry(entries, "COLLECT_ITEM", "minecraft:coal", 5, 32, "minecraft:emerald", 1, 5, 15);
        addEntry(entries, "COLLECT_ITEM", "minecraft:iron_ingot", 3, 16, "minecraft:emerald", 1, 5, 10);
        addEntry(entries, "COLLECT_ITEM", "minecraft:wheat", 8, 32, "minecraft:emerald", 1, 4, 12);
        addEntry(entries, "COLLECT_ITEM", "minecraft:apple", 3, 12, "minecraft:emerald", 1, 4, 8);
        addEntry(entries, "COLLECT_ITEM", "minecraft:rotten_flesh", 5, 20, "minecraft:emerald", 1, 3, 10);
        addEntry(entries, "COLLECT_ITEM", "minecraft:bone", 5, 20, "minecraft:emerald", 1, 3, 10);
        addEntry(entries, "COLLECT_ITEM", "minecraft:gunpowder", 3, 16, "minecraft:emerald", 1, 5, 10);
        addEntry(entries, "COLLECT_ITEM", "minecraft:ender_pearl", 1, 5, "minecraft:emerald", 3, 8, 8);

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

        int totalWeight = 0;
        for (JsonElement e : entries) {
            totalWeight += e.getAsJsonObject().get("weight").getAsInt();
        }
        root.addProperty("totalWeight", totalWeight);
        root.add("entries", entries);

        try (Writer writer = new FileWriter(defaultFile.toFile())) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String nameKey(String type, String target) {
        return "quest.improved_original.name." + target.replace(':', '.');
    }

    private static String descKey(String type, String target) {
        return "quest.improved_original.desc_text." + target.replace(':', '.');
    }

    private static void addEntry(JsonArray entries, String type, String target,
                                  int countMin, int countMax, String rewardItem,
                                  int rewardCountMin, int rewardCountMax, int weight) {
        addEntryInternal(entries, type, target, countMin, countMax, rewardItem, rewardCountMin, rewardCountMax, weight,
                nameKey(type, target), descKey(type, target));
    }

    private static void addEntryInternal(JsonArray entries, String type, String target,
                                  int countMin, int countMax, String rewardItem,
                                  int rewardCountMin, int rewardCountMax, int weight,
                                  String name, String description) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", type);
        entry.addProperty("target", target);
        entry.addProperty("countMin", countMin);
        entry.addProperty("countMax", countMax);
        JsonObject reward = new JsonObject();
        reward.addProperty("item", rewardItem);
        reward.addProperty("countMin", rewardCountMin);
        reward.addProperty("countMax", rewardCountMax);
        entry.add("reward", reward);
        entry.addProperty("weight", weight);
        entry.addProperty("name", name);
        if (!description.isEmpty()) {
            entry.addProperty("description", description);
        }
        entries.add(entry);
    }
}
