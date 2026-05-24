// 任务完成Toast通知：原版风格，右上角弹出，5秒自动消失
package com.alma.improved_original.quest.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;

public class QuestToast implements Toast {
    private static final int DURATION = 5000;
    private final Component title;
    private final Component desc;

    public QuestToast(Component title, Component desc) {
        this.title = title;
        this.desc = desc;
    }

    @Override
    public Visibility render(GuiGraphics g, ToastComponent tc, long time) {
        // 深色背景
        g.fill(0, 0, width(), height(), 0xCC000000);
        g.renderOutline(0, 0, width(), height(), 0xFF555555);

        Font f = tc.getMinecraft().font;
        g.drawString(f, title, 8, 7, 0xFFFFAA00);
        g.drawString(f, desc, 8, 20, 0xFFFFFFFF);

        return time < DURATION ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public int width() { return 160; }

    @Override
    public int height() { return 36; }
}
