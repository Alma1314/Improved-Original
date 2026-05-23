package com.alma.improved_original.quest.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class QuestToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE =
            ResourceLocation.withDefaultNamespace("toast/advancement");
    private static final int DISPLAY_TIME = 5000; // 5 seconds
    private final Component title;
    private final Component description;

    public QuestToast(Component title, Component description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());

        Font font = toastComponent.getMinecraft().font;
        // Title
        guiGraphics.drawString(font, title, 30, 7, 0xFFFF00);
        // Description
        guiGraphics.drawString(font, description, 30, 18, 0xFFFFFF);

        // Render emerald icon
        guiGraphics.renderFakeItem(new ItemStack(Items.EMERALD), 8, 8);

        return timeSinceLastVisible < DISPLAY_TIME ? Visibility.SHOW : Visibility.HIDE;
    }
}
