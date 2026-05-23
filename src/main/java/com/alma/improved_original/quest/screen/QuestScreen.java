package com.alma.improved_original.quest.screen;

import com.alma.improved_original.Config;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestDefinition;
import com.alma.improved_original.quest.network.C2SQuestLockPayload;
import com.alma.improved_original.quest.network.C2SQuestRefreshPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

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
            int y = startY + i * 60;

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
            ).bounds(centerX + 100, y + 10, 50, 20).build();

            if (!hasQuest || complete) {
                lockButton.active = false;
            }

            this.addRenderableWidget(lockButton);
        }

        // Manual refresh button
        int refreshCost = Config.EMERALD_REFRESH_COST.getAsInt();
        this.addRenderableWidget(
                Button.builder(
                        Component.translatable("quest.improved_original.refresh_button", refreshCost),
                        btn -> {
                            btn.active = false;
                            PacketDistributor.sendToServer(new C2SQuestRefreshPayload());
                        }
                ).bounds(centerX + 60, startY + QuestData.SLOT_COUNT * 60 + 8, 80, 20).build()
        );

        // Close button
        this.addRenderableWidget(
                Button.builder(Component.translatable("quest.improved_original.done"), btn -> this.onClose())
                        .bounds(centerX - 40, startY + QuestData.SLOT_COUNT * 60 + 8, 40, 20)
                        .build()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = 40;

        // Title
        guiGraphics.drawCenteredString(this.font, this.title, centerX, 15, 0xFFFFFFFF);

        // Countdown timer
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

        for (int i = 0; i < QuestData.SLOT_COUNT; i++) {
            int y = startY + i * 60;
            var questOpt = questData.getQuest(i);

            // Slot background
            guiGraphics.fill(centerX - 110, y - 2, centerX + 150, y + 50, 0x33000000);

            if (questOpt.isEmpty()) {
                guiGraphics.drawString(this.font,
                        Component.translatable("quest.improved_original.empty_slot"),
                        centerX - 100, y + 12, 0xFF888888);
                continue;
            }

            QuestDefinition quest = questOpt.get();
            int progress = questData.getProgress(i);
            int target = quest.targetCount();
            boolean complete = progress >= target;

            // Description
            Component desc = Component.translatable(quest.getDescriptionKey(),
                    quest.getTargetDisplayName(), target);
            guiGraphics.drawString(this.font, desc, centerX - 100, y + 2, 0xFFFFFF);

            // Progress bar
            int barWidth = 100;
            int barHeight = 12;
            int barX = centerX - 100;
            int barY = y + 16;

            guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF444444);
            int fillWidth = (int) ((float) progress / target * barWidth);
            int fillColor = complete ? 0xFF00AA00 : 0xFF4488FF;
            if (fillWidth > 0) {
                guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, fillColor);
            }
            guiGraphics.renderOutline(barX, barY, barWidth, barHeight, 0xFF888888);

            // Progress text
            Component progressText = Component.literal(progress + " / " + target);
            guiGraphics.drawCenteredString(this.font, progressText, barX + barWidth / 2, barY + 2, 0xFFFFFF);

            // Reward
            Component rewardText = Component.translatable("quest.improved_original.reward", quest.rewardEmeralds());
            int rewardColor = complete ? 0xFF55FF55 : 0xFFFFAA00;
            guiGraphics.drawString(this.font, rewardText, centerX - 100, y + 34, rewardColor);

            // Lock cost hint
            if (!questData.isSlotLocked(i) && !complete) {
                int cost = Config.EMERALD_LOCK_COST.getAsInt();
                Component costText = Component.translatable("quest.improved_original.lock_cost_hint", cost);
                guiGraphics.drawString(this.font, costText, centerX + 10, y + 2, 0xFFAAAAAA);
            }
        }
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
