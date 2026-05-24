// JSON quest pool config: load/save/generate default quest entries
package com.alma.improved_original.datagen;

import com.alma.improved_original.quest.QuestType;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class QuestPoolConfig {

    private static List<PoolEntry> cachedPool = null;

    public static void reloadCache() {
        cachedPool = null;
    }

    public record TargetEntry(ResourceLocation item, int countMin, int countMax) {}
    public record RewardEntry(ResourceLocation item, int countMin, int countMax) {}

    public record PoolEntry(
            QuestType type,
            List<TargetEntry> targets,
            List<RewardEntry> rewards,
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

                            // Targets — support both new "targets" array and legacy "target" field
                            List<TargetEntry> targets;
                            if (entry.has("targets")) {
                                targets = parseTargetEntries(entry.getAsJsonArray("targets"));
                            } else if (entry.has("target")) {
                                ResourceLocation legacyTarget = ResourceLocation.parse(entry.get("target").getAsString());
                                int cMin = entry.has("countMin") ? entry.get("countMin").getAsInt() : 1;
                                int cMax = entry.has("countMax") ? entry.get("countMax").getAsInt() : 1;
                                targets = List.of(new TargetEntry(legacyTarget, cMin, cMax));
                            } else {
                                continue; // skip invalid entry
                            }

                            // Rewards — support both new "rewards" array and legacy "reward" object
                            List<RewardEntry> rewards;
                            if (entry.has("rewards")) {
                                rewards = parseRewardEntries(entry.getAsJsonArray("rewards"));
                            } else if (entry.has("reward")) {
                                JsonObject reward = entry.getAsJsonObject("reward");
                                ResourceLocation rItem = ResourceLocation.parse(reward.get("item").getAsString());
                                int rMin = reward.has("countMin") ? reward.get("countMin").getAsInt() : 1;
                                int rMax = reward.has("countMax") ? reward.get("countMax").getAsInt() : 1;
                                rewards = List.of(new RewardEntry(rItem, rMin, rMax));
                            } else {
                                continue;
                            }

                            int weight = entry.has("weight") ? entry.get("weight").getAsInt() : 10;
                            String name = entry.has("name") ? entry.get("name").getAsString() : "";
                            String description = entry.has("description") ? entry.get("description").getAsString() : "";
                            allEntries.add(new PoolEntry(type, targets, rewards, weight, name, description));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return allEntries;
    }

    private static List<TargetEntry> parseTargetEntries(JsonArray arr) {
        List<TargetEntry> result = new ArrayList<>();
        for (JsonElement e : arr) {
            JsonObject o = e.getAsJsonObject();
            ResourceLocation item = ResourceLocation.parse(o.get("item").getAsString());
            int cMin = o.has("countMin") ? o.get("countMin").getAsInt() : 1;
            int cMax = o.has("countMax") ? o.get("countMax").getAsInt() : 1;
            result.add(new TargetEntry(item, cMin, cMax));
        }
        return result;
    }

    private static List<RewardEntry> parseRewardEntries(JsonArray arr) {
        List<RewardEntry> result = new ArrayList<>();
        for (JsonElement e : arr) {
            JsonObject o = e.getAsJsonObject();
            ResourceLocation item = ResourceLocation.parse(o.get("item").getAsString());
            int cMin = o.has("countMin") ? o.get("countMin").getAsInt() : 1;
            int cMax = o.has("countMax") ? o.get("countMax").getAsInt() : 1;
            result.add(new RewardEntry(item, cMin, cMax));
        }
        return result;
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
        addEntry(entries, "COLLECT_ITEM", "minecraft:dirt", 1, 1, "minecraft:grass_block", 1, 1, 5);
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

        // Multi-target quest: exchange 6 gems for 1 diamond
        addGemExchangeEntry(entries);

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

    private static void addGemExchangeEntry(JsonArray entries) {
        String type = "COLLECT_ITEM";
        String[][] targets = {
            {"improved_original:ruby", "1", "1"},
            {"improved_original:sapphire", "1", "1"},
            {"improved_original:topaz", "1", "1"},
            {"improved_original:amethyst", "1", "1"},
            {"improved_original:onyx", "1", "1"},
            {"minecraft:emerald", "1", "1"}
        };
        String[][] rewards = {
            {"minecraft:diamond", "1", "1"}
        };
        String name = "quest.improved_original.name.gem_exchange";
        String desc = "quest.improved_original.desc_text.gem_exchange";
        addMultiEntry(entries, type, targets, rewards, 8, name, desc);
    }

    private static void addMultiEntry(JsonArray entries, String type,
                                       String[][] targets, String[][] rewards,
                                       int weight, String name, String description) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", type);

        JsonArray targetsArr = new JsonArray();
        for (String[] t : targets) {
            JsonObject tObj = new JsonObject();
            tObj.addProperty("item", t[0]);
            tObj.addProperty("countMin", Integer.parseInt(t[1]));
            tObj.addProperty("countMax", Integer.parseInt(t[2]));
            targetsArr.add(tObj);
        }
        entry.add("targets", targetsArr);

        JsonArray rewardsArr = new JsonArray();
        for (String[] r : rewards) {
            JsonObject rObj = new JsonObject();
            rObj.addProperty("item", r[0]);
            rObj.addProperty("countMin", Integer.parseInt(r[1]));
            rObj.addProperty("countMax", Integer.parseInt(r[2]));
            rewardsArr.add(rObj);
        }
        entry.add("rewards", rewardsArr);

        entry.addProperty("weight", weight);
        entry.addProperty("name", name);
        entry.addProperty("description", description);
        entries.add(entry);
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

        JsonArray targetsArr = new JsonArray();
        JsonObject tObj = new JsonObject();
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
}
