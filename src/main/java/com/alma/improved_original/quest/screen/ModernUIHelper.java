// ModernUI 渲染辅助层 — ModernUI 为强制前置，直接调用其 API
package com.alma.improved_original.quest.screen;

import icyllis.modernui.mc.BlurHandler;
import icyllis.modernui.mc.ExtendedGuiGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

final class ModernUIHelper {

    private static final float PANEL_RADIUS = 8.0f;
    private static final float BUTTON_RADIUS = 4.0f;

    // ── 圆角矩形填充 ──
    static void fillRoundRect(GuiGraphics g, float left, float top, float right, float bottom, float radius, int color) {
        ExtendedGuiGraphics eg = new ExtendedGuiGraphics(g);
        eg.setColor(color);
        eg.fillRoundRect(left, top, right, bottom, radius);
    }

    // ── 圆角矩形描边 ──
    static void strokeRoundRect(GuiGraphics g, float left, float top, float right, float bottom, float radius, int color, float strokeWidth) {
        ExtendedGuiGraphics eg = new ExtendedGuiGraphics(g);
        eg.setColor(color);
        eg.setStrokeWidth(strokeWidth);
        eg.strokeRoundRect(left, top, right, bottom, radius);
    }

    // ── 渐变圆角矩形 ──
    static void fillGradientRoundRect(GuiGraphics g, float left, float top, float right, float bottom,
                                       float radius, int startColor, int endColor,
                                       ExtendedGuiGraphics.Orientation orientation) {
        ExtendedGuiGraphics eg = new ExtendedGuiGraphics(g);
        eg.setGradient(orientation, startColor, endColor);
        eg.fillRoundRect(left, top, right, bottom, radius);
    }

    // ── 面板圆角填充 ──
    static void fillPanelBg(GuiGraphics g, float left, float top, float right, float bottom) {
        fillRoundRect(g, left, top, right, bottom, PANEL_RADIUS, 0xCC000000);
    }

    static void fillHeaderBg(GuiGraphics g, float left, float top, float right, float bottom) {
        ExtendedGuiGraphics eg = new ExtendedGuiGraphics(g);
        eg.setColor(0xEE111111);
        eg.fillRoundRect(left, top, right, bottom, PANEL_RADIUS);
        g.fill((int) left, (int) (bottom - PANEL_RADIUS), (int) right, (int) bottom, 0xEE111111);
    }

    static void fillFooterBg(GuiGraphics g, float left, float top, float right, float bottom) {
        ExtendedGuiGraphics eg = new ExtendedGuiGraphics(g);
        eg.setColor(0xEE111111);
        eg.fillRoundRect(left, top, right, bottom, PANEL_RADIUS);
        g.fill((int) left, (int) top, (int) right, (int) (top + PANEL_RADIUS), 0xEE111111);
    }

    // ── 按钮圆角 ──
    static void fillButtonBg(GuiGraphics g, float left, float top, float right, float bottom, int color) {
        fillRoundRect(g, left, top, right, bottom, BUTTON_RADIUS, color);
    }

    // ── 背景渲染（ModernUI Gaussian blur） ──
    static void renderBackground(Screen screen, GuiGraphics g) {
        BlurHandler.INSTANCE.drawScreenBackground(g, 0, 0, screen.width, screen.height);
    }
}
