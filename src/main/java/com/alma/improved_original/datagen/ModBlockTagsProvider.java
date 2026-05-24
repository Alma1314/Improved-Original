// 数据生成-方块标签：为宝石块添加 mineable/pickaxe 标签，确保只能用镐采集
// 继承 BlockTagsProvider，在 addTags 中添加自定义标签
// 生成到 src/generated/resources/data/minecraft/tags/block/
package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.block.ModBlocks;
import com.alma.improved_original.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.references.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ImprovedOriginal.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.AMETHYST_BLOCK.get(), ModBlocks.RUBY_BLOCK.get(), ModBlocks.ONYX_BLOCK.get(),
                        ModBlocks.TOPAZ_BLOCK.get(), ModBlocks.SAPPHIRE_BLOCK.get());
    }
}
