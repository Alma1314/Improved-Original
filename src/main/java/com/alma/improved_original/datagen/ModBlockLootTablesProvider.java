// 数据生成-战利品表：宝石块掉落自身，矿石按精准采集/时运逻辑掉落对应宝石
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
    public ModBlockLootTablesProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // All gem blocks drop themselves
        dropSelf(ModBlocks.RUBY_BLOCK.get());
        dropSelf(ModBlocks.SAPPHIRE_BLOCK.get());
        dropSelf(ModBlocks.TOPAZ_BLOCK.get());
        dropSelf(ModBlocks.AMETHYST_BLOCK.get());
        dropSelf(ModBlocks.ONYX_BLOCK.get());

        // All regular ores drop the corresponding gem
        add(ModBlocks.RUBY_ORE.get(), block -> createOreDrop(ModBlocks.RUBY_ORE.get(), ModItems.RUBY.get()));
        add(ModBlocks.SAPPHIRE_ORE.get(), block -> createOreDrop(ModBlocks.SAPPHIRE_ORE.get(), ModItems.SAPPHIRE.get()));
        add(ModBlocks.TOPAZ_ORE.get(), block -> createOreDrop(ModBlocks.TOPAZ_ORE.get(), ModItems.TOPAZ.get()));
        add(ModBlocks.AMETHYST_ORE.get(), block -> createOreDrop(ModBlocks.AMETHYST_ORE.get(), ModItems.AMETHYST.get()));
        add(ModBlocks.ONYX_ORE.get(), block -> createOreDrop(ModBlocks.ONYX_ORE.get(), ModItems.ONYX.get()));

        // All deepslate ores drop the corresponding gem
        add(ModBlocks.DEEPSLATE_RUBY_ORE.get(), block -> createOreDrop(ModBlocks.DEEPSLATE_RUBY_ORE.get(), ModItems.RUBY.get()));
        add(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(), block -> createOreDrop(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(), ModItems.SAPPHIRE.get()));
        add(ModBlocks.DEEPSLATE_TOPAZ_ORE.get(), block -> createOreDrop(ModBlocks.DEEPSLATE_TOPAZ_ORE.get(), ModItems.TOPAZ.get()));
        add(ModBlocks.DEEPSLATE_AMETHYST_ORE.get(), block -> createOreDrop(ModBlocks.DEEPSLATE_AMETHYST_ORE.get(), ModItems.AMETHYST.get()));
        add(ModBlocks.DEEPSLATE_ONYX_ORE.get(), block -> createOreDrop(ModBlocks.DEEPSLATE_ONYX_ORE.get(), ModItems.ONYX.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
