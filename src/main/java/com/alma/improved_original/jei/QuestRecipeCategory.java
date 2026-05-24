// JEI任务配方类别：目标三行两列 → 奖励三行两列，实心箭头，顶部名称，底部简介
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

    private static final int W = 116;
    private static final int SLOT = 18;
    private static final int GAP = 2;
    private static final int ROWS = 3;
    private static final int COLS = 2;
    private static final int TOP = 22;
    private static final int SLOTS_H = ROWS * (SLOT + GAP);
    private static final int DESC_H = 20;
    private static final int DESC_TOP = TOP + SLOTS_H + 4;
    private static final int H = TOP + SLOTS_H + 6 + DESC_H;

    // 目标列坐标（左半区）
    // 目标区域宽度和奖励区域宽度（各两列占宽）
    private static final int T_ZONE_W = 38; // T_C1 到 T_C2+18 的宽度
    private static final int R_ZONE_W = 38;
    private static final int T_C1 = 1;
    private static final int T_C2 = 21;
    private static final int R_C1 = 77;
    private static final int R_C2 = 97;

    private final IDrawable slotBg;

    public QuestRecipeCategory(IGuiHelper guiHelper) {
        super(QuestJeiPlugin.QUEST_RECIPE_TYPE,
                Component.translatable("screen.improved_original.quest"),
                guiHelper.createDrawableItemStack(new ItemStack(Items.BOOK)),
                W, H);
        this.slotBg = guiHelper.getSlotDrawable();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, QuestRecipe recipe, IFocusGroup focuses) {
        int tCnt = Math.min(recipe.targets().size(), ROWS * COLS);
        int tRows = (tCnt + COLS - 1) / COLS;
        int tCols = tCnt > ROWS ? 2 : 1; // 物品超过3个才用第二列
        int tOffY = (ROWS - tRows) * (SLOT + GAP) / 2;
        int tOffX = (T_ZONE_W - tCols * (SLOT + 2)) / 2; // 水平居中

        for (int i = 0; i < tCnt; i++) {
            int col = i / ROWS;
            int row = i % ROWS;
            builder.addSlot(RecipeIngredientRole.INPUT, T_C1 + tOffX + col * (SLOT + 2), TOP + tOffY + row * (SLOT + GAP))
                    .addItemStack(recipe.targets().get(i))
                    .setBackground(slotBg, -1, -1)
                    .addRichTooltipCallback((sv, tb) -> tooltip(recipe, tb));
        }

        int rCnt = Math.min(recipe.rewards().size(), ROWS * COLS);
        int rRows = (rCnt + COLS - 1) / COLS;
        int rCols = rCnt > ROWS ? 2 : 1;
        int rOffY = (ROWS - rRows) * (SLOT + GAP) / 2;
        int rOffX = (R_ZONE_W - rCols * (SLOT + 2)) / 2;

        for (int i = 0; i < rCnt; i++) {
            int col = i / ROWS;
            int row = i % ROWS;
            builder.addSlot(RecipeIngredientRole.OUTPUT, R_C1 + rOffX + col * (SLOT + 2), TOP + rOffY + row * (SLOT + GAP))
                    .addItemStack(recipe.rewards().get(i))
                    .setBackground(slotBg, -1, -1)
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

        // 名称
        Component name;
        if (recipe.nameKey() != null && !recipe.nameKey().isEmpty())
            name = Component.translatable(recipe.nameKey()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
        else
            name = Component.literal(recipe.type().name());
        g.pose().pushPose();
        g.pose().translate(0, 0, 0);
        g.drawString(f, name, (W - f.width(name)) / 2, 0, 0xFFFFFFFF, false);

        // 列标题
        g.drawString(f, Component.translatable("quest.improved_original.jei.targets_title").withStyle(ChatFormatting.GRAY),
                T_C1, TOP - f.lineHeight - 1, 0xFFAAAAAA, false);
        g.drawString(f, Component.translatable("quest.improved_original.jei.rewards_title").withStyle(ChatFormatting.GRAY),
                R_C1, TOP - f.lineHeight - 1, 0xFFAAAAAA, false);
        g.pose().popPose();

        // 实心箭头：中间区域粗线 + 三角
        int midY = TOP + SLOTS_H / 2;
        g.fill(T_C2 + 18, midY - 2, R_C1 - 2, midY + 2, 0xFF888888);
        for (int d = -4; d <= 4; d++) {
            int w = 4 - Math.abs(d);
            if (w > 0)
                g.fill(R_C1 - 4, midY + d, R_C1 - 4 + w + 2, midY + d + 1, 0xFF888888);
        }

        // 简介
        g.fill(0, DESC_TOP, W, DESC_TOP + DESC_H, 0x15000000);
        g.renderOutline(0, DESC_TOP, W, DESC_H, 0xFF444444);
        if (recipe.descKey() != null && !recipe.descKey().isEmpty()) {
            g.drawWordWrap(f,
                    Component.translatable(recipe.descKey()).withStyle(ChatFormatting.DARK_GRAY),
                    3, DESC_TOP + 2, W - 6, ChatFormatting.DARK_GRAY.getColor());
        } else {
            g.drawString(f,
                    Component.translatable("quest.improved_original.jei.no_description").withStyle(ChatFormatting.ITALIC),
                    3, DESC_TOP + 4, ChatFormatting.GRAY.getColor(), false);
        }
    }
}
