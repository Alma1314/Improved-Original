package com.alma.improved_original.item;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ImprovedOriginal.MOD_ID);

    public static final Supplier<CreativeModeTab> GEMSTONES_TAB =
            CREATIVE_MODE_TABS.register("gemstones_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.RUBY.get()))
                    .title(Component.translatable("itemGroup.gemstones_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.RUBY);
                        output.accept(ModItems.SAPPHIRE);
                        output.accept(ModItems.TOPAZ);
                        output.accept(ModItems.AMETHYST);
                        output.accept(ModItems.ONYX);
                        output.accept(Items.EMERALD);
                        output.accept(ModBlocks.RUBY_BLOCK);
                        output.accept(ModBlocks.SAPPHIRE_BLOCK);
                        output.accept(ModBlocks.TOPAZ_BLOCK);
                        output.accept(ModBlocks.AMETHYST_BLOCK);
                        output.accept(ModBlocks.ONYX_BLOCK);
                        output.accept(Items.EMERALD_BLOCK);
                        output.accept(ModBlocks.RUBY_ORE);
                        output.accept(ModBlocks.SAPPHIRE_ORE);
                        output.accept(ModBlocks.TOPAZ_ORE);
                        output.accept(ModBlocks.AMETHYST_ORE);
                        output.accept(ModBlocks.ONYX_ORE);
                        output.accept(Items.EMERALD_ORE);
                        output.accept(ModBlocks.DEEPSLATE_RUBY_ORE);
                        output.accept(ModBlocks.DEEPSLATE_SAPPHIRE_ORE);
                        output.accept(ModBlocks.DEEPSLATE_TOPAZ_ORE);
                        output.accept(ModBlocks.DEEPSLATE_AMETHYST_ORE);
                        output.accept(ModBlocks.DEEPSLATE_ONYX_ORE);
                        output.accept(Items.DEEPSLATE_EMERALD_ORE);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}


