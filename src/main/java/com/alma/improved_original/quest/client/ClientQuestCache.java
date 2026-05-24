// 客户端任务数据缓存：存储从服务端同步的最新QuestData
// 客户端网络处理收到同步包后更新此缓存，UI层从此读取
// 简单静态单例，线程安全由 Minecraft 主线程保证
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
