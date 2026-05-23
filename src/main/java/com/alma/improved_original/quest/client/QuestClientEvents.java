package com.alma.improved_original.quest.client;

import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.screen.QuestScreen;
import net.minecraft.client.Minecraft;

public class QuestClientEvents {
    public static void onQuestDataReceived(QuestData data) {
        ClientQuestCache.set(data);
        Minecraft.getInstance().execute(() -> {
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof QuestScreen) {
                // Refresh existing screen
                Minecraft.getInstance().setScreen(new QuestScreen(data));
            } else {
                Minecraft.getInstance().setScreen(new QuestScreen(data));
            }
        });
    }
}
