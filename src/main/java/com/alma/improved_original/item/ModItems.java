// 物品注册：5种宝石（红宝石、蓝宝石、黄宝石、紫宝石、黑宝石）
// 使用 DeferredRegister.Items 延迟注册，Item.Properties() 使用默认属性
// 模组专用物品注册表，方块对应的 BlockItem 在 ModBlocks 中自动注册
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

