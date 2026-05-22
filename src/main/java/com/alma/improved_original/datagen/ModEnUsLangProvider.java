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
    }
}
