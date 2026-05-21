package com.alma.improved_original.block;

import com.alma.improved_original.ElementalSorcery;
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
    // 创建延迟注册器1
    // 在 NeoForge 1.21+ 中，DeferredRegister 被进一步细化为泛型专用的静态内部类，如DeferredRegister.Blocks
    // DeferredRegister.createBlocks(modId)，这是一个工厂方法，用于创建一个专门注册方块的 DeferredRegister.Blocks 实例
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ElementalSorcery.MOD_ID);

    // BlockBehaviour.Properties，在 Minecraft 1.20+ 中，Block.Properties 被重命名为 BlockBehaviour.Properties
    // 它定义了方块的物理和交互属性，例如：
    // 硬度（hardness）抗爆性（explosion resistance）是否透明，是否遮挡光线挖掘工具要求，声音类型，材质（map color）
    // BlockBehaviour.Properties.of() 用于设置方块的各种属性 .ofFullCopy 即复制一个方块的所有属性
    // 不存在 new Block.Properties()
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

    // 使用与方块相同的注册名（如 "ruby_block"），在 ModItems.ITEMS 中注册一个 BlockItem。
    // block.get()：获取真实的 Block 实例
    // new BlockItem(block, props)：将方块包装成物品
    // 注册到 ModItems.ITEMS
    // 确保了方块和其物品使用同一个 ID，符合原版惯例
    private static <T extends Block> void registerBlockItems(String name, DeferredBlock<T> block){
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // 先注册方块（返回 DeferredBlock<T>）
    // <T extends Block> 是 Java 中的 泛型（Generic）语法，表示：“T 是一个类型参数，它必须是 Block 类或其任意子类。”
    // 立即调用 registerBlockItems 为其注册对应物品
    // 返回 DeferredBlock<T>，便于后续引用（如用于配方、战利品等）
    private static <T extends Block> DeferredBlock<T> registerBlocks(String name, Supplier<T> block){
        DeferredBlock<T> blocks = BLOCKS.register(name, block);
        registerBlockItems(name, blocks);
        return blocks;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
