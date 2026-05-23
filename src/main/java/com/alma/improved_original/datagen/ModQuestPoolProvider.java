// 数据生成-任务池：生成默认任务池JSON到 generated/resources/data/improved_original/quest_pool/
package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModQuestPoolProvider implements DataProvider {

    private final PackOutput packOutput;

    public ModQuestPoolProvider(PackOutput packOutput) {
        this.packOutput = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        JsonObject root = new JsonObject();
        JsonArray entries = buildDefaultEntries();
        int totalWeight = 0;
        for (JsonElement e : entries) {
            totalWeight += e.getAsJsonObject().get("weight").getAsInt();
        }
        root.addProperty("totalWeight", totalWeight);
        root.add("entries", entries);

        Path outPath = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(ImprovedOriginal.MOD_ID)
                .resolve("quest_pool")
                .resolve("default.json");

        return DataProvider.saveStable(cachedOutput, new GsonBuilder().setPrettyPrinting().create().toJsonTree(root), outPath);
    }

    @Override
    public String getName() {
        return "Quest Pool: " + ImprovedOriginal.MOD_ID;
    }

    private static JsonArray buildDefaultEntries() {
        JsonArray entries = new JsonArray();

        // BREAK_BLOCK
        addEntry(entries, "BREAK_BLOCK", "minecraft:stone", 10, 64, "minecraft:emerald", 1, 10, 20,
                "Stone Miner", "Break stone blocks to earn emerald rewards");
        addEntry(entries, "BREAK_BLOCK", "minecraft:oak_log", 10, 48, "minecraft:emerald", 1, 8, 15,
                "Lumberjack", "Chop down oak logs");
        addEntry(entries, "BREAK_BLOCK", "minecraft:coal_ore", 8, 32, "minecraft:emerald", 1, 5, 10,
                "Coal Miner", "Mine coal ore deep underground");
        addEntry(entries, "BREAK_BLOCK", "minecraft:iron_ore", 5, 24, "minecraft:emerald", 1, 7, 8,
                "Iron Miner", "Extract iron ore from the earth");
        addEntry(entries, "BREAK_BLOCK", "minecraft:dirt", 20, 80, "minecraft:emerald", 1, 8, 20,
                "Dirt Digger", "Dig up dirt blocks");
        addEntry(entries, "BREAK_BLOCK", "minecraft:deepslate", 16, 64, "minecraft:emerald", 1, 8, 18,
                "Deepslate Excavator", "Excavate deepslate deep underground");
        addEntry(entries, "BREAK_BLOCK", "minecraft:sand", 12, 48, "minecraft:emerald", 1, 6, 15,
                "Sand Collector", "Gather sand from beaches and deserts");
        addEntry(entries, "BREAK_BLOCK", "minecraft:gravel", 10, 32, "minecraft:emerald", 1, 3, 12,
                "Gravel Gatherer", "Collect gravel from riverbeds");
        addEntry(entries, "BREAK_BLOCK", "minecraft:netherrack", 20, 80, "minecraft:emerald", 1, 5, 15,
                "Nether Miner", "Mine netherrack in the Nether");

        // KILL_ENTITY
        addEntry(entries, "KILL_ENTITY", "minecraft:zombie", 5, 20, "minecraft:emerald", 1, 5, 20,
                "Zombie Slayer", "Defeat zombies to protect the village");
        addEntry(entries, "KILL_ENTITY", "minecraft:skeleton", 5, 20, "minecraft:emerald", 1, 5, 20,
                "Skeleton Hunter", "Take down skeletons from a distance");
        addEntry(entries, "KILL_ENTITY", "minecraft:spider", 3, 15, "minecraft:emerald", 1, 5, 15,
                "Spider Exterminator", "Eliminate spiders and their webs");
        addEntry(entries, "KILL_ENTITY", "minecraft:creeper", 3, 15, "minecraft:emerald", 1, 6, 15,
                "Creeper Buster", "Neutralize creepers before they explode");
        addEntry(entries, "KILL_ENTITY", "minecraft:enderman", 3, 10, "minecraft:emerald", 2, 8, 8,
                "Enderman Vanquisher", "Challenge the tall, dark endermen");
        addEntry(entries, "KILL_ENTITY", "minecraft:witch", 3, 10, "minecraft:emerald", 2, 8, 8,
                "Witch Neutralizer", "Stop witches and their potions");
        addEntry(entries, "KILL_ENTITY", "minecraft:drowned", 3, 15, "minecraft:emerald", 1, 4, 12,
                "Drowned Hunter", "Hunt drowned in the depths");
        addEntry(entries, "KILL_ENTITY", "minecraft:husk", 3, 15, "minecraft:emerald", 1, 4, 12,
                "Husk Eliminator", "Take down husks in the desert");

        // CRAFT_ITEM
        addEntry(entries, "CRAFT_ITEM", "minecraft:crafting_table", 3, 10, "minecraft:emerald", 1, 3, 10,
                "Crafting Table Crafter", "Craft basic crafting tables");
        addEntry(entries, "CRAFT_ITEM", "minecraft:furnace", 3, 8, "minecraft:emerald", 1, 3, 10,
                "Furnace Maker", "Smelt your way to a furnace");
        addEntry(entries, "CRAFT_ITEM", "minecraft:iron_pickaxe", 1, 5, "minecraft:emerald", 2, 6, 12,
                "Tool Smith", "Forge an iron pickaxe");
        addEntry(entries, "CRAFT_ITEM", "minecraft:iron_sword", 1, 3, "minecraft:emerald", 2, 6, 10,
                "Weaponsmith", "Craft an iron sword");
        addEntry(entries, "CRAFT_ITEM", "minecraft:torch", 16, 64, "minecraft:emerald", 1, 4, 15,
                "Torchbearer", "Light up the darkness with torches");
        addEntry(entries, "CRAFT_ITEM", "minecraft:bread", 5, 20, "minecraft:emerald", 1, 3, 12,
                "Baker", "Bake bread from wheat");
        addEntry(entries, "CRAFT_ITEM", "minecraft:stick", 8, 32, "minecraft:emerald", 1, 2, 8,
                "Woodworker", "Shape sticks from planks");
        addEntry(entries, "CRAFT_ITEM", "minecraft:iron_chestplate", 1, 3, "minecraft:emerald", 3, 8, 8,
                "Armorer", "Forge an iron chestplate");

        // COLLECT_ITEM
        addEntry(entries, "COLLECT_ITEM", "minecraft:coal", 5, 32, "minecraft:emerald", 1, 5, 15,
                "Coal Collector", "Gather coal from mining or loot");
        addEntry(entries, "COLLECT_ITEM", "minecraft:iron_ingot", 3, 16, "minecraft:emerald", 1, 5, 10,
                "Iron Hoarder", "Collect iron ingots from smelting");
        addEntry(entries, "COLLECT_ITEM", "minecraft:wheat", 8, 32, "minecraft:emerald", 1, 4, 12,
                "Wheat Farmer", "Harvest wheat from your farm");
        addEntry(entries, "COLLECT_ITEM", "minecraft:apple", 3, 12, "minecraft:emerald", 1, 4, 8,
                "Apple Picker", "Collect apples from oak trees");
        addEntry(entries, "COLLECT_ITEM", "minecraft:rotten_flesh", 5, 20, "minecraft:emerald", 1, 3, 10,
                "Flesh Collector", "Gather rotten flesh from undead");
        addEntry(entries, "COLLECT_ITEM", "minecraft:bone", 5, 20, "minecraft:emerald", 1, 3, 10,
                "Bone Collector", "Collect bones from skeletons");
        addEntry(entries, "COLLECT_ITEM", "minecraft:gunpowder", 3, 16, "minecraft:emerald", 1, 5, 10,
                "Gunpowder Gatherer", "Gather gunpowder from creepers");
        addEntry(entries, "COLLECT_ITEM", "minecraft:ender_pearl", 1, 5, "minecraft:emerald", 3, 8, 8,
                "Pearl Seeker", "Collect ender pearls from endermen");

        // FIND_STRUCTURE
        addEntry(entries, "FIND_STRUCTURE", "minecraft:village_plains", 1, 1, "minecraft:emerald", 5, 10, 10,
                "Village Explorer", "Find a plains village");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:village_desert", 1, 1, "minecraft:emerald", 5, 10, 8,
                "Desert Explorer", "Discover a desert village");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:village_savanna", 1, 1, "minecraft:emerald", 5, 10, 8,
                "Savanna Explorer", "Locate a savanna village");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:village_taiga", 1, 1, "minecraft:emerald", 5, 10, 8,
                "Taiga Explorer", "Find a taiga village");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:village_snowy", 1, 1, "minecraft:emerald", 5, 10, 8,
                "Snowy Explorer", "Brave the cold to find a snowy village");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:desert_pyramid", 1, 1, "minecraft:diamond", 2, 5, 8,
                "Pyramid Raider", "Discover a desert pyramid");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:jungle_pyramid", 1, 1, "minecraft:diamond", 2, 5, 8,
                "Jungle Explorer", "Find a jungle pyramid hidden in the foliage");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:pillager_outpost", 1, 1, "minecraft:emerald", 8, 15, 8,
                "Outpost Scout", "Scout a pillager outpost");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:mineshaft", 1, 1, "minecraft:iron_ingot", 5, 10, 8,
                "Mineshaft Explorer", "Discover an abandoned mineshaft");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:stronghold", 1, 1, "minecraft:diamond", 5, 10, 5,
                "Stronghold Seeker", "Find the hidden stronghold");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:ruined_portal", 1, 1, "minecraft:gold_ingot", 3, 8, 8,
                "Portal Finder", "Locate a ruined nether portal");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:ocean_ruin_cold", 1, 1, "minecraft:iron_ingot", 3, 6, 6,
                "Ocean Explorer", "Explore a cold ocean ruin");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:shipwreck", 1, 1, "minecraft:iron_ingot", 3, 6, 6,
                "Shipwreck Salvager", "Find a shipwreck on the ocean floor");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:buried_treasure", 1, 1, "minecraft:diamond", 3, 8, 6,
                "Treasure Hunter", "Find buried treasure");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:swamp_hut", 1, 1, "minecraft:emerald", 5, 10, 6,
                "Swamp Explorer", "Discover a swamp hut");
        addEntry(entries, "FIND_STRUCTURE", "minecraft:igloo", 1, 1, "minecraft:emerald", 3, 6, 5,
                "Igloo Adventurer", "Find an igloo in the icy tundra");

        return entries;
    }

    private static void addEntry(JsonArray entries, String type, String target,
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
