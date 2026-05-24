// Quest screen: 3 quest slots with multi-target progress bars, rewards, lock/refresh buttons
package com.alma.improved_original.quest.screen;

import com.alma.improved_original.Config;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestDefinition;
import com.alma.improved_original.quest.QuestSlotData;
import com.alma.improved_original.quest.network.C2SQuestLockPayload;
import com.alma.improved_original.quest.network.C2SQuestRefreshPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class QuestScreen extends Screen {
    private final QuestData questData;

    public QuestScreen(QuestData questData) {
        super(Component.translatable("screen.improved_original.quest"));
        this.questData = questData;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int startY = 40;

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            final int slot = i;
            int y = startY + i * slotHeight(i);

            var questOpt = questData.getQuest(i);
            boolean locked = questData.isSlotLocked(i);
            boolean hasQuest = questOpt.isPresent();
            boolean complete = hasQuest && questData.getSlot(i).isComplete();

            String lockTextKey;
            if (!hasQuest || complete) {
                lockTextKey = "quest.improved_original.lock_button";
            } else if (locked) {
                lockTextKey = "quest.improved_original.locked";
            } else {
                lockTextKey = "quest.improved_original.lock_button";
            }

            Button lockButton = Button.builder(
                    Component.translatable(lockTextKey),
                    btn -> PacketDistributor.sendToServer(new C2SQuestLockPayload(slot))
            ).bounds(centerX + 100, y + 22, 50, 20).build();

            if (!hasQuest || complete) {
                lockButton.active = false;
            }

            this.addRenderableWidget(lockButton);
        }

        int buttonY = startY;
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            buttonY += slotHeight(i);
        }

        int refreshCost = Config.EMERALD_REFRESH_COST.getAsInt();
        this.addRenderableWidget(
                Button.builder(
                        Component.translatable("quest.improved_original.refresh_button", refreshCost),
                        btn -> {
                            btn.active = false;
                            PacketDistributor.sendToServer(new C2SQuestRefreshPayload());
                        }
                ).bounds(centerX + 60, buttonY + 8, 80, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("quest.improved_original.done"), btn -> this.onClose())
                        .bounds(centerX - 40, buttonY + 8, 40, 20)
                        .build()
        );
    }

    /** Compute slot height based on number of targets and rewards */
    private int slotHeight(int index) {
        var questOpt = questData.getQuest(index);
        if (questOpt.isEmpty()) return 70;
        QuestDefinition quest = questOpt.get();
        // target lines at 12px each + reward lines at 10px each + name/desc bar/reward header
        return Math.max(70, 16 + quest.targets().size() * 14 + quest.rewards().size() * 10 + 14);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = 40;

        guiGraphics.drawCenteredString(this.font, this.title, centerX, 15, 0xFFFFFFFF);

        if (this.minecraft != null && this.minecraft.level != null) {
            long currentTick = this.minecraft.level.getGameTime();
            long intervalTicks = 20L * 60 * Config.QUEST_REFRESH_INTERVAL_MINUTES.getAsInt();
            if (intervalTicks <= 0) intervalTicks = 1;
            long elapsed = currentTick % intervalTicks;
            long remainingTicks = intervalTicks - elapsed;
            long remainingSeconds = remainingTicks / 20;
            long minutes = remainingSeconds / 60;
            long seconds = remainingSeconds % 60;
            Component countdown = Component.translatable("quest.improved_original.countdown",
                    minutes, seconds);
            guiGraphics.drawCenteredString(this.font, countdown, centerX + 80, 15, 0xFFAAAAAA);
        }

        int y = startY;
        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            y = renderSlot(guiGraphics, i, centerX, y, mouseX, mouseY);
        }
    }

    private int renderSlot(GuiGraphics guiGraphics, int i, int centerX, int y, int mouseX, int mouseY) {
        var questOpt = questData.getQuest(i);
        int height = slotHeight(i);

        guiGraphics.fill(centerX - 110, y - 2, centerX + 150, y + height - 4, 0x33000000);

        if (questOpt.isEmpty()) {
            guiGraphics.drawString(this.font,
                    Component.translatable("quest.improved_original.empty_slot"),
                    centerX - 100, y + height / 2 - 6, 0xFF888888);
            return y + height;
        }

        QuestDefinition quest = questOpt.get();
        QuestSlotData slot = questData.getSlot(i);
        List<Integer> perTarget = slot.perTargetProgress();
        boolean complete = slot.isComplete();

        // Quest name
        Component nameText;
        String nameKey = quest.name();
        if (nameKey != null && !nameKey.isEmpty()) {
            nameText = Component.translatable(nameKey);
        } else {
            nameText = Component.translatable(quest.getDescriptionKey(),
                    quest.getTargetDisplayNames().get(0), quest.targets().get(0).count());
        }
        guiGraphics.drawString(this.font, nameText, centerX - 100, y + 2, 0xFFFFFF);

        if (this.font != null) {
            int nameWidth = this.font.width(nameText);
            if (mouseX >= centerX - 100 && mouseX <= centerX - 100 + nameWidth
                    && mouseY >= y + 2 && mouseY <= y + 2 + this.font.lineHeight) {
                String descKey = quest.description();
                if (descKey != null && !descKey.isEmpty()) {
                    guiGraphics.renderTooltip(this.font,
                            Component.translatable(descKey), mouseX, mouseY);
                }
            }
        }

        int lineY = y + 14;
        int barX = centerX - 100;
        int barWidth = 100;

        // Per-target progress bars
        for (int t = 0; t < quest.targets().size(); t++) {
            QuestDefinition.ItemCount target = quest.targets().get(t);
            int prog = t < perTarget.size() ? perTarget.get(t) : 0;
            int max = target.count();
            boolean thisComplete = prog >= max;

            Component targetName = quest.getTargetDisplayName(target.item());
            guiGraphics.drawString(this.font, targetName, centerX - 100, lineY, 0xFFCCCCCC);

            int barHeight = 10;
            int barY = lineY + this.font.lineHeight;
            guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF444444);
            int fillWidth = max > 0 ? (int) ((float) prog / max * barWidth) : 0;
            int fillColor = thisComplete ? 0xFF00AA00 : 0xFF4488FF;
            if (fillWidth > 0) {
                guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, fillColor);
            }
            guiGraphics.renderOutline(barX, barY, barWidth, barHeight, 0xFF888888);

            Component progressText = Component.literal(prog + " / " + max);
            float scale = 0.8f;
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.scale(scale, scale, 1);
            guiGraphics.drawCenteredString(this.font, progressText,
                    (int)((barX + barWidth / 2) / scale), (int)((barY + 1) / scale), 0xFFFFFF);
            pose.popPose();

            lineY = barY + barHeight + 4;
        }

        // Rewards
        lineY = Math.max(lineY + 2, y + 14 + quest.targets().size() * 14 + 4);
        var rewardNames = quest.getRewardDisplayNames();
        for (int r = 0; r < quest.rewards().size(); r++) {
            QuestDefinition.ItemCount reward = quest.rewards().get(r);
            Component rewardText = Component.translatable("quest.improved_original.reward",
                    reward.count(), rewardNames.get(r));
            int rewardColor = complete ? 0xFF55FF55 : 0xFFFFAA00;
            guiGraphics.drawString(this.font, rewardText, centerX - 100, lineY, rewardColor);
            lineY += this.font.lineHeight + 2;
        }

        // Lock cost hint
        if (!questData.isSlotLocked(i) && !complete) {
            int cost = Config.EMERALD_LOCK_COST.getAsInt();
            Component costText = Component.translatable("quest.improved_original.lock_cost_hint", cost);
            guiGraphics.drawString(this.font, costText, centerX + 10, y + 2, 0xFFAAAAAA);
        }

        return y + height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }
}
