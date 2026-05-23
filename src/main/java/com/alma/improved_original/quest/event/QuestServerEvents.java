package com.alma.improved_original.quest.event;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.quest.QuestManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = ImprovedOriginal.MOD_ID)
public class QuestServerEvents {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        QuestManager.onServerTick(event.getServer());
    }
}
