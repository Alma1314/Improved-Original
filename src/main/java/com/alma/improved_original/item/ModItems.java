package com.alma.improved_original.item;

import com.alma.improved_original.ImprovedOriginal;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ImprovedOriginal.MOD_ID);

    public static final DeferredItem<Item> RUBY =
            ITEMS.register("ruby", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SAPPHIRE =
            ITEMS.register("sapphire", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TOPAZ =
            ITEMS.register("topaz", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> AMETHYST =
            ITEMS.register("amethyst", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> ONYX =
            ITEMS.register("onyx", () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

