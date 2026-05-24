// JEI recipe category: renders quest targets on left, rewards on right with name/description
package com.alma.improved_original.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class QuestRecipeCategory extends AbstractRecipeCategory<QuestRecipe> {

    private final IDrawable background;
    private final IDrawable slotDrawable;

    public QuestRecipeCategory(IGuiHelper guiHelper) {
        super(
                QuestJeiPlugin.QUEST_RECIPE_TYPE,
                Component.translatable("quest.improved_original.jei.category"),
                guiHelper.createDrawableItemStack(new ItemStack(Items.EMERALD)),
                160,
                80
        );
        this.background = guiHelper.createBlankDrawable(160, 80);
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, QuestRecipe recipe, IFocusGroup focuses) {
        int columnGap = 130;
        int slotSize = 20;

        for (int i = 0; i < recipe.targets().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 18 + i * slotSize)
                    .addItemStack(recipe.targets().get(i))
                    .setBackground(slotDrawable, -1, -1)
                    .addRichTooltipCallback((slotView, tooltip) -> addQuestTooltip(recipe, tooltip));
        }
        for (int i = 0; i < recipe.rewards().size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, columnGap - 5, 18 + i * slotSize)
                    .addItemStack(recipe.rewards().get(i))
                    .setBackground(slotDrawable, -1, -1)
                    .addRichTooltipCallback((slotView, tooltip) -> addQuestTooltip(recipe, tooltip));
        }
    }

    private static void addQuestTooltip(QuestRecipe recipe, mezz.jei.api.gui.builder.ITooltipBuilder tooltip) {
        if (recipe.nameKey() != null && !recipe.nameKey().isEmpty()) {
            tooltip.add(Component.translatable(recipe.nameKey())
                    .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
        }
        if (recipe.descKey() != null && !recipe.descKey().isEmpty()) {
            tooltip.add(Component.translatable(recipe.descKey())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void draw(QuestRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;

        Component name;
        if (recipe.nameKey() != null && !recipe.nameKey().isEmpty()) {
            name = Component.translatable(recipe.nameKey()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
        } else {
            name = Component.literal(recipe.type().name());
        }
        int nameWidth = font.width(name);
        guiGraphics.drawString(font, name, (160 - nameWidth) / 2, 0, 0xFFFFFFFF);

        // Draw arrow — horizontal line + triangle head, centered vertically between slot columns
        int maxRows = Math.max(recipe.targets().size(), recipe.rewards().size());
        int leftSlotRight = 5 + 18;
        int rightSlotLeft = 125;
        int gapWidth = rightSlotLeft - leftSlotRight;
        int arrowCenterY = 18 + maxRows * 10 - 1;

        int lineStartX = leftSlotRight + 2;
        int lineEndX = rightSlotLeft - 8;
        int lineY = arrowCenterY;

        // Horizontal shaft
        guiGraphics.fill(lineStartX, lineY, lineEndX, lineY + 2, 0xFF888888);

        // Arrow head (right-pointing triangle)
        int headSize = 5;
        int headX = lineEndX;
        for (int dy = -headSize; dy <= headSize; dy++) {
            int dx = Math.abs(dy);
            guiGraphics.fill(headX + dx, lineY + 1 + dy, headX + dx + 2, lineY + 1 + dy + 1, 0xFF888888);
        }

        // Description at the bottom
        if (recipe.descKey() != null && !recipe.descKey().isEmpty()) {
            Component desc = Component.translatable(recipe.descKey()).withStyle(ChatFormatting.DARK_GRAY);
            int descY = 18 + maxRows * 20 + 4;
            guiGraphics.drawWordWrap(font, desc, 5, descY, 150, ChatFormatting.DARK_GRAY.getColor());
        }
    }
}
