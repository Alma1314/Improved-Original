// JEI任务配方类别：左侧显示目标物品，右侧显示奖励物品，顶部任务名称，底部简介
// 支持多目标/多奖励的垂直槽位布局，箭头用像素绘制而非字符
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
                Component.translatable("screen.improved_original.quest"),
                guiHelper.createDrawableItemStack(new ItemStack(Items.BOOK)),
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

        // 绘制箭头：水平线 + 三角箭头，左右留有间隙
        int maxRows = Math.max(recipe.targets().size(), recipe.rewards().size());
        int inputSlotRight = 5 + 18;    // right edge of input slots
        int outputSlotLeft = 127;       // left edge of output slots (builder x=127, slot ~18px wide)
        int gap = 8;                    // gap on each side
        int arrowCenterY = 18 + maxRows * 10 - 1;

        int lineStartX = inputSlotRight + gap;
        int lineEndX = outputSlotLeft - gap;
        int lineY = arrowCenterY;

        // 水平箭头杆（左侧离输入槽有间隙，右侧留空给箭头尖端）
        guiGraphics.fill(lineStartX, lineY, lineEndX, lineY + 2, 0xFF888888);

        // 向右指向的三角箭头尖端（靠近奖励列）
        int headSize = 4;
        for (int dy = -headSize; dy <= headSize; dy++) {
            int dx = Math.abs(dy);
            guiGraphics.fill(lineEndX - dx, lineY + 1 + dy, lineEndX - dx + 3, lineY + 1 + dy + 1, 0xFF888888);
        }

        // 底部显示任务简介（自动换行）
        if (recipe.descKey() != null && !recipe.descKey().isEmpty()) {
            Component desc = Component.translatable(recipe.descKey()).withStyle(ChatFormatting.DARK_GRAY);
            int descY = 18 + maxRows * 20 + 4;
            guiGraphics.drawWordWrap(font, desc, 5, descY, 150, ChatFormatting.DARK_GRAY.getColor());
        }
    }
}
