package com.alma.elementalsorcery;

import com.alma.elementalsorcery.block.ModBlocks;
import com.alma.elementalsorcery.item.ModCreativeModeTabs;
import com.alma.elementalsorcery.item.ModItems;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// 这里的值应该与 META-INF/neoforge.mods.toml 文件中的条目相匹配
@Mod(ElementalSorcery.MOD_ID)
public class ElementalSorcery {
    // 在一个通用的地方定义模组ID，方便所有内容引用
    public static final String MOD_ID = "improved_original";
    // 直接引用slf4j日志机
    public static final Logger LOGGER = LogUtils.getLogger();

    // mod类的构造子是加载mod时运行的第一个代码。
    // FML 会识别一些参数类型，比如 IEventBus 或 ModContainer，并自动传递。
    public ElementalSorcery(IEventBus modEventBus, ModContainer modContainer) {
        // 注册 commonSetup 方法进行模组加载
        modEventBus.addListener(this::commonSetup);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        // 注意，当且仅当我们希望*这个*职业（元素魔法）能直接响应事件时，这是必要的。
        // 如果这个类（ElementalSorcery）没有被注解为 @SubscribeEvent，则不要添加此行。
        NeoForge.EVENT_BUS.register(this);

        // 注册我们的模组的 ModConfigSpec 以便 FML 可以为我们创建并加载配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // 一些常见的设置代码
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // 你可以使用 SubscribeEvent 并让事件总线发现要调用的方法
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 当服务器启动时执行某些操作
        LOGGER.info("HELLO from server starting");
    }
}
