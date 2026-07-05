// 客户端任务数据缓存：存储从服务端同步的最新QuestData
// 网络线程写入，渲染线程读取，使用 volatile 保证跨线程可见性
package com.alma.improved_original.quest.client;

import com.alma.improved_original.quest.QuestData;

public class ClientQuestCache {
    private static volatile QuestData cachedData;

    public static QuestData get() {
        return cachedData;
    }

    public static void set(QuestData data) {
        cachedData = data;
    }
}
