// 客户端网络事件处理：接收服务端同步包，更新缓存，刷新面板，显示完成Toast
// 在 Minecraft.getInstance().execute() 中执行以确保线程安全
// 三种处理分支：打开面板、刷新已有面板、显示完成Toast（可叠加）
package com.alma.improved_original.quest.client;

import com.alma.improved_original.quest.network.S2CQuestSyncPayload;
import com.alma.improved_original.quest.screen.QuestScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class QuestClientEvents {

    public static void onQuestSyncReceived(S2CQuestSyncPayload payload) {
        var data = payload.data();
        ClientQuestCache.set(data);

        Minecraft.getInstance().execute(() -> {
            var screen = Minecraft.getInstance().screen;

            // Open screen when /quest command is used
            if (payload.openScreen()) {
                Minecraft.getInstance().setScreen(new QuestScreen(data));
            } else if (screen instanceof QuestScreen) {
                // Refresh existing screen in-place
                screen.init(Minecraft.getInstance(), screen.width, screen.height);
            }

            // Show completion toast
            if (payload.completion().isPresent()) {
                var completion = payload.completion().get();
                Component title = Component.translatable("quest.improved_original.toast.title");
                Component desc = Component.translatable("quest.improved_original.toast.desc",
                        completion.questDescription(), completion.rewardText());
                Minecraft.getInstance().getToasts().addToast(
                        new QuestToast(title, desc)
                );
            }
        });
    }
}
