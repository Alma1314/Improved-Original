// 任务面板界面：320x380设计画布，按屏幕高度缩放，init()时从缓存刷新数据
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
    private static final int DW = 320;
    private static final int DH = 400;
    private static final int CX = DW / 2;

    private QuestData questData;
    private float uiScale = 1.0f;
    private int offX, offY, panelW, panelH;

    private int s(int v) { return Math.round(v * uiScale); }

    public QuestScreen(QuestData questData) {
        super(Component.translatable("screen.improved_original.quest"));
        this.questData = questData;
    }

    @Override
    protected void init() {
        // 从缓存取最新数据，解决刷新后UI不更新的问题
        QuestData latest = ClientQuestCache.get();
        if (latest != null) this.questData = latest;

        uiScale = Math.min(1.0f, (float) this.height / DH);
        panelW = s(DW);
        panelH = s(DH);
        offX = (this.width - panelW) / 2;
        offY = (this.height - panelH) / 2;

        // 标题区
        int startY = offY + s(38);

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            final int slot = i;
            int y = startY;
            for (int j = 0; j < i; j++) y += s(slotHeight(j));

            var qo = questData.getQuest(i);
            boolean locked = questData.isSlotLocked(i);
            boolean hasQuest = qo.isPresent();
            boolean complete = hasQuest && questData.getSlot(i).isComplete();

            String key;
            if (!hasQuest || complete) key = "quest.improved_original.lock_button";
            else if (locked) key = "quest.improved_original.locked";
            else key = "quest.improved_original.lock_button";

            Button lb = Button.builder(Component.translatable(key),
                    btn -> PacketDistributor.sendToServer(new C2SQuestLockPayload(slot))
            ).bounds(offX + s(CX + 95), y + s(18), s(50), s(16)).build();

            if (!hasQuest || complete) lb.active = false;
            this.addRenderableWidget(lb);
        }

        int btnY = startY;
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) btnY += s(slotHeight(i));
        btnY += s(8);

        int cost = Config.EMERALD_REFRESH_COST.getAsInt();
        this.addRenderableWidget(Button.builder(
                Component.translatable("quest.improved_original.refresh_button", cost),
                btn -> { btn.active = false; PacketDistributor.sendToServer(new C2SQuestRefreshPayload()); }
        ).bounds(offX + s(CX + 50), btnY, s(80), s(18)).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("quest.improved_original.done"), btn -> this.onClose()
        ).bounds(offX + s(CX - 50), btnY, s(40), s(18)).build());
    }

    private int slotHeight(int i) {
        var q = questData.getQuest(i);
        if (q.isEmpty()) return 80;
        QuestDefinition d = q.get();
        return Math.max(80, 22 + d.targets().size() * 15 + d.rewards().size() * 11 + 10);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g, mx, my, pt);
        g.fill(offX, offY, offX + panelW, offY + panelH, 0xC0101010);
        super.render(g, mx, my, pt);

        // 标题行1：标题居中
        g.drawCenteredString(this.font, this.title, offX + CX, offY + s(8), 0xFFFFFFFF);

        // 标题行2：倒计时居中
        if (this.minecraft != null && this.minecraft.level != null) {
            long tick = this.minecraft.level.getGameTime();
            long iv = 20L * 60 * Config.QUEST_REFRESH_INTERVAL_MINUTES.getAsInt();
            if (iv <= 0) iv = 1;
            long sec = (iv - tick % iv) / 20;
            g.drawCenteredString(this.font,
                    Component.translatable("quest.improved_original.countdown", sec / 60, sec % 60),
                    offX + CX, offY + s(22), 0xFFAAAAAA);
        }

        int y = offY + s(38);
        for (int i = 0; i < QuestData.SLOT_COUNT; i++)
            y = renderSlot(g, i, y, mx, my);
    }

    private int renderSlot(GuiGraphics g, int i, int y, int mx, int my) {
        var qo = questData.getQuest(i);
        int h = s(slotHeight(i));

        // 槽位磨砂背景
        g.fill(offX + s(4), y - 1, offX + s(DW - 4), y + h - 3, 0x33000000);

        if (qo.isEmpty()) {
            g.drawString(this.font, Component.translatable("quest.improved_original.empty_slot"),
                    offX + s(14), y + h / 2 - 5, 0xFF888888);
            return y + h;
        }

        QuestDefinition q = qo.get();
        QuestSlotData sl = questData.getSlot(i);
        List<Integer> pt = sl.perTargetProgress();
        boolean done = sl.isComplete();

        // 任务名称
        Component name = buildName(q);
        g.drawString(this.font, name, offX + s(14), y + s(4), 0xFFFFFF);

        // tooltip
        if (this.font != null) {
            int nw = this.font.width(name);
            int nx = offX + s(14), ny = y + s(4);
            if (mx >= nx && mx <= nx + nw && my >= ny && my <= ny + this.font.lineHeight) {
                String dk = q.description();
                if (dk != null && !dk.isEmpty())
                    g.renderTooltip(this.font, Component.translatable(dk), mx, my);
            }
        }

        int lineY = y + s(18);
        int bx = offX + s(14), bw = s(130);

        // 进度条
        for (int t = 0; t < q.targets().size(); t++) {
            QuestDefinition.QuestTarget tg = q.targets().get(t);
            int prog = t < pt.size() ? pt.get(t) : 0;
            int max = tg.count();
            boolean tc = prog >= max;

            g.drawString(this.font, q.getTargetDisplayName(tg.type(), tg.item()), bx, lineY, 0xFFCCCCCC);

            int bh = Math.max(s(8), 6);
            int by = lineY + this.font.lineHeight;
            g.fill(bx, by, bx + bw, by + bh, 0xFF444444);
            if (max > 0) {
                int fw = (int) ((float) prog / max * bw);
                g.fill(bx, by, bx + fw, by + bh, tc ? 0xFF00AA00 : 0xFF4488FF);
            }
            g.renderOutline(bx, by, bw, bh, 0xFF888888);

            var p = g.pose();
            p.pushPose();
            p.scale(0.7f, 0.7f, 1);
            g.drawCenteredString(this.font, Component.literal(prog + "/" + max),
                    (int) ((bx + bw / 2) / 0.7f), (int) ((by + 1) / 0.7f), 0xFFFFFF);
            p.popPose();

            lineY = by + bh + s(2);
        }

        // 奖励
        lineY = Math.max(lineY + s(1), y + s(18) + q.targets().size() * s(15) + s(2));
        var rn = q.getRewardDisplayNames();
        for (int r = 0; r < q.rewards().size(); r++) {
            QuestDefinition.ItemCount rw = q.rewards().get(r);
            g.drawString(this.font, Component.translatable("quest.improved_original.reward", rw.count(), rn.get(r)),
                    bx, lineY, done ? 0xFF55FF55 : 0xFFFFAA00);
            lineY += this.font.lineHeight + s(1);
        }

        // 锁定消耗提示
        if (!questData.isSlotLocked(i) && !done) {
            g.drawString(this.font, Component.translatable("quest.improved_original.lock_cost_hint",
                    Config.EMERALD_LOCK_COST.getAsInt()), offX + s(130), y + s(4), 0xFFAAAAAA);
        }

        return y + h;
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
