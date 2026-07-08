package com.alma.improved_original.quest.client;

import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.network.QuestSyncPayload;
import com.alma.improved_original.quest.screen.ChainProgressFragment;
import com.alma.improved_original.quest.screen.QuestFragment;
import icyllis.modernui.mc.MuiModApi;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

public class QuestClientEvents {

    public static void onQuestSyncReceived(QuestSyncPayload payload) {
        var data = payload.data();
        ClientQuestCache.set(data);

        Minecraft.getInstance().execute(() -> {
            // 检查是否有 completedChains → 这是 CHAIN_PANEL 响应
            if (!payload.completedChains().isEmpty()) {
                MuiModApi.openScreen(new ChainProgressFragment(payload.completedChains()));
                return;
            }

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
