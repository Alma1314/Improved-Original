// 客户端网络事件处理：接收服务端同步包，更新缓存，刷新面板，显示完成Toast
// 使用 ModernUI Fragment 替代原 QuestScreen
package com.alma.improved_original.quest.client;

import com.alma.improved_original.quest.network.S2CQuestSyncPayload;
import com.alma.improved_original.quest.screen.QuestFragment;
import icyllis.modernui.mc.MuiModApi;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class QuestClientEvents {

    public static void onQuestSyncReceived(S2CQuestSyncPayload payload) {
        var data = payload.data();
        ClientQuestCache.set(data);

        Minecraft.getInstance().execute(() -> {
            // Open screen when /quest command is used
            if (payload.openScreen()) {
                MuiModApi.openScreen(new QuestFragment());
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
