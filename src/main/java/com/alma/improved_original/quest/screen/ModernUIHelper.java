// ModernUI 反射适配层 — 在 ModernUI 存在时提供圆角矩形、渐变等增强渲染
// 所有 ModernUI 类访问通过此辅助类隔离，避免未安装 ModernUI 时类加载错误
package com.alma.improved_original.quest.screen;

import com.alma.improved_original.Config;
import icyllis.modernui.mc.ExtendedGuiGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;

final class ModernUIHelper {

    private static final boolean ENABLED = checkEnabled();
    private static final float PANEL_RADIUS = 8.0f;
    private static final float BUTTON_RADIUS = 4.0f;

    private static boolean checkEnabled() {
        return ModList.get().isLoaded("modernui") && Config.MODERN_UI_ENABLED.getAsBoolean();
    }

    /** 刷新可用状态（配置变更后调用） */
    static void refresh() {
        // ENABLED is re-evaluated via checkEnabled on each call path;
        // kept as a convenience for potential future hot-reload
    }

    static boolean isAvailable() {
        return ENABLED;
    }

    // ── 圆角矩形填充 ──

    static void fillRoundRect(GuiGraphics g, float left, float top, float right, float bottom, float radius, int color) {
        if (ENABLED) {
            ExtendedGuiGraphics eg = new ExtendedGuiGraphics(g);
            eg.setColor(color);
            eg.fillRoundRect(left, top, right, bottom, radius);
        } else {
            g.fill((int) left, (int) top, (int) right, (int) bottom, color);
        }
    }

    // ── 圆角矩形描边 ──

    static void strokeRoundRect(GuiGraphics g, float left, float top, float right, float bottom, float radius, int color, float strokeWidth) {
        if (ENABLED) {
            ExtendedGuiGraphics eg = new ExtendedGuiGraphics(g);
            eg.setColor(color);
            eg.setStrokeWidth(strokeWidth);
            eg.strokeRoundRect(left, top, right, bottom, radius);
        }
    }

    // ── 渐变圆角矩形 ──

    static void fillGradientRoundRect(GuiGraphics g, float left, float top, float right, float bottom,
                                       float radius, int startColor, int endColor,
                                       ExtendedGuiGraphics.Orientation orientation) {
        if (ENABLED) {
            ExtendedGuiGraphics eg = new ExtendedGuiGraphics(g);
            eg.setGradient(orientation, startColor, endColor);
            eg.fillRoundRect(left, top, right, bottom, radius);
        } else {
            g.fillGradient((int) left, (int) top, (int) right, (int) bottom, startColor, endColor);
        }
    }

    // ── 面板圆角填充（clip-aware，用于 header/footer 裁剪圆角） ──

    static void fillPanelBg(GuiGraphics g, float left, float top, float right, float bottom) {
        fillRoundRect(g, left, top, right, bottom, PANEL_RADIUS, 0xCC000000);
    }

    static void fillHeaderBg(GuiGraphics g, float left, float top, float right, float bottom) {
        if (ENABLED) {
            ExtendedGuiGraphics eg = new ExtendedGuiGraphics(g);
            eg.setColor(0xEE111111);
            eg.fillRoundRect(left, top, right, bottom, PANEL_RADIUS);
            // 盖掉下半部分圆角，使只有上两角圆角
            g.fill((int) left, (int) (bottom - PANEL_RADIUS), (int) right, (int) bottom, 0xEE111111);
        } else {
            g.fill((int) left, (int) top, (int) right, (int) bottom, 0xEE111111);
        }
    }

    static void fillFooterBg(GuiGraphics g, float left, float top, float right, float bottom) {
        if (ENABLED) {
            ExtendedGuiGraphics eg = new ExtendedGuiGraphics(g);
            eg.setColor(0xEE111111);
            eg.fillRoundRect(left, top, right, bottom, PANEL_RADIUS);
            // 盖掉上半部分圆角，使只有下两角圆角
            g.fill((int) left, (int) top, (int) right, (int) (top + PANEL_RADIUS), 0xEE111111);
        } else {
            g.fill((int) left, (int) top, (int) right, (int) bottom, 0xEE111111);
        }
    }

    // ── 按钮圆角 ──

    static void fillButtonBg(GuiGraphics g, float left, float top, float right, float bottom, int color) {
        fillRoundRect(g, left, top, right, bottom, BUTTON_RADIUS, color);
    }

    // ── 背景渲染（使用 ModernUI 的 Gaussian blur） ──

    static void renderBackground(Screen screen, GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (ENABLED) {
            icyllis.modernui.mc.BlurHandler.INSTANCE.drawScreenBackground(g, 0, 0, screen.width, screen.height);
        } else {
            screen.renderBackground(g, mouseX, mouseY, partialTick);
        }
    }
}
