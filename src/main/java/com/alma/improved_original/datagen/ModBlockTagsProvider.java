// 数据生成-方块标签：为所有宝石方块和矿石添加 mineable/pickaxe 标签
// 继承 BlockTagsProvider，在 addTags 中添加自定义标签
// 生成到 src/generated/resources/data/minecraft/tags/block/
package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ImprovedOriginal.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.RUBY_BLOCK.get(), ModBlocks.SAPPHIRE_BLOCK.get(), ModBlocks.TOPAZ_BLOCK.get(),
                        ModBlocks.AMETHYST_BLOCK.get(), ModBlocks.ONYX_BLOCK.get())
                .add(ModBlocks.RUBY_ORE.get(), ModBlocks.SAPPHIRE_ORE.get(), ModBlocks.TOPAZ_ORE.get(),
                        ModBlocks.AMETHYST_ORE.get(), ModBlocks.ONYX_ORE.get())
                .add(ModBlocks.DEEPSLATE_RUBY_ORE.get(), ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(),
                        ModBlocks.DEEPSLATE_TOPAZ_ORE.get(), ModBlocks.DEEPSLATE_AMETHYST_ORE.get(),
                        ModBlocks.DEEPSLATE_ONYX_ORE.get());
    }
}
