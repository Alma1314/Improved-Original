package com.alma.improved_original.datagen;

import com.alma.improved_original.block.ModBlocks;
import com.alma.improved_original.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;


import java.util.Set;

public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    protected ModBlockLootTablesProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.AMETHYST_BLOCK.get());
        add(ModBlocks.AMETHYST_ORE.get(), block -> createOreDrop(ModBlocks.AMETHYST_ORE.get(), ModItems.AMETHYST.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
