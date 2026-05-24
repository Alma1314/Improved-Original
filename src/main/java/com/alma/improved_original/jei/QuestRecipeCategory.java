// JEI任务配方类别：紧凑布局，最多3行2列，名称单行，简介2行，整体适配JEI界面
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

    private static final int W = 146;
    private static final int ROWS = 3;
    private static final int SLOT = 18;
    private static final int GAP = 2;
    private static final int LEFT = 1;
    private static final int RIGHT = 124;
    private static final int TOP = 10;
    private static final int SLOTS_H = ROWS * SLOT + (ROWS - 1) * GAP;
    private static final int DESC_TOP = TOP + SLOTS_H + 4;
    private static final int DESC_H = 24;
    private static final int H = TOP + SLOTS_H + 6 + DESC_H;

    private final IDrawable slotDrawable;

    public QuestRecipeCategory(IGuiHelper guiHelper) {
        super(QuestJeiPlugin.QUEST_RECIPE_TYPE,
                Component.translatable("screen.improved_original.quest"),
                guiHelper.createDrawableItemStack(new ItemStack(Items.BOOK)),
                W, H);
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, QuestRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < Math.min(recipe.targets().size(), ROWS); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, LEFT, TOP + i * (SLOT + GAP))
                    .addItemStack(recipe.targets().get(i))
                    .setBackground(slotDrawable, -1, -1)
                    .addRichTooltipCallback((sv, tb) -> tooltip(recipe, tb));
        }
        for (int i = 0; i < Math.min(recipe.rewards().size(), ROWS); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, RIGHT, TOP + i * (SLOT + GAP))
                    .addItemStack(recipe.rewards().get(i))
                    .setBackground(slotDrawable, -1, -1)
                    .addRichTooltipCallback((sv, tb) -> tooltip(recipe, tb));
        }
    }

    private static void tooltip(QuestRecipe r, mezz.jei.api.gui.builder.ITooltipBuilder tb) {
        if (r.nameKey() != null && !r.nameKey().isEmpty())
            tb.add(Component.translatable(r.nameKey()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
        if (r.descKey() != null && !r.descKey().isEmpty())
            tb.add(Component.translatable(r.descKey()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void draw(QuestRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        var f = Minecraft.getInstance().font;

        // 名称 — 居中单行
        Component name;
        if (recipe.nameKey() != null && !recipe.nameKey().isEmpty())
            name = Component.translatable(recipe.nameKey()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
        else
            name = Component.literal(recipe.type().name());
        g.drawCenteredString(f, name, W / 2, 0, 0xFFFFFFFF);

        // 箭头 — 两列之间粗线 + 三角
        int midY = TOP + SLOTS_H / 2;
        g.fill(LEFT + 19, midY - 2, RIGHT - 10, midY + 2, 0xFF888888);
        for (int d = -4; d <= 4; d++) {
            int dx = Math.abs(d);
            g.fill(RIGHT - dx - 6, midY + d, RIGHT - dx - 4, midY + d + 1, 0xFF888888);
        }

        // 列标题
        g.drawString(f, Component.translatable("quest.improved_original.jei.targets_title").withStyle(ChatFormatting.GRAY),
                LEFT, TOP - 9, 0xFFAAAAAA);
        g.drawString(f, Component.translatable("quest.improved_original.jei.rewards_title").withStyle(ChatFormatting.GRAY),
                RIGHT, TOP - 9, 0xFFAAAAAA);

        // 简介 — 底部框内最多3行自动换行
        g.fill(LEFT - 1, DESC_TOP, W - LEFT + 1, DESC_TOP + DESC_H, 0x15000000);
        g.renderOutline(LEFT - 1, DESC_TOP, W - LEFT + 1, DESC_H, 0xFF444444);
        if (recipe.descKey() != null && !recipe.descKey().isEmpty()) {
            g.drawWordWrap(f,
                    Component.translatable(recipe.descKey()).withStyle(ChatFormatting.DARK_GRAY),
                    LEFT + 2, DESC_TOP + 2, W - 8, ChatFormatting.DARK_GRAY.getColor());
        } else {
            g.drawString(f,
                    Component.translatable("quest.improved_original.jei.no_description").withStyle(ChatFormatting.ITALIC),
                    LEFT + 2, DESC_TOP + 4, ChatFormatting.GRAY.getColor());
        }
    }
}
