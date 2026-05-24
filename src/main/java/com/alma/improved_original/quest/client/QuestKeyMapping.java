// 任务面板按键绑定：默认O键打开任务界面，可在游戏设置中修改
// 注册到 RegisterKeyMappingsEvent 使其出现在"控制"设置界面
// KeyConflictContext.IN_GAME 限定仅在游戏中生效，GUI界面不触发
// consumeClick() 防止长按重复发送网络包
package com.alma.improved_original.quest.client;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.quest.network.C2SQuestOpenPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ImprovedOriginal.MOD_ID, value = Dist.CLIENT)
public class QuestKeyMapping {
    public static final String CATEGORY = "key.categories." + ImprovedOriginal.MOD_ID;

    public static final KeyMapping OPEN_QUEST_KEY = new KeyMapping(
            "key." + ImprovedOriginal.MOD_ID + ".open_quest",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_QUEST_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (OPEN_QUEST_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new C2SQuestOpenPayload());
        }
    }
}
