// 客户端任务数据缓存：存储从服务端同步的最新QuestData
// 网络线程写入，渲染线程读取，使用 volatile 保证跨线程可见性
// listener 机制：面板注册回调，服务端数据到达时立即通知刷新
// version：每次 set() 递增，UI 层通过版本号比较检测数据变化，避免重复刷新
package com.alma.improved_original.quest.client;

import com.alma.improved_original.quest.QuestData;

public class ClientQuestCache {
    private static volatile QuestData cachedData;
    private static volatile Runnable onDataChanged;
    private static volatile long version;

    public static QuestData get() {
        return cachedData;
    }

    public static long getVersion() {
        return version;
    }

    public static void set(QuestData data) {
        cachedData = data;
        version++;
        Runnable listener = onDataChanged;
        if (listener != null) {
            listener.run();
        }
    }

    public static void setListener(Runnable listener) {
        onDataChanged = listener;
    }

    public static void clearListener() {
        onDataChanged = null;
    }
}