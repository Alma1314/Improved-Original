// 数据生成-英文翻译：物品/方块名、任务系统所有文本
package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.block.ModBlocks;
import com.alma.improved_original.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModEnUsLangProvider extends LanguageProvider {
    public ModEnUsLangProvider(PackOutput output) {
        super(output, ImprovedOriginal.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(ModItems.RUBY.get(), "Ruby");
        add(ModItems.SAPPHIRE.get(), "Sapphire");
        add(ModItems.TOPAZ.get(), "Topaz");
        add(ModItems.AMETHYST.get(), "Amethyst");
        add(ModItems.ONYX.get(), "Onyx");

        add(ModBlocks.RUBY_BLOCK.get(), "Ruby Block");
        add(ModBlocks.SAPPHIRE_BLOCK.get(), "Sapphire Block");
        add(ModBlocks.TOPAZ_BLOCK.get(), "Topaz Block");
        add(ModBlocks.AMETHYST_BLOCK.get(), "Amethyst Block");
        add(ModBlocks.ONYX_BLOCK.get(), "Onyx Block");

        add(ModBlocks.RUBY_ORE.get(), "Ruby Ore");
        add(ModBlocks.SAPPHIRE_ORE.get(), "Sapphire Ore");
        add(ModBlocks.TOPAZ_ORE.get(), "Topaz Ore");
        add(ModBlocks.AMETHYST_ORE.get(), "Amethyst Ore");
        add(ModBlocks.ONYX_ORE.get(), "Onyx Ore");

        add(ModBlocks.DEEPSLATE_RUBY_ORE.get(), "Deepslate Ruby Ore");
        add(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(), "Deepslate Sapphire Ore");
        add(ModBlocks.DEEPSLATE_TOPAZ_ORE.get(), "Deepslate Topaz Ore");
        add(ModBlocks.DEEPSLATE_AMETHYST_ORE.get(), "Deepslate Amethyst Ore");
        add(ModBlocks.DEEPSLATE_ONYX_ORE.get(), "Deepslate Onyx Ore");

        add("itemGroup.gemstones_tab", "Gemstones");

        // Quest system
        add("key.categories.improved_original", "Improved Original");
        add("key.improved_original.open_quest", "Open Quest Screen");
        add("screen.improved_original.quest", "Daily Quests");
        add("quest.improved_original.empty_slot", "No quest assigned");
        add("quest.improved_original.reward", "Reward: %d %s");
        add("quest.improved_original.lock_button", "Lock");
        add("quest.improved_original.locked", "Locked");
        add("quest.improved_original.done", "Done");
        add("quest.improved_original.lock_cost_hint", "(%d E)");

        add("quest.improved_original.desc.break", "Break %s x%d");
        add("quest.improved_original.desc.craft", "Craft %s x%d");
        add("quest.improved_original.desc.kill", "Kill %s x%d");
        add("quest.improved_original.desc.collect", "Collect %s x%d");
        add("quest.improved_original.desc.find", "Explore %s");

        add("quest.improved_original.lock.success", "Quest in slot %d has been locked!");
        add("quest.improved_original.lock.no_emeralds", "You need %d emeralds to lock a quest!");
        add("quest.improved_original.lock.already_locked", "This quest is already locked.");
        add("quest.improved_original.lock.no_quest", "No quest in this slot to lock.");
        add("quest.improved_original.lock.completed", "Cannot lock a completed quest.");
        add("quest.improved_original.unlock.success", "Quest in slot %d has been unlocked.");
        add("quest.improved_original.unlock.not_locked", "This quest is not locked.");
        add("quest.improved_original.complete_success", "Completed quest: %s! Received %d emeralds.");
        add("quest.improved_original.toast.title", "Quest Complete!");
        add("quest.improved_original.toast.desc", "%s - %s");
        add("quest.improved_original.refresh_notify", "Daily quests have been refreshed!");
        add("quest.improved_original.refresh_button", "Refresh (%d E)");
        add("quest.improved_original.refresh.success", "Quests refreshed! Cost: %d emeralds.");
        add("quest.improved_original.refresh.no_emeralds", "You need %d emeralds to refresh quests!");
        add("quest.improved_original.countdown", "Refresh in: %02d:%02d");

        // Quest names and descriptions
        add("quest.improved_original.name.minecraft.stone", "Stone Miner");
        add("quest.improved_original.desc_text.minecraft.stone", "Break stone blocks to earn emerald rewards");
        add("quest.improved_original.name.minecraft.oak_log", "Lumberjack");
        add("quest.improved_original.desc_text.minecraft.oak_log", "Chop down oak logs");
        add("quest.improved_original.name.minecraft.coal_ore", "Coal Miner");
        add("quest.improved_original.desc_text.minecraft.coal_ore", "Mine coal ore deep underground");
        add("quest.improved_original.name.minecraft.iron_ore", "Iron Miner");
        add("quest.improved_original.desc_text.minecraft.iron_ore", "Extract iron ore from the earth");
        add("quest.improved_original.name.minecraft.dirt", "Dirt Digger");
        add("quest.improved_original.desc_text.minecraft.dirt", "Dig up dirt blocks");
        add("quest.improved_original.name.minecraft.deepslate", "Deepslate Excavator");
        add("quest.improved_original.desc_text.minecraft.deepslate", "Excavate deepslate deep underground");
        add("quest.improved_original.name.minecraft.sand", "Sand Collector");
        add("quest.improved_original.desc_text.minecraft.sand", "Gather sand from beaches and deserts");
        add("quest.improved_original.name.minecraft.gravel", "Gravel Gatherer");
        add("quest.improved_original.desc_text.minecraft.gravel", "Collect gravel from riverbeds");
        add("quest.improved_original.name.minecraft.netherrack", "Nether Miner");
        add("quest.improved_original.desc_text.minecraft.netherrack", "Mine netherrack in the Nether");
        add("quest.improved_original.name.minecraft.zombie", "Zombie Slayer");
        add("quest.improved_original.desc_text.minecraft.zombie", "Defeat zombies to protect the village");
        add("quest.improved_original.name.minecraft.skeleton", "Skeleton Hunter");
        add("quest.improved_original.desc_text.minecraft.skeleton", "Take down skeletons from a distance");
        add("quest.improved_original.name.minecraft.spider", "Spider Exterminator");
        add("quest.improved_original.desc_text.minecraft.spider", "Eliminate spiders and their webs");
        add("quest.improved_original.name.minecraft.creeper", "Creeper Buster");
        add("quest.improved_original.desc_text.minecraft.creeper", "Neutralize creepers before they explode");
        add("quest.improved_original.name.minecraft.enderman", "Enderman Vanquisher");
        add("quest.improved_original.desc_text.minecraft.enderman", "Challenge the tall, dark endermen");
        add("quest.improved_original.name.minecraft.witch", "Witch Neutralizer");
        add("quest.improved_original.desc_text.minecraft.witch", "Stop witches and their potions");
        add("quest.improved_original.name.minecraft.drowned", "Drowned Hunter");
        add("quest.improved_original.desc_text.minecraft.drowned", "Hunt drowned in the depths");
        add("quest.improved_original.name.minecraft.husk", "Husk Eliminator");
        add("quest.improved_original.desc_text.minecraft.husk", "Take down husks in the desert");
        add("quest.improved_original.name.minecraft.crafting_table", "Crafting Table Crafter");
        add("quest.improved_original.desc_text.minecraft.crafting_table", "Craft basic crafting tables");
        add("quest.improved_original.name.minecraft.furnace", "Furnace Maker");
        add("quest.improved_original.desc_text.minecraft.furnace", "Smelt your way to a furnace");
        add("quest.improved_original.name.minecraft.iron_pickaxe", "Tool Smith");
        add("quest.improved_original.desc_text.minecraft.iron_pickaxe", "Forge an iron pickaxe");
        add("quest.improved_original.name.minecraft.iron_sword", "Weaponsmith");
        add("quest.improved_original.desc_text.minecraft.iron_sword", "Craft an iron sword");
        add("quest.improved_original.name.minecraft.torch", "Torchbearer");
        add("quest.improved_original.desc_text.minecraft.torch", "Light up the darkness with torches");
        add("quest.improved_original.name.minecraft.bread", "Baker");
        add("quest.improved_original.desc_text.minecraft.bread", "Bake bread from wheat");
        add("quest.improved_original.name.minecraft.stick", "Woodworker");
        add("quest.improved_original.desc_text.minecraft.stick", "Shape sticks from planks");
        add("quest.improved_original.name.minecraft.iron_chestplate", "Armorer");
        add("quest.improved_original.desc_text.minecraft.iron_chestplate", "Forge an iron chestplate");
        add("quest.improved_original.name.minecraft.coal", "Coal Collector");
        add("quest.improved_original.desc_text.minecraft.coal", "Gather coal from mining or loot");
        add("quest.improved_original.name.minecraft.iron_ingot", "Iron Hoarder");
        add("quest.improved_original.desc_text.minecraft.iron_ingot", "Collect iron ingots from smelting");
        add("quest.improved_original.name.minecraft.wheat", "Wheat Farmer");
        add("quest.improved_original.desc_text.minecraft.wheat", "Harvest wheat from your farm");
        add("quest.improved_original.name.minecraft.apple", "Apple Picker");
        add("quest.improved_original.desc_text.minecraft.apple", "Collect apples from oak trees");
        add("quest.improved_original.name.minecraft.rotten_flesh", "Flesh Collector");
        add("quest.improved_original.desc_text.minecraft.rotten_flesh", "Gather rotten flesh from undead");
        add("quest.improved_original.name.minecraft.bone", "Bone Collector");
        add("quest.improved_original.desc_text.minecraft.bone", "Collect bones from skeletons");
        add("quest.improved_original.name.minecraft.gunpowder", "Gunpowder Gatherer");
        add("quest.improved_original.desc_text.minecraft.gunpowder", "Gather gunpowder from creepers");
        add("quest.improved_original.name.minecraft.ender_pearl", "Pearl Seeker");
        add("quest.improved_original.desc_text.minecraft.ender_pearl", "Collect ender pearls from endermen");
        add("quest.improved_original.name.minecraft.village_plains", "Village Explorer");
        add("quest.improved_original.desc_text.minecraft.village_plains", "Find a plains village");
        add("quest.improved_original.name.minecraft.village_desert", "Desert Explorer");
        add("quest.improved_original.desc_text.minecraft.village_desert", "Discover a desert village");
        add("quest.improved_original.name.minecraft.village_savanna", "Savanna Explorer");
        add("quest.improved_original.desc_text.minecraft.village_savanna", "Locate a savanna village");
        add("quest.improved_original.name.minecraft.village_taiga", "Taiga Explorer");
        add("quest.improved_original.desc_text.minecraft.village_taiga", "Find a taiga village");
        add("quest.improved_original.name.minecraft.village_snowy", "Snowy Explorer");
        add("quest.improved_original.desc_text.minecraft.village_snowy", "Brave the cold to find a snowy village");
        add("quest.improved_original.name.minecraft.desert_pyramid", "Pyramid Raider");
        add("quest.improved_original.desc_text.minecraft.desert_pyramid", "Discover a desert pyramid");
        add("quest.improved_original.name.minecraft.jungle_pyramid", "Jungle Explorer");
        add("quest.improved_original.desc_text.minecraft.jungle_pyramid", "Find a jungle pyramid hidden in the foliage");
        add("quest.improved_original.name.minecraft.pillager_outpost", "Outpost Scout");
        add("quest.improved_original.desc_text.minecraft.pillager_outpost", "Scout a pillager outpost");
        add("quest.improved_original.name.minecraft.mineshaft", "Mineshaft Explorer");
        add("quest.improved_original.desc_text.minecraft.mineshaft", "Discover an abandoned mineshaft");
        add("quest.improved_original.name.minecraft.stronghold", "Stronghold Seeker");
        add("quest.improved_original.desc_text.minecraft.stronghold", "Find the hidden stronghold");
        add("quest.improved_original.name.minecraft.ruined_portal", "Portal Finder");
        add("quest.improved_original.desc_text.minecraft.ruined_portal", "Locate a ruined nether portal");
        add("quest.improved_original.name.minecraft.ocean_ruin_cold", "Ocean Explorer");
        add("quest.improved_original.desc_text.minecraft.ocean_ruin_cold", "Explore a cold ocean ruin");
        add("quest.improved_original.name.minecraft.shipwreck", "Shipwreck Salvager");
        add("quest.improved_original.desc_text.minecraft.shipwreck", "Find a shipwreck on the ocean floor");
        add("quest.improved_original.name.minecraft.buried_treasure", "Treasure Hunter");
        add("quest.improved_original.desc_text.minecraft.buried_treasure", "Find buried treasure");
        add("quest.improved_original.name.minecraft.swamp_hut", "Swamp Explorer");
        add("quest.improved_original.desc_text.minecraft.swamp_hut", "Discover a swamp hut");
        add("quest.improved_original.name.minecraft.igloo", "Igloo Adventurer");
        add("quest.improved_original.desc_text.minecraft.igloo", "Find an igloo in the icy tundra");

        // JEI integration
        add("quest.improved_original.jei.category", "Daily Quests");
        add("quest.improved_original.jei.arrow", "→");
        add("quest.improved_original.jei.quest_target_info", "This item is a quest target - complete the quest to earn rewards!");
    }
}
