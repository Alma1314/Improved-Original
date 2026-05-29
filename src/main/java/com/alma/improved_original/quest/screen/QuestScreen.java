// 每日任务主面板
// 布局：上1/4标题+倒计时 | 中2/4=3个任务槽位平分 | 下1/4刷新+关闭按钮
// 每个槽位内：左3/4可滚动任务内容 | 右1/4锁定按钮
package com.alma.improved_original.quest.screen;

import com.alma.improved_original.Config;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestDefinition;
import com.alma.improved_original.quest.QuestSlotData;
import com.alma.improved_original.quest.client.ClientQuestCache;
import com.alma.improved_original.quest.network.C2SQuestLockPayload;
import com.alma.improved_original.quest.network.C2SQuestRefreshPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class QuestScreen extends Screen {

    private static final int PANEL_W = 256;
    private static final int PANEL_H = 200;
    private static final int HEADER_H = PANEL_H / 4;          // 50 — 上1/4
    private static final int FOOTER_H = PANEL_H / 4;          // 50 — 下1/4
    private static final int SLOT_H = (PANEL_H - HEADER_H - FOOTER_H) / QuestData.SLOT_COUNT; // 33每槽 — 中2/4平分
    private static final int SEP_H = 1;

    private QuestData questData;
    private final int[] scrollOffset = new int[QuestData.SLOT_COUNT];
    private final int[] maxScroll = new int[QuestData.SLOT_COUNT];

    public QuestScreen(QuestData questData) {
        super(Component.translatable("screen.improved_original.quest"));
        this.questData = questData;
    }

    // ---- 布局计算 ----

    private int panelX() { return (this.width - PANEL_W) / 2; }
    private int panelY() { return (this.height - PANEL_H) / 2; }

    private int contentLeft(int i) { return panelX() + PANEL_W * 3 / 4; }      // 右1/4起始 — 锁按钮区
    private int contentW() { return PANEL_W * 3 / 4 - 8; }                     // 左3/4宽度 — 任务内容

    private int slotY(int i) {
        return panelY() + HEADER_H + i * (SLOT_H + SEP_H);
    }

    private int contentH(int i) {
        var q = questData.getQuest(i);
        if (q.isEmpty()) return SLOT_H;
        QuestDefinition d = q.get();
        return Math.max(SLOT_H, 18 + d.targets().size() * 16 + d.rewards().size() * 12 + 4);
    }

    // ---- init ----

    @Override
    protected void init() {
        QuestData latest = ClientQuestCache.get();
        if (latest != null) this.questData = latest;

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            maxScroll[i] = Math.max(0, contentH(i) - SLOT_H);
            if (scrollOffset[i] > maxScroll[i]) scrollOffset[i] = maxScroll[i];
        }
    }

    // ---- render ----

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g, mx, my, pt);

        int px = panelX(), py = panelY();
        var pose = g.pose();

        // ---- 面板背景 ----
        g.fill(px, py, px + PANEL_W, py + PANEL_H, 0xCC000000);

        // ---- 上1/4: 标题 + 倒计时 ----
        g.fill(px, py, px + PANEL_W, py + HEADER_H, 0xEE111111);
        g.drawCenteredString(this.font, this.title, px + PANEL_W / 2, py + 8, 0xFFFFAA00);
        if (this.minecraft != null && this.minecraft.level != null) {
            long tick = this.minecraft.level.getGameTime();
            long iv = 20L * 60 * Config.QUEST_REFRESH_INTERVAL_MINUTES.getAsInt();
            if (iv <= 0) iv = 1;
            long sec = (iv - tick % iv) / 20;
            g.drawCenteredString(this.font,
                    Component.translatable("quest.improved_original.countdown", sec / 60, sec % 60),
                    px + PANEL_W / 2, py + 22, 0xFFAAAAAA);
        }
        // header 底部线
        g.fill(px, py + HEADER_H, px + PANEL_W, py + HEADER_H + 1, 0xFF444444);

        // ---- 中2/4: 3个任务槽位 ----
        int lockCost = Config.EMERALD_LOCK_COST.getAsInt();

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            int sy = slotY(i);
            int leftW = PANEL_W * 3 / 4;  // 左3/4 = 内容区

            // 槽位分隔线
            g.fill(px, sy, px + PANEL_W, sy + SEP_H, 0xFF444444);

            // 左3/4: 任务内容（可滚动、scissor裁剪）
            g.enableScissor(px + 1, sy + SEP_H, px + leftW - 1, sy + SEP_H + SLOT_H);

            pose.pushPose();
            pose.translate(0, -scrollOffset[i], 0);
            renderSlotContent(g, i, px + 4, sy + SEP_H + 2);
            pose.popPose();

            g.disableScissor();

            // 内容区右侧边线（分隔内容与按钮）
            g.fill(px + leftW, sy + SEP_H, px + leftW + 1, sy + SEP_H + SLOT_H, 0xFF333333);

            // 右1/4: 锁定按钮区域
            int btnZoneX = px + contentLeft(i) - panelX();
            var qo = questData.getQuest(i);
            boolean hasQuest = qo.isPresent();
            boolean done = hasQuest && questData.getSlot(i).isComplete();

            if (hasQuest && !done) {
                String label = questData.isSlotLocked(i)
                        ? Component.translatable("quest.improved_original.locked").getString()
                        : Component.translatable("quest.improved_original.lock_button").getString();
                int btnW = contentLeft(i) - panelX() - (px + leftW) - 8; // 按钮区可用宽度
                int btnX = px + leftW + 4;
                int btnY = sy + SEP_H + SLOT_H / 2 - 10;

                boolean hovered = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + 20;
                int bgColor = hovered ? 0xCC555555 : 0xCC333333;
                int borderColor = hovered ? 0xFFFFFFFF : 0xFF888888;

                g.fill(btnX, btnY, btnX + btnW, btnY + 20, bgColor);
                g.renderOutline(btnX, btnY, btnX + btnW, btnY + 20, borderColor);
                g.drawCenteredString(this.font, Component.literal(label),
                        btnX + btnW / 2, btnY + 6, 0xFFFFFFFF);
            }
        }

        // 槽位底部线
        int slotsBottom = slotY(QuestData.SLOT_COUNT - 1) + SLOT_H + SEP_H;
        g.fill(px, slotsBottom, px + PANEL_W, slotsBottom + 1, 0xFF444444);

        // ---- 下1/4: 刷新 + 关闭按钮 ----
        int footerY = py + PANEL_H - FOOTER_H;
        g.fill(px, footerY, px + PANEL_W, py + PANEL_H, 0xEE111111);

        int refreshCost = Config.EMERALD_REFRESH_COST.getAsInt();
        String refreshLabel = Component.translatable("quest.improved_original.refresh_button", refreshCost).getString();
        String doneLabel = Component.translatable("quest.improved_original.done").getString();

        int btnW1 = 80, btnW2 = 40;
        int btnY = footerY + 16;
        int btnX1 = px + PANEL_W / 2 - btnW1 - 4;
        int btnX2 = px + PANEL_W / 2 + 4;

        // 刷新按钮
        boolean rfHover = mx >= btnX1 && mx <= btnX1 + btnW1 && my >= btnY && my <= btnY + 16;
        g.fill(btnX1, btnY, btnX1 + btnW1, btnY + 16, rfHover ? 0xCC666666 : 0xCC444444);
        g.renderOutline(btnX1, btnY, btnX1 + btnW1, btnY + 16, rfHover ? 0xFFFFFFFF : 0xFF888888);
        g.drawCenteredString(this.font, Component.literal(refreshLabel),
                btnX1 + btnW1 / 2, btnY + 4, 0xFFFFFFFF);

        // 关闭按钮
        boolean dnHover = mx >= btnX2 && mx <= btnX2 + btnW2 && my >= btnY && my <= btnY + 16;
        g.fill(btnX2, btnY, btnX2 + btnW2, btnY + 16, dnHover ? 0xCC666666 : 0xCC444444);
        g.renderOutline(btnX2, btnY, btnX2 + btnW2, btnY + 16, dnHover ? 0xFFFFFFFF : 0xFF888888);
        g.drawCenteredString(this.font, Component.literal(doneLabel),
                btnX2 + btnW2 / 2, btnY + 4, 0xFFFFFFFF);
    }

    private void renderSlotContent(GuiGraphics g, int i, int cx, int sy) {
        var qo = questData.getQuest(i);
        int cw = contentW() - 10;

        if (qo.isEmpty()) {
            g.drawString(this.font, Component.translatable("quest.improved_original.empty_slot"),
                    cx, sy + SLOT_H / 2 - 5, 0xFF666666);
            return;
        }

        QuestDefinition q = qo.get();
        QuestSlotData sl = questData.getSlot(i);
        List<Integer> pt = sl.perTargetProgress();
        boolean done = sl.isComplete();

        // 槽位编号 + 任务名
        Component name = buildName(q);
        int color = done ? 0xFF55FF55 : 0xFFFFFFFF;
        g.drawString(this.font, "[" + (i + 1) + "] ", cx, sy, 0xFF555555);
        g.drawString(this.font, name, cx + this.font.width("[x] "), sy, color);

        if (questData.isSlotLocked(i)) {
            g.drawString(this.font, Component.translatable("quest.improved_original.locked"),
                    cx + this.font.width("[x] ") + this.font.width(name) + 4, sy, 0xFFFF5555);
        }

        // 进度条
        int lineY = sy + 11;
        int barW = Math.min(cw - 4, 120);
        for (int t = 0; t < q.targets().size(); t++) {
            QuestDefinition.QuestTarget tg = q.targets().get(t);
            int prog = t < pt.size() ? pt.get(t) : 0;
            int max = tg.count();
            boolean tc = prog >= max;

            g.drawString(this.font, q.getTargetDisplayName(tg.type(), tg.item()), cx, lineY, 0xFFCCCCCC);

            int bh = 6;
            int by = lineY + this.font.lineHeight;
            g.fill(cx, by, cx + barW, by + bh, 0xFF333333);
            if (max > 0) {
                int fw = (int) ((float) prog / max * barW);
                g.fill(cx, by, cx + fw, by + bh, tc ? 0xFF55FF55 : 0xFF4488FF);
            }

            var p = g.pose();
            p.pushPose();
            p.scale(0.6f, 0.6f, 1);
            g.drawString(this.font, prog + "/" + max,
                    (int) ((cx + barW + 3) / 0.6f), (int) ((by + 1) / 0.6f), 0xFFAAAAAA);
            p.popPose();

            lineY = by + bh + 3;
        }

        // 奖励
        lineY = Math.max(lineY, sy + 12 + q.targets().size() * 12);
        var rn = q.getRewardDisplayNames();
        for (int r = 0; r < q.rewards().size(); r++) {
            QuestDefinition.ItemCount rw = q.rewards().get(r);
            g.drawString(this.font, Component.translatable("quest.improved_original.reward", rw.count(), rn.get(r)),
                    cx, lineY, done ? 0xFF55FF55 : 0xFFFFAA00);
            lineY += this.font.lineHeight + 1;
        }

        // 滚动条指示器（在内容区右边缘内侧）
        if (maxScroll[i] > 0) {
            int barX = panelX() + PANEL_W * 3 / 4 - 4;
            int barH = Math.max(8, SLOT_H * SLOT_H / contentH(i));
            int barY = slotY(i) + SEP_H + scrollOffset[i] * (SLOT_H - barH) / maxScroll[i];
            g.fill(barX, barY, barX + 2, barY + barH, 0x66AAAAAA);
        }
    }

    // ---- 鼠标事件 ----

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        int px = panelX();
        int leftW = PANEL_W * 3 / 4;
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            if (maxScroll[i] <= 0) continue;
            int sy = slotY(i) + SEP_H;
            // 只在内容区域（左3/4）响应滚动
            if (mx >= px && mx <= px + leftW && my >= sy && my <= sy + SLOT_H) {
                scrollOffset[i] = clamp(scrollOffset[i] - (int) scrollY * 10, 0, maxScroll[i]);
                return true;
            }
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return super.mouseClicked(mx, my, btn);

        int px = panelX(), py = panelY();
        int leftW = PANEL_W * 3 / 4;

        // 锁按钮点击
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            final int slot = i;
            var qo = questData.getQuest(i);
            boolean hasQuest = qo.isPresent();
            boolean done = hasQuest && questData.getSlot(i).isComplete();
            if (!hasQuest || done) continue;

            int sy = slotY(i) + SEP_H;
            int btnX = px + leftW + 4;
            int btnW = PANEL_W / 4 - 12;
            int btnY = sy + SLOT_H / 2 - 10;

            if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + 20) {
                PacketDistributor.sendToServer(new C2SQuestLockPayload(slot));
                return true;
            }
        }

        // 底部按钮点击
        int footerY = py + PANEL_H - FOOTER_H;
        int btnY = footerY + 16;
        int btnW1 = 80, btnW2 = 40;
        int btnX1 = px + PANEL_W / 2 - btnW1 - 4;
        int btnX2 = px + PANEL_W / 2 + 4;

        if (mx >= btnX1 && mx <= btnX1 + btnW1 && my >= btnY && my <= btnY + 16) {
            PacketDistributor.sendToServer(new C2SQuestRefreshPayload());
            return true;
        }
        if (mx >= btnX2 && mx <= btnX2 + btnW2 && my >= btnY && my <= btnY + 16) {
            onClose();
            return true;
        }

        return super.mouseClicked(mx, my, btn);
    }

    private static int clamp(int val, int min, int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    private Component buildName(QuestDefinition q) {
        if (q.name() != null && !q.name().isEmpty()) return Component.translatable(q.name());
        if (!q.targets().isEmpty()) {
            var ft = q.targets().get(0);
            return Component.translatable("quest.improved_original.desc." + ft.type().getTranslationKeySuffix(),
                    q.getTargetDisplayName(ft.type(), ft.item()), ft.count());
        }
        return Component.translatable("quest.improved_original.empty_slot");
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(null);
    }
}
