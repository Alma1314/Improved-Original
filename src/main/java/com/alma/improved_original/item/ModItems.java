package com.alma.improved_original.item;

import com.alma.improved_original.ImprovedOriginal;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    // 创建延迟注册器，详细见 ModBlock 文件
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ImprovedOriginal.MOD_ID);

    // 在字段初始化阶段，游戏注册表还未准备好，不能直接创建或使用 Item，DeferredItem<Item> 是一个代表“尚未注册但将在未来注册的物品”的引用类型
    // ITEMS是一个 DeferredRegister.Items 类型的静态字段，在 ModItems 类中定义，专门用于注册 Item 的延迟注册器
    // 调用 ITEMS.register(...) 方法，向注册器中添加一个待注册的物品
    // "ruby" 是该物品的注册路径（registry path），最终资源定位符（ResourceLocation）为：elemental_sorcery:ruby
    // 第二个参数是一个 Supplier<Item>（函数式接口），即一个 lambda 表达式，用于在注册时创建物品实例，代表“无参、返回 Item 的函数”
    // new Item.Properties() 是创建一个物品属性（Item Properties）对象的方式，用于配置自定义物品的行为和外观（如最大堆叠数量、是否可食用等）
    // 此处无参为默认，即物品的最大堆叠数量为 64，不可食用
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

    // Minecraft NeoForge 1.21.1 模组开发中用于触发物品（Items）自动注册的标准方法
    // 将之前通过 DeferredRegister.Items 声明的所有物品，绑定到模组事件总线（Mod Event Bus）上以便 NeoForge 在游戏启动的正确阶段自动完成注册
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

