// JEI任务配方类别：左侧提交物品区域（可滚动），右侧奖励物品区域（可滚动），顶部名称，底部简介
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

import java.util.List;

public class QuestRecipeCategory extends AbstractRecipeCategory<QuestRecipe> {

    private static final int WIDTH = 240;
    // 固定高度：名称行 + 标签行 + 最大6行槽位 + 箭头 + 简介区域
    private static final int MAX_SLOT_ROWS = 6;
    private static final int SLOT_SIZE = 20;
    private static final int SLOTS_TOP = 30;
    private static final int DESC_TOP = SLOTS_TOP + MAX_SLOT_ROWS * SLOT_SIZE + 8;
    private static final int DESC_HEIGHT = 38;
    private static final int HEIGHT = DESC_TOP + DESC_HEIGHT;

    // 两列布局：左列(x=5)放目标，右列(x=127)放奖励，中间区域(x=26~124)留给箭头
    private static final int LEFT_COL_X = 5;
    private static final int RIGHT_COL_X = 127;

    private final IDrawable slotDrawable;

    public QuestRecipeCategory(IGuiHelper guiHelper) {
        super(
                QuestJeiPlugin.QUEST_RECIPE_TYPE,
                Component.translatable("screen.improved_original.quest"),
                guiHelper.createDrawableItemStack(new ItemStack(Items.BOOK)),
                WIDTH,
                HEIGHT
        );
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, QuestRecipe recipe, IFocusGroup focuses) {
        // 可滚动槽位列：目标物品（左侧）
        for (int i = 0; i < recipe.targets().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, LEFT_COL_X, SLOTS_TOP + i * SLOT_SIZE)
                    .addItemStack(recipe.targets().get(i))
                    .setBackground(slotDrawable, -1, -1)
                    .addRichTooltipCallback((slotView, tooltip) -> addQuestTooltip(recipe, tooltip));
        }

        // 可滚动槽位列：奖励物品（右侧）
        for (int i = 0; i < recipe.rewards().size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, RIGHT_COL_X, SLOTS_TOP + i * SLOT_SIZE)
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
        int totalRows = recipe.targets().size() + recipe.rewards().size();
        int maxVisible = MAX_SLOT_ROWS * 2;
        if (totalRows > maxVisible) {
            tooltip.add(Component.translatable("quest.improved_original.jei.scroll_hint")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    @Override
    public void draw(QuestRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;

        // 任务名称（顶部居中）
        Component name;
        if (recipe.nameKey() != null && !recipe.nameKey().isEmpty()) {
            name = Component.translatable(recipe.nameKey()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
        } else {
            name = Component.literal(recipe.type().name());
        }
        int nameWidth = font.width(name);
        guiGraphics.drawString(font, name, (WIDTH - nameWidth) / 2, 0, 0xFFFFFFFF);

        // 列标题：提交物品 | 奖励物品
        guiGraphics.drawString(font,
                Component.translatable("quest.improved_original.jei.targets_title")
                        .withStyle(ChatFormatting.WHITE),
                LEFT_COL_X, SLOTS_TOP - font.lineHeight - 2, 0xFFFFFFFF);
        guiGraphics.drawString(font,
                Component.translatable("quest.improved_original.jei.rewards_title")
                        .withStyle(ChatFormatting.WHITE),
                RIGHT_COL_X, SLOTS_TOP - font.lineHeight - 2, 0xFFFFFFFF);

        // 如果槽位超过可见区域，绘制滚动指示器
        int maxRows = Math.max(recipe.targets().size(), recipe.rewards().size());
        if (maxRows > MAX_SLOT_ROWS) {
            // 左侧滚动条
            int scrollLeftX = LEFT_COL_X + 18;
            drawScrollIndicator(guiGraphics, scrollLeftX, SLOTS_TOP, SLOTS_TOP + MAX_SLOT_ROWS * SLOT_SIZE,
                    recipe.targets().size(), MAX_SLOT_ROWS);
            // 右侧滚动条
            int scrollRightX = RIGHT_COL_X + 18;
            drawScrollIndicator(guiGraphics, scrollRightX, SLOTS_TOP, SLOTS_TOP + MAX_SLOT_ROWS * SLOT_SIZE,
                    recipe.rewards().size(), MAX_SLOT_ROWS);
        }

        // 绘制两列之间的箭头（水平线 + 三角箭头）
        int gapLeft = LEFT_COL_X + 20;
        int gapRight = RIGHT_COL_X - 4;
        int arrowY = SLOTS_TOP + MAX_SLOT_ROWS * SLOT_SIZE / 2;
        guiGraphics.fill(gapLeft, arrowY, gapRight, arrowY + 2, 0xFF888888);
        int headSize = 4;
        for (int dy = -headSize; dy <= headSize; dy++) {
            int dx = Math.abs(dy);
            guiGraphics.fill(gapRight - dx, arrowY + 1 + dy, gapRight - dx + 3, arrowY + 1 + dy + 1, 0xFF888888);
        }

        // 简介区域（带边框和背景）
        guiGraphics.fill(LEFT_COL_X, DESC_TOP, WIDTH - 5, DESC_TOP + DESC_HEIGHT - 4, 0x20000000);
        guiGraphics.renderOutline(LEFT_COL_X, DESC_TOP, WIDTH - 10, DESC_HEIGHT - 4, 0xFF555555);

        if (recipe.descKey() != null && !recipe.descKey().isEmpty()) {
            Component desc = Component.translatable(recipe.descKey()).withStyle(ChatFormatting.DARK_GRAY);
            guiGraphics.drawWordWrap(font, desc, LEFT_COL_X + 3, DESC_TOP + 3,
                    WIDTH - 16, ChatFormatting.GRAY.getColor());
        } else {
            guiGraphics.drawString(font,
                    Component.translatable("quest.improved_original.jei.no_description")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
                    LEFT_COL_X + 3, DESC_TOP + 3, ChatFormatting.GRAY.getColor());
        }
    }

    // 绘制简易滚动条指示器
    private static void drawScrollIndicator(GuiGraphics g, int x, int top, int bottom,
                                            int totalItems, int visibleItems) {
        if (totalItems <= visibleItems) return;
        int trackHeight = bottom - top;
        int thumbHeight = Math.max(4, trackHeight * visibleItems / totalItems);
        int thumbTop = top;
        g.fill(x, thumbTop, x + 2, thumbTop + thumbHeight, 0xFF888888);
        g.renderOutline(x, top, 2, trackHeight, 0xFF444444);
    }
}
