// 方块注册：5种宝石块+5种矿石+5种深层矿石，自动注册对应的BlockItem
package com.alma.improved_original.block;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ImprovedOriginal.MOD_ID);

    public static final DeferredBlock<Block> RUBY_BLOCK =
            registerBlocks("ruby_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK)));

    public static final DeferredBlock<Block> SAPPHIRE_BLOCK =
            registerBlocks("sapphire_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK)));

    public static final DeferredBlock<Block> TOPAZ_BLOCK =
            registerBlocks("topaz_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK)));

    public static final DeferredBlock<Block> AMETHYST_BLOCK =
            registerBlocks("amethyst_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK)));

    public static final DeferredBlock<Block> ONYX_BLOCK =
            registerBlocks("onyx_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK)));

    public static final DeferredBlock<Block> RUBY_ORE =
            registerBlocks("ruby_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_ORE)));

    public static final DeferredBlock<Block> SAPPHIRE_ORE =
            registerBlocks("sapphire_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_ORE)));

    public static final DeferredBlock<Block> TOPAZ_ORE =
            registerBlocks("topaz_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_ORE)));

    public static final DeferredBlock<Block> AMETHYST_ORE =
            registerBlocks("amethyst_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_ORE)));

    public static final DeferredBlock<Block> ONYX_ORE =
            registerBlocks("onyx_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_RUBY_ORE =
            registerBlocks("deepslate_ruby_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_EMERALD_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_SAPPHIRE_ORE =
            registerBlocks("deepslate_sapphire_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_EMERALD_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_TOPAZ_ORE =
            registerBlocks("deepslate_topaz_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_EMERALD_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_AMETHYST_ORE =
            registerBlocks("deepslate_amethyst_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_EMERALD_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_ONYX_ORE =
            registerBlocks("deepslate_onyx_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_EMERALD_ORE)));


    private static <T extends Block> void registerBlockItems(String name, DeferredBlock<T> block){
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }


    private static <T extends Block> DeferredBlock<T> registerBlocks(String name, Supplier<T> block){
        DeferredBlock<T> blocks = BLOCKS.register(name, block);
        registerBlockItems(name, blocks);
        return blocks;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
