// JEI任务配方类别：左侧目标物品列 → 右侧奖励物品列，顶部名称，底部可滚动简介
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

    private static final int WIDTH = 160;
    private static final int MAX_SLOT_ROWS = 6;
    private static final int SLOT_SIZE = 20;
    private static final int TOP_Y = 14;
    private static final int INPUT_X = 1;
    private static final int OUTPUT_X = 137;
    private static final int DESC_Y = TOP_Y + MAX_SLOT_ROWS * SLOT_SIZE + 4;
    private static final int DESC_H = 22;
    private static final int HEIGHT = DESC_Y + DESC_H;

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
        for (int i = 0; i < recipe.targets().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, TOP_Y + i * SLOT_SIZE)
                    .addItemStack(recipe.targets().get(i))
                    .setBackground(slotDrawable, -1, -1)
                    .addRichTooltipCallback((sv, tb) -> addTooltip(recipe, tb));
        }
        for (int i = 0; i < recipe.rewards().size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, TOP_Y + i * SLOT_SIZE)
                    .addItemStack(recipe.rewards().get(i))
                    .setBackground(slotDrawable, -1, -1)
                    .addRichTooltipCallback((sv, tb) -> addTooltip(recipe, tb));
        }
    }

    private static void addTooltip(QuestRecipe r, mezz.jei.api.gui.builder.ITooltipBuilder tb) {
        if (r.nameKey() != null && !r.nameKey().isEmpty())
            tb.add(Component.translatable(r.nameKey()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
        if (r.descKey() != null && !r.descKey().isEmpty())
            tb.add(Component.translatable(r.descKey()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void draw(QuestRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        var f = Minecraft.getInstance().font;

        // 任务名称 — 居中
        Component name;
        if (recipe.nameKey() != null && !recipe.nameKey().isEmpty())
            name = Component.translatable(recipe.nameKey()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
        else
            name = Component.literal(recipe.type().name());
        g.drawCenteredString(f, name, WIDTH / 2, 0, 0xFFFFFFFF);

        // 箭头 — 两列之间
        int rows = Math.max(recipe.targets().size(), recipe.rewards().size());
        int midY = TOP_Y + rows * SLOT_SIZE / 2;
        int arrowX = INPUT_X + 19;
        int arrowEndX = OUTPUT_X - 2;
        g.fill(arrowX, midY - 5, arrowX + 1, midY + 5, 0xFF888888);
        for (int d = -3; d <= 3; d++) {
            int dx = Math.abs(d);
            g.fill(arrowEndX - dx, midY + d, arrowEndX - dx + 2, midY + d + 1, 0xFF888888);
        }

        // 列标题
        g.drawString(f, Component.translatable("quest.improved_original.jei.targets_title").withStyle(ChatFormatting.GRAY),
                INPUT_X + 1, TOP_Y - 11, 0xFFAAAAAA);
        g.drawString(f, Component.translatable("quest.improved_original.jei.rewards_title").withStyle(ChatFormatting.GRAY),
                OUTPUT_X + 1, TOP_Y - 11, 0xFFAAAAAA);

        // 简介 — 底部框定区域，自动换行，超出区域由JEI页面级滚动支持
        g.fill(INPUT_X - 1, DESC_Y, WIDTH - INPUT_X + 1, DESC_Y + DESC_H, 0x18000000);
        g.renderOutline(INPUT_X - 1, DESC_Y, WIDTH - INPUT_X + 1, DESC_H, 0xFF444444);

        if (recipe.descKey() != null && !recipe.descKey().isEmpty()) {
            Component desc = Component.translatable(recipe.descKey()).withStyle(ChatFormatting.DARK_GRAY);
            g.drawWordWrap(f, desc, INPUT_X + 2, DESC_Y + 2, WIDTH - 8, ChatFormatting.DARK_GRAY.getColor());
        } else {
            g.drawString(f,
                    Component.translatable("quest.improved_original.jei.no_description").withStyle(ChatFormatting.ITALIC),
                    INPUT_X + 2, DESC_Y + 4, ChatFormatting.GRAY.getColor());
        }
    }
}
