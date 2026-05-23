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
        add("screen.improved_original.quest", "Daily Quests");
        add("quest.improved_original.empty_slot", "No quest assigned");
        add("quest.improved_original.reward", "Reward: %d Emeralds");
        add("quest.improved_original.lock_button", "Lock");
        add("quest.improved_original.locked", "Locked");
        add("quest.improved_original.done", "Done");
        add("quest.improved_original.lock_cost_hint", "(%d E)");

        add("quest.improved_original.desc.break", "Break %s x%d");
        add("quest.improved_original.desc.craft", "Craft %s x%d");
        add("quest.improved_original.desc.kill", "Kill %s x%d");
        add("quest.improved_original.desc.collect", "Collect %s x%d");

        add("quest.improved_original.lock.success", "Quest in slot %d has been locked!");
        add("quest.improved_original.lock.no_emeralds", "You need %d emeralds to lock a quest!");
        add("quest.improved_original.lock.already_locked", "This quest is already locked.");
        add("quest.improved_original.lock.no_quest", "No quest in this slot to lock.");
        add("quest.improved_original.lock.completed", "Cannot lock a completed quest.");
        add("quest.improved_original.unlock.success", "Quest in slot %d has been unlocked.");
        add("quest.improved_original.unlock.not_locked", "This quest is not locked.");
        add("quest.improved_original.complete_success", "Completed quest: %s! Received %d emeralds.");
        add("quest.improved_original.toast.title", "Quest Complete!");
        add("quest.improved_original.toast.desc", "%s - +%d Emeralds");
        add("quest.improved_original.refresh_notify", "Daily quests have been refreshed!");
        add("quest.improved_original.refresh_button", "Refresh (%d E)");
        add("quest.improved_original.refresh.success", "Quests refreshed! Cost: %d emeralds.");
        add("quest.improved_original.refresh.no_emeralds", "You need %d emeralds to refresh quests!");
        add("quest.improved_original.countdown", "Refresh in: %02d:%02d");
    }
}
