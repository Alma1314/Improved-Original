// JEI recipe category: renders quest target on left, reward on right with quest name/description
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
import net.minecraft.resources.ResourceLocation;
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
                150,
                68
        );
        this.background = guiHelper.createBlankDrawable(150, 68);
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, QuestRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 18)
                .addItemStack(recipe.target())
                .setBackground(slotDrawable, -1, -1)
                .addRichTooltipCallback((slotView, tooltip) -> addQuestTooltip(recipe, tooltip));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 127, 18)
                .addItemStack(recipe.reward())
                .setBackground(slotDrawable, -1, -1)
                .addRichTooltipCallback((slotView, tooltip) -> addQuestTooltip(recipe, tooltip));
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
        String rewardStr = recipe.rewardCountMin() == recipe.rewardCountMax()
                ? String.valueOf(recipe.rewardCountMin())
                : recipe.rewardCountMin() + "-" + recipe.rewardCountMax();
        tooltip.add(Component.translatable("quest.improved_original.reward",
                rewardStr, recipe.reward().getHoverName()).withStyle(ChatFormatting.GREEN));
    }

    @Override
    public void draw(QuestRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;

        // Quest name — centered at top
        Component name;
        if (recipe.nameKey() != null && !recipe.nameKey().isEmpty()) {
            name = Component.translatable(recipe.nameKey()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
        } else {
            name = Component.literal(recipe.type().name());
        }
        int nameWidth = font.width(name);
        guiGraphics.drawString(font, name, (150 - nameWidth) / 2, 0, 0xFFFFFFFF);

        // Target count below left slot
        String countStr = recipe.countMin() == recipe.countMax()
                ? "x" + recipe.countMin()
                : "x" + recipe.countMin() + "-" + recipe.countMax();
        guiGraphics.drawString(font, countStr, 5, 38, ChatFormatting.GRAY.getColor());

        // Reward count below right slot
        String rewardStr = recipe.rewardCountMin() == recipe.rewardCountMax()
                ? "x" + recipe.rewardCountMin()
                : "x" + recipe.rewardCountMin() + "-" + recipe.rewardCountMax();
        guiGraphics.drawString(font, rewardStr, 127, 38, ChatFormatting.GRAY.getColor());

        // Arrow between slots
        guiGraphics.drawString(font, Component.translatable("quest.improved_original.jei.arrow"),
                60, 24, ChatFormatting.GRAY.getColor());

        // Description below the number line (wrapped if needed, max ~140px wide)
        if (recipe.descKey() != null && !recipe.descKey().isEmpty()) {
            Component desc = Component.translatable(recipe.descKey()).withStyle(ChatFormatting.DARK_GRAY);
            guiGraphics.drawWordWrap(font, desc, 5, 52, 140, ChatFormatting.DARK_GRAY.getColor());
        }
    }
}
