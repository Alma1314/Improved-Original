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
        add(ModItems.AMETHYST.get(), "amethyst");
        add(ModBlocks.AMETHYST_BLOCK.get(), "amethyst block");

        add("itemGroup.gemstones_tab", "gemstones_tab");

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
    }
}
