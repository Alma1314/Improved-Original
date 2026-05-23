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
        addEntry(entries, "BREAK_BLOCK", "minecraft:stone", 10, 64, "minecraft:emerald", 1, 10, 20);
        addEntry(entries, "BREAK_BLOCK", "minecraft:oak_log", 10, 48, "minecraft:emerald", 1, 8, 15);
        addEntry(entries, "BREAK_BLOCK", "minecraft:coal_ore", 8, 32, "minecraft:emerald", 1, 5, 10);
        addEntry(entries, "BREAK_BLOCK", "minecraft:iron_ore", 5, 24, "minecraft:emerald", 1, 7, 8);
        addEntry(entries, "BREAK_BLOCK", "minecraft:dirt", 20, 80, "minecraft:emerald", 1, 8, 20);
        addEntry(entries, "BREAK_BLOCK", "minecraft:deepslate", 16, 64, "minecraft:emerald", 1, 8, 18);
        addEntry(entries, "BREAK_BLOCK", "minecraft:sand", 12, 48, "minecraft:emerald", 1, 6, 15);
        addEntry(entries, "BREAK_BLOCK", "minecraft:gravel", 10, 32, "minecraft:emerald", 1, 3, 12);
        addEntry(entries, "BREAK_BLOCK", "minecraft:netherrack", 20, 80, "minecraft:emerald", 1, 5, 15);

        // KILL_ENTITY
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

        return entries;
    }

    private static void addEntry(JsonArray entries, String type, String target,
                                  int countMin, int countMax, String rewardItem,
                                  int rewardCountMin, int rewardCountMax, int weight) {
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
        entries.add(entry);
    }
}
