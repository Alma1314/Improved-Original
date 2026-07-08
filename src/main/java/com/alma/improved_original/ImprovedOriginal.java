// 主模组类：注册所有组件（物品、方块、创造标签、附件、网络包），监听服务端启动事件
// @Mod 注解标记这是 NeoForge 模组入口，MOD_ID 来自下方常量定义
// IEventBus 用于注册 DeferredRegister 和各种监听器
// NeoForge.EVENT_BUS 用于注册服务端事件（ServerStartingEvent 等）
package com.alma.improved_original;

import com.alma.improved_original.block.ModBlocks;
import com.alma.improved_original.item.ModCreativeModeTabs;
import com.alma.improved_original.item.ModItems;
import com.alma.improved_original.quest.ModAttachments;
import com.alma.improved_original.quest.engine.QuestEngine;
import com.alma.improved_original.quest.network.ModPayloadHandlers;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(ImprovedOriginal.MOD_ID)
public class ImprovedOriginal {
    public static final String MOD_ID = "improved_original";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ImprovedOriginal(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModAttachments.register(modEventBus);
        modEventBus.addListener(ModPayloadHandlers::register);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
        // 在服务端启动时初始化 QuestEngine（确保配置已加载）
        QuestEngine.get().reloadQuestPool();
    }
}
