// JEI recipe category: renders quest target on left, reward on right with quest info in center
package com.alma.improved_original.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
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

import java.util.List;

public class QuestRecipeCategory extends AbstractRecipeCategory<QuestRecipe> {

    public static final int WIDTH = 150;
    public static final int HEIGHT = 54;

    private final IDrawable slotDrawable;

    public QuestRecipeCategory(IGuiHelper guiHelper) {
        super(
                QuestJeiPlugin.QUEST_RECIPE_TYPE,
                Component.translatable("quest.improved_original.jei.category"),
                guiHelper.createDrawableItemStack(new ItemStack(Items.EMERALD)),
                WIDTH,
                HEIGHT
        );
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, QuestRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 9, 9);
        inputSlot.addItemStack(recipe.target());
        inputSlot.setBackground(slotDrawable, -1, -1);

        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 123, 9);
        outputSlot.addItemStack(recipe.reward());
        outputSlot.setBackground(slotDrawable, -1, -1);
    }

    @Override
    public void draw(QuestRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;

        guiGraphics.drawString(font, "→", 65, 15, 0xFFAAAAAA);

        Component name;
        if (recipe.nameKey() != null && !recipe.nameKey().isEmpty()) {
            name = Component.translatable(recipe.nameKey()).withStyle(ChatFormatting.BOLD);
        } else {
            name = Component.literal(recipe.type().name()).withStyle(ChatFormatting.BOLD);
        }
        int nameWidth = font.width(name);
        guiGraphics.drawString(font, name,
                (WIDTH - nameWidth) / 2, 0, 0xFFFFFFFF);

        String countText = recipe.countMin() == recipe.countMax()
                ? String.valueOf(recipe.countMin())
                : recipe.countMin() + "-" + recipe.countMax();
        guiGraphics.drawString(font, Component.literal("x" + countText).withStyle(ChatFormatting.GRAY),
                33, 28, 0xFFAAAAAA);

        String rewardText = recipe.rewardCountMin() == recipe.rewardCountMax()
                ? String.valueOf(recipe.rewardCountMin())
                : recipe.rewardCountMin() + "-" + recipe.rewardCountMax();
        guiGraphics.drawString(font, Component.literal("x" + rewardText).withStyle(ChatFormatting.GRAY),
                96, 28, 0xFFAAAAAA);

        guiGraphics.drawString(font,
                Component.translatable("quest.improved_original.jei.weight", recipe.weight())
                        .withStyle(ChatFormatting.DARK_GRAY),
                (WIDTH - font.width("Weight: 00")) / 2, 40, 0xFF666666);
    }

    @SuppressWarnings("removal")
    @Override
    public List<Component> getTooltipStrings(QuestRecipe recipe, IRecipeSlotsView recipeSlotsView,
                                             double mouseX, double mouseY) {
        if (recipe.descKey() != null && !recipe.descKey().isEmpty() && mouseX >= 28 && mouseX <= 122) {
            return List.of(Component.translatable(recipe.descKey()));
        }
        return List.of();
    }
}
