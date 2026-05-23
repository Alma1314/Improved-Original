// 客户端类：注册模组配置界面，监听客户端设置事件
package com.alma.improved_original;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ImprovedOriginal.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ImprovedOriginal.MOD_ID, value = Dist.CLIENT)
public class ImprovedOriginalClient {
    public ImprovedOriginalClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        ImprovedOriginal.LOGGER.info("HELLO FROM CLIENT SETUP");
        ImprovedOriginal.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
