// 每日任务主面板
// 固定面板大小不超出屏幕，每个槽位内容超过最大高度时可独立滚动
// 锁定按钮和底部按钮悬浮在槽位内容上方（高z-index），不被scissor裁剪
package com.alma.improved_original.quest.screen;

import com.alma.improved_original.Config;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestDefinition;
import com.alma.improved_original.quest.QuestSlotData;
import com.alma.improved_original.quest.client.ClientQuestCache;
import com.alma.improved_original.quest.network.C2SQuestLockPayload;
import com.alma.improved_original.quest.network.C2SQuestRefreshPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class QuestScreen extends Screen {

    private static final int PANEL_W = 256;
    private static final int HEADER_H = 26;
    private static final int FOOTER_H = 24;
    private static final int SLOT_MIN_H = 64;
    private static final int SLOT_MAX_H = 96;
    private static final int SEP_H = 1;
    private static final int LEFT_W = 20;
    private static final int SCROLL_BAR_W = 3;
    private static final int BUTTON_BOTTOM_PAD = 8;

    private QuestData questData;
    private final int[] scrollOffset = new int[QuestData.SLOT_COUNT];
    private final int[] maxScroll = new int[QuestData.SLOT_COUNT];
    private Button refreshBtn;
    private Button doneBtn;

    public QuestScreen(QuestData questData) {
        super(Component.translatable("screen.improved_original.quest"));
        this.questData = questData;
    }

    // ---- 布局计算 ----

    private int panelX() { return (this.width - PANEL_W) / 2; }
    private int panelY() { return (this.height - panelH()) / 2; }

    private int panelH() {
        int h = HEADER_H + FOOTER_H + SEP_H * (QuestData.SLOT_COUNT + 1);
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) h += slotDisplayH(i);
        return Math.min(h, this.height - 20);
    }

    private int slotContentH(int i) {
        var q = questData.getQuest(i);
        if (q.isEmpty()) return SLOT_MIN_H;
        QuestDefinition d = q.get();
        // 底部留空让滚到底时内容不贴按钮
        return Math.max(SLOT_MIN_H, 22 + d.targets().size() * 16 + d.rewards().size() * 12 + 12 + BUTTON_BOTTOM_PAD);
    }

    private int slotDisplayH(int i) {
        return Math.min(slotContentH(i), SLOT_MAX_H);
    }

    private int slotY(int i) {
        int y = panelY() + HEADER_H + SEP_H;
        for (int j = 0; j < i; j++) y += slotDisplayH(j) + SEP_H;
        return y;
    }

    // ---- init ----

    @Override
    protected void init() {
        QuestData latest = ClientQuestCache.get();
        if (latest != null) this.questData = latest;

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            maxScroll[i] = Math.max(0, slotContentH(i) - slotDisplayH(i));
            if (scrollOffset[i] > maxScroll[i]) scrollOffset[i] = maxScroll[i];
        }

        // 底部按钮 — 作为 widget 注册获取点击事件，但手动渲染以控制 z-order
        int fy = panelY() + panelH() - FOOTER_H;
        int btnY = fy + 4;
        int px = panelX();
        int refreshCost = Config.EMERALD_REFRESH_COST.getAsInt();

        refreshBtn = Button.builder(
                Component.translatable("quest.improved_original.refresh_button", refreshCost),
                b -> { b.active = false; PacketDistributor.sendToServer(new C2SQuestRefreshPayload()); }
        ).bounds(px + PANEL_W / 2 - 62, btnY, 80, 16).build();
        this.addRenderableWidget(refreshBtn);

        doneBtn = Button.builder(
                Component.translatable("quest.improved_original.done"), b -> onClose()
        ).bounds(px + PANEL_W / 2 + 22, btnY, 40, 16).build();
        this.addRenderableWidget(doneBtn);
    }

    // ---- render ----

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g, mx, my, pt);

        int px = panelX(), py = panelY();
        int ph = panelH();

        var pose = g.pose();

        // ---- Z 层 0: 面板背景 ----
        g.fill(px, py, px + PANEL_W, py + ph, 0xCC000000);

        // ---- Z 层 1: 槽位内容（含 scissor 裁剪） ----
        pose.pushPose();
        pose.translate(0, 0, 10);

        // 标题区（固定，不滚动）
        g.fill(px, py, px + PANEL_W, py + HEADER_H, 0x88000000);
        g.drawCenteredString(this.font, this.title, px + PANEL_W / 2, py + 5, 0xFFFFAA00);
        if (this.minecraft != null && this.minecraft.level != null) {
            long tick = this.minecraft.level.getGameTime();
            long iv = 20L * 60 * Config.QUEST_REFRESH_INTERVAL_MINUTES.getAsInt();
            if (iv <= 0) iv = 1;
            long sec = (iv - tick % iv) / 20;
            g.drawCenteredString(this.font,
                    Component.translatable("quest.improved_original.countdown", sec / 60, sec % 60),
                    px + PANEL_W / 2, py + 16, 0xFFAAAAAA);
        }

        int lockCost = Config.EMERALD_LOCK_COST.getAsInt();

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            int sy = slotY(i);
            int sh = slotDisplayH(i);
            g.fill(px, sy - SEP_H, px + PANEL_W, sy, 0xFF444444);

            // scissor 裁剪槽位可见区域（仅裁剪内容，不裁剪按钮）
            g.enableScissor(px, sy, px + PANEL_W, sy + sh);

            pose.pushPose();
            pose.translate(0, -scrollOffset[i], 0);
            renderSlotContent(g, i, px, sy);
            pose.popPose();

            g.disableScissor();

            // 滚动条指示器
            if (maxScroll[i] > 0) {
                int barX = px + PANEL_W - SCROLL_BAR_W - 2;
                int barH = Math.max(8, sh * sh / slotContentH(i));
                int barY = sy + scrollOffset[i] * (sh - barH) / maxScroll[i];
                g.fill(barX, barY, barX + SCROLL_BAR_W, barY + barH, 0x66AAAAAA);
            }
        }

        int bottomSepY = slotY(QuestData.SLOT_COUNT - 1) + slotDisplayH(QuestData.SLOT_COUNT - 1);
        g.fill(px, bottomSepY, px + PANEL_W, bottomSepY + SEP_H, 0xFF444444);
        // 底部区域背景
        g.fill(px, bottomSepY + SEP_H, px + PANEL_W, py + ph, 0xCC111111);

        pose.popPose();

        // ---- Z 层 2: 悬浮按钮（在 scissor 上方，覆盖槽位） ----
        pose.pushPose();
        pose.translate(0, 0, 200);

        // 锁按钮 — 手动渲染 + 手动处理点击
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            var qo = questData.getQuest(i);
            boolean hasQuest = qo.isPresent();
            boolean done = hasQuest && questData.getSlot(i).isComplete();
            if (!hasQuest || done) continue;

            String label = questData.isSlotLocked(i)
                    ? Component.translatable("quest.improved_original.locked").getString()
                    : Component.translatable("quest.improved_original.lock_button").getString() + " " + lockCost + "绿宝石";
            int btnW = questData.isSlotLocked(i) ? 40 : 66;
            int btnX = px + PANEL_W - btnW - 8 - SCROLL_BAR_W;
            int btnY = slotY(i) + slotDisplayH(i) - 20;

            boolean hovered = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + 18;
            int bgColor = hovered ? 0xCC555555 : 0xCC333333;
            int borderColor = hovered ? 0xFFFFFFFF : 0xFF888888;

            g.fill(btnX, btnY, btnX + btnW, btnY + 18, bgColor);
            g.renderOutline(btnX, btnY, btnX + btnW, btnY + 18, borderColor);
            g.drawCenteredString(this.font, Component.literal(label), btnX + btnW / 2, btnY + 5, 0xFFFFFFFF);
        }

        // 底部按钮 — 渲染在槽位分隔线之上
        int footerY = py + ph - FOOTER_H;
        renderButton(g, refreshBtn, mx, my, px + PANEL_W / 2 - 62, footerY + 4, 80, 16);
        renderButton(g, doneBtn, mx, my, px + PANEL_W / 2 + 22, footerY + 4, 40, 16);

        pose.popPose();
    }

    private void renderButton(GuiGraphics g, Button btn, int mx, int my, int bx, int by, int bw, int bh) {
        boolean hovered = btn.active && mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        int bgColor = !btn.active ? 0xCC222222 : hovered ? 0xCC777777 : 0xCC555555;
        int borderColor = !btn.active ? 0xFF444444 : hovered ? 0xFFFFFFFF : 0xFFAAAAAA;
        int textColor = btn.active ? 0xFFFFFFFF : 0xFF666666;

        g.fill(bx, by, bx + bw, by + bh, bgColor);
        g.renderOutline(bx, by, bx + bw, by + bh, borderColor);
        g.drawCenteredString(this.font, btn.getMessage(), bx + bw / 2, by + 4, textColor);
    }

    private void renderSlotContent(GuiGraphics g, int i, int px, int sy) {
        var qo = questData.getQuest(i);
        int h = slotContentH(i);
        int lx = px + 4;
        int rx = px + LEFT_W;

        // 槽位编号
        g.drawCenteredString(this.font, Component.literal(String.valueOf(i + 1)), lx + LEFT_W / 2, sy + h / 2 - 5, 0xFF555555);

        // 左侧分隔竖线
        g.fill(rx, sy + 2, rx + 1, sy + h - 2, 0xFF333333);

        int cx = px + LEFT_W + 6;
        int cw = PANEL_W - LEFT_W - 12 - SCROLL_BAR_W - 4;

        if (qo.isEmpty()) {
            g.drawString(this.font, Component.translatable("quest.improved_original.empty_slot"),
                    cx, sy + h / 2 - 5, 0xFF666666);
            return;
        }

        QuestDefinition q = qo.get();
        QuestSlotData sl = questData.getSlot(i);
        List<Integer> pt = sl.perTargetProgress();
        boolean done = sl.isComplete();

        // 任务名
        Component name = buildName(q);
        g.drawString(this.font, name, cx, sy + 3, done ? 0xFF55FF55 : 0xFFFFFFFF);

        // 锁定状态
        if (questData.isSlotLocked(i)) {
            int nw = this.font.width(name);
            g.drawString(this.font, Component.translatable("quest.improved_original.locked"),
                    cx + nw + 6, sy + 3, 0xFFFF5555);
        }

        // 进度条
        int lineY = sy + 16;
        int barW = Math.min(cw - 4, 150);
        for (int t = 0; t < q.targets().size(); t++) {
            QuestDefinition.QuestTarget tg = q.targets().get(t);
            int prog = t < pt.size() ? pt.get(t) : 0;
            int max = tg.count();
            boolean tc = prog >= max;

            g.drawString(this.font, q.getTargetDisplayName(tg.type(), tg.item()), cx, lineY, 0xFFCCCCCC);

            int bh = 8;
            int by = lineY + this.font.lineHeight + 1;
            g.fill(cx, by, cx + barW, by + bh, 0xFF333333);
            if (max > 0) {
                int fw = (int) ((float) prog / max * barW);
                g.fill(cx, by, cx + fw, by + bh, tc ? 0xFF55FF55 : 0xFF4488FF);
            }

            var p = g.pose();
            p.pushPose();
            p.scale(0.65f, 0.65f, 1);
            g.drawString(this.font, prog + "/" + max,
                    (int) ((cx + barW + 4) / 0.65f), (int) ((by + 1) / 0.65f), 0xFFAAAAAA);
            p.popPose();

            lineY = by + bh + 3;
        }

        // 奖励
        lineY = Math.max(lineY + 1, sy + 16 + q.targets().size() * 16 + 1);
        var rn = q.getRewardDisplayNames();
        for (int r = 0; r < q.rewards().size(); r++) {
            QuestDefinition.ItemCount rw = q.rewards().get(r);
            g.drawString(this.font, Component.translatable("quest.improved_original.reward", rw.count(), rn.get(r)),
                    cx, lineY, done ? 0xFF55FF55 : 0xFFFFAA00);
            lineY += this.font.lineHeight + 2;
        }
    }

    // ---- 鼠标事件 ----

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        int px = panelX();
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            if (maxScroll[i] <= 0) continue;
            int sy = slotY(i);
            int sh = slotDisplayH(i);
            if (mx >= px && mx <= px + PANEL_W && my >= sy && my <= sy + sh) {
                scrollOffset[i] = clamp(scrollOffset[i] - (int) scrollY * 10, 0, maxScroll[i]);
                return true;
            }
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            int px = panelX();

            // 锁按钮点击检测
            int lockCost = Config.EMERALD_LOCK_COST.getAsInt();
            for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
                final int slot = i;
                var qo = questData.getQuest(i);
                boolean hasQuest = qo.isPresent();
                boolean done = hasQuest && questData.getSlot(i).isComplete();
                if (!hasQuest || done) continue;

                int btnW = questData.isSlotLocked(i) ? 40 : 66;
                int btnX = px + PANEL_W - btnW - 8 - SCROLL_BAR_W;
                int btnY = slotY(i) + slotDisplayH(i) - 20;

                if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + 18) {
                    PacketDistributor.sendToServer(new C2SQuestLockPayload(slot));
                    return true;
                }
            }

            // 底部按钮点击检测
            int footerY = panelY() + panelH() - FOOTER_H;
            if (refreshBtn.active && mx >= refreshBtn.getX() && mx <= refreshBtn.getX() + 80
                    && my >= refreshBtn.getY() && my <= refreshBtn.getY() + 16) {
                refreshBtn.onPress();
                return true;
            }
            if (doneBtn.active && mx >= doneBtn.getX() && mx <= doneBtn.getX() + 40
                    && my >= doneBtn.getY() && my <= doneBtn.getY() + 16) {
                doneBtn.onPress();
                return true;
            }
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
