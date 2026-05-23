package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStatesProvider extends BlockStateProvider {

    public ModBlockStatesProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ImprovedOriginal.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ModBlocks.RUBY_BLOCK.get(), cubeAll(ModBlocks.RUBY_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.SAPPHIRE_BLOCK.get(), cubeAll(ModBlocks.SAPPHIRE_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.TOPAZ_BLOCK.get(), cubeAll(ModBlocks.TOPAZ_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.AMETHYST_BLOCK.get(), cubeAll(ModBlocks.AMETHYST_BLOCK.get()));
        simpleBlockWithItem(ModBlocks.ONYX_BLOCK.get(), cubeAll(ModBlocks.ONYX_BLOCK.get()));

        simpleBlockWithItem(ModBlocks.RUBY_ORE.get(), cubeAll(ModBlocks.RUBY_ORE.get()));
        simpleBlockWithItem(ModBlocks.SAPPHIRE_ORE.get(), cubeAll(ModBlocks.SAPPHIRE_ORE.get()));
        simpleBlockWithItem(ModBlocks.TOPAZ_ORE.get(), cubeAll(ModBlocks.TOPAZ_ORE.get()));
        simpleBlockWithItem(ModBlocks.AMETHYST_ORE.get(), cubeAll(ModBlocks.AMETHYST_ORE.get()));
        simpleBlockWithItem(ModBlocks.ONYX_ORE.get(), cubeAll(ModBlocks.ONYX_ORE.get()));

        simpleBlockWithItem(ModBlocks.DEEPSLATE_RUBY_ORE.get(), cubeAll(ModBlocks.DEEPSLATE_RUBY_ORE.get()));
        simpleBlockWithItem(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(), cubeAll(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get()));
        simpleBlockWithItem(ModBlocks.DEEPSLATE_TOPAZ_ORE.get(), cubeAll(ModBlocks.DEEPSLATE_TOPAZ_ORE.get()));
        simpleBlockWithItem(ModBlocks.DEEPSLATE_AMETHYST_ORE.get(), cubeAll(ModBlocks.DEEPSLATE_AMETHYST_ORE.get()));
        simpleBlockWithItem(ModBlocks.DEEPSLATE_ONYX_ORE.get(), cubeAll(ModBlocks.DEEPSLATE_ONYX_ORE.get()));
    }
}
