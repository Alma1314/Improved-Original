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

// 这个职业不会在专用服务器上加载。从这里访问客户端代码是安全的。
@Mod(value = ElementalSorcery.MOD_ID, dist = Dist.CLIENT)
// 你可以使用 EventBusSubscriber 自动注册所有标注为 @SubscribeEvent 的类中的所有静态方法。
@EventBusSubscriber(modid = ElementalSorcery.MOD_ID, value = Dist.CLIENT)
public class ElementalSorceryClient {
    public ElementalSorceryClient(ModContainer container) {
        // 允许NeoForge为该模组的配置创建配置界面。
        // 配置界面通过访问模组屏幕 > 点击你的模组 > 点击配置来访问。
        // 不要忘记为你的配置选项添加翻译到 en_us.json 文件。
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // 一些客户端设置代码
        ElementalSorcery.LOGGER.info("HELLO FROM CLIENT SETUP");
        ElementalSorcery.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
