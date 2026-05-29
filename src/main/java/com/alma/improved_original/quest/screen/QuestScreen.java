// 每日任务主面板
// 布局：上1/5标题+倒计时 | 中3/5=3个任务槽位平分 | 下1/5刷新+关闭按钮
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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class QuestScreen extends Screen {

    private static final int PANEL_W = 256;
    private static final int PANEL_H = 220;
    private static final int HEADER_H = PANEL_H / 5;
    private static final int FOOTER_H = PANEL_H / 5;
    private static final int SLOTS_AREA_H = PANEL_H - HEADER_H - FOOTER_H;
    private static final int SLOT_H = SLOTS_AREA_H / QuestData.SLOT_COUNT;
    private static final int SEP_H = 1;

    private QuestData questData;
    private final int[] scrollOffset = new int[QuestData.SLOT_COUNT];
    private final int[] maxScroll = new int[QuestData.SLOT_COUNT];

    private record LockBtn(int x, int y, int w) {}
    private final LockBtn[] lockBtns = new LockBtn[QuestData.SLOT_COUNT];

    private Button refreshBtn;
    private Button doneBtn;

    public QuestScreen(QuestData questData) {
        super(Component.translatable("screen.improved_original.quest"));
        this.questData = questData;
    }

    private int panelX() { return (this.width - PANEL_W) / 2; }
    private int panelY() { return (this.height - PANEL_H) / 2; }

    private int contentLeft() { return panelX() + PANEL_W * 3 / 4; }
    private int contentRightX() { return contentLeft() + 1; }

    private int slotY(int i) {
        return panelY() + HEADER_H + i * SLOT_H;
    }

    private int contentH(int i) {
        var q = questData.getQuest(i);
        if (q.isEmpty()) return SLOT_H;
        QuestDefinition d = q.get();
        return Math.max(SLOT_H, 16 + d.targets().size() * 14 + d.rewards().size() * 11 + 4);
    }

    @Override
    protected void init() {
        QuestData latest = ClientQuestCache.get();
        if (latest != null) this.questData = latest;

        int px = panelX();

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            maxScroll[i] = Math.max(0, contentH(i) - SLOT_H);
            if (scrollOffset[i] > maxScroll[i]) scrollOffset[i] = maxScroll[i];

            int sy = slotY(i);
            int btnW = Math.max(40, PANEL_W / 4 - 12);
            int btnX = contentRightX() + (PANEL_W / 4 - btnW) / 2;
            int btnY = sy + SLOT_H / 2 - 10;
            lockBtns[i] = new LockBtn(btnX, btnY, btnW);
        }

        int footerY = panelY() + PANEL_H - FOOTER_H;
        int btnY = footerY + (FOOTER_H - 20) / 2;
        int refreshCost = Config.EMERALD_REFRESH_COST.getAsInt();

        refreshBtn = Button.builder(
                Component.translatable("quest.improved_original.refresh_button", refreshCost),
                b -> {
                    b.active = false;
                    PacketDistributor.sendToServer(new C2SQuestRefreshPayload());
                }
        ).bounds(px + PANEL_W / 2 - 62, btnY, 80, 20).build();
        doneBtn = Button.builder(
                Component.translatable("quest.improved_original.done"),
                b -> onClose()
        ).build();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // 模糊背景 — 最下层
        this.renderBackground(g, mx, my, pt);

        // 完全手绘，不调用 super.render() 避免其内部再次 renderBackground
        int px = panelX(), py = panelY();
        var pose = g.pose();

        // 面板背景
        g.fill(px, py, px + PANEL_W, py + PANEL_H, 0xCC000000);

        // 上1/5: 标题 + 倒计时
        g.fill(px, py, px + PANEL_W, py + HEADER_H, 0xEE111111);
        g.drawCenteredString(this.font, this.title, px + PANEL_W / 2, py + 6, 0xFFFFAA00);
        if (this.minecraft != null && this.minecraft.level != null) {
            long tick = this.minecraft.level.getGameTime();
            long iv = 20L * 60 * Config.QUEST_REFRESH_INTERVAL_MINUTES.getAsInt();
            if (iv <= 0) iv = 1;
            long sec = (iv - tick % iv) / 20;
            g.drawCenteredString(this.font,
                    Component.translatable("quest.improved_original.countdown", sec / 60, sec % 60),
                    px + PANEL_W / 2, py + 20, 0xFFAAAAAA);
        }
        g.fill(px, py + HEADER_H, px + PANEL_W, py + HEADER_H + SEP_H, 0xFF444444);

        // 中3/5: 3个任务槽位
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            int sy = slotY(i);
            int dividerX = contentLeft();

            if (i > 0) g.fill(px, sy, px + PANEL_W, sy + SEP_H, 0xFF444444);

            // 左3/4: 任务内容（scissor裁剪 + 滚动）
            int clipTop = sy + (i > 0 ? SEP_H : 0);
            int clipH = SLOT_H - (i > 0 ? SEP_H : 0);
            g.enableScissor(px + 1, clipTop, dividerX - 1, clipTop + clipH);

            pose.pushPose();
            pose.translate(0, -scrollOffset[i], 0);
            renderSlotContent(g, i, px + 4, clipTop + 2);
            pose.popPose();

            g.disableScissor();

            // 内容/按钮分隔竖线
            g.fill(dividerX, sy, dividerX + 1, sy + SLOT_H, 0xFF333333);

            // 右1/4: 锁定按钮
            var qo = questData.getQuest(i);
            boolean hasQuest = qo.isPresent();
            boolean done = hasQuest && questData.getSlot(i).isComplete();

            if (hasQuest && !done) {
                LockBtn lb = lockBtns[i];
                String label = questData.isSlotLocked(i)
                        ? Component.translatable("quest.improved_original.locked").getString()
                        : Component.translatable("quest.improved_original.lock_button").getString();
                boolean hovered = mx >= lb.x && mx <= lb.x + lb.w && my >= lb.y && my <= lb.y + 20;

                int textColor = hovered ? 0xFFFFAA00 : 0xFFAAAAAA;
                g.drawCenteredString(this.font, Component.literal(label),
                        lb.x + lb.w / 2, lb.y + 6, textColor);
            }
        }

        // 底部区域分隔线 + 背景
        int slotsBottom = panelY() + HEADER_H + SLOTS_AREA_H;
        g.fill(px, slotsBottom, px + PANEL_W, slotsBottom + SEP_H, 0xFF444444);
        int footerY = panelY() + PANEL_H - FOOTER_H;
        g.fill(px, footerY, px + PANEL_W, py + PANEL_H, 0xEE111111);

        // 底部按钮 — 手绘（不依赖 super.render 避免重复 renderBackground）
        renderBottomButton(g, mx, my, refreshBtn, px + PANEL_W / 2 - 62, footerY + (FOOTER_H - 20) / 2, 80, 20);
        renderBottomButton(g, mx, my, doneBtn, px + PANEL_W / 2 + 22, footerY + (FOOTER_H - 20) / 2, 40, 20);
    }

    private void renderBottomButton(GuiGraphics g, int mx, int my, Button btn, int bx, int by, int bw, int bh) {
        boolean hovered = btn.active && mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        int bgColor = !btn.active ? 0xFF555555 : hovered ? 0xFF777777 : 0xFF555555;
        g.fill(bx, by, bx + bw, by + bh, bgColor);
        int textColor = !btn.active ? 0xFF777777 : hovered ? 0xFFFFCC00 : 0xFFCCCCCC;
        g.drawCenteredString(this.font, btn.getMessage(), bx + bw / 2, by + (bh - 8) / 2, textColor);
    }

    private void renderSlotContent(GuiGraphics g, int i, int cx, int sy) {
        var qo = questData.getQuest(i);

        if (qo.isEmpty()) {
            g.drawString(this.font, Component.translatable("quest.improved_original.empty_slot"),
                    cx, sy + SLOT_H / 2 - 5, 0xFF666666);
            return;
        }

        QuestDefinition q = qo.get();
        QuestSlotData sl = questData.getSlot(i);
        List<Integer> pt = sl.perTargetProgress();
        boolean done = sl.isComplete();

        Component name = buildName(q);
        int color = done ? 0xFF55FF55 : 0xFFFFFFFF;
        String prefix = "[" + (i + 1) + "] ";
        g.drawString(this.font, prefix, cx, sy, 0xFF555555);
        g.drawString(this.font, name, cx + this.font.width(prefix) + 2, sy, color);

        if (questData.isSlotLocked(i)) {
            int afterName = cx + this.font.width(prefix) + 2 + this.font.width(name) + 4;
            g.drawString(this.font, Component.translatable("quest.improved_original.locked"),
                    afterName, sy, 0xFFFF5555);
        }

        int lineY = sy + 11;
        int cw = PANEL_W * 3 / 4 - 10;
        int barW = Math.min(cw, 120);
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

        lineY = Math.max(lineY + 1, sy + 12 + q.targets().size() * 12);
        var rn = q.getRewardDisplayNames();
        for (int r = 0; r < q.rewards().size(); r++) {
            QuestDefinition.ItemCount rw = q.rewards().get(r);
            g.drawString(this.font, Component.translatable("quest.improved_original.reward", rw.count(), rn.get(r)),
                    cx, lineY, done ? 0xFF55FF55 : 0xFFFFAA00);
            lineY += this.font.lineHeight + 1;
        }

        if (maxScroll[i] > 0) {
            int barX = contentLeft() - 4;
            int barH = Math.max(8, SLOT_H * SLOT_H / contentH(i));
            int slotTop = slotY(i) + (i > 0 ? SEP_H : 0);
            int barY = slotTop + scrollOffset[i] * (SLOT_H - barH) / maxScroll[i];
            g.fill(barX, barY, barX + 2, barY + barH, 0x66AAAAAA);
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        int px = panelX();
        int leftW = PANEL_W * 3 / 4;
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            if (maxScroll[i] <= 0) continue;
            int sy = slotY(i) + (i > 0 ? SEP_H : 0);
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

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            var qo = questData.getQuest(i);
            if (qo.isEmpty() || questData.getSlot(i).isComplete()) continue;
            LockBtn lb = lockBtns[i];
            if (mx >= lb.x && mx <= lb.x + lb.w && my >= lb.y && my <= lb.y + 20) {
                PacketDistributor.sendToServer(new C2SQuestLockPayload(i));
                return true;
            }
        }

        // 底部按钮点击
        if (refreshBtn != null && doneBtn != null) {
            int footerY = panelY() + PANEL_H - FOOTER_H;
            int btnY = footerY + (FOOTER_H - 20) / 2;
            int px = panelX();
            if (refreshBtn.active && mx >= px + PANEL_W / 2 - 62 && mx <= px + PANEL_W / 2 + 18
                    && my >= btnY && my <= btnY + 20) {
                refreshBtn.onPress();
                return true;
            }
            if (doneBtn.active && mx >= px + PANEL_W / 2 + 22 && mx <= px + PANEL_W / 2 + 62
                    && my >= btnY && my <= btnY + 20) {
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
