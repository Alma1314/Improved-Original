package com.alma.improved_original.quest.client;

import com.alma.improved_original.quest.QuestData;

public class ClientQuestCache {
    private static QuestData cachedData;

    public static QuestData get() {
        return cachedData;
    }

    public static void set(QuestData data) {
        cachedData = data;
    }
}
