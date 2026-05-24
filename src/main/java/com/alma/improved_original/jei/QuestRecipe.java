// JEI任务配方记录：多目标物品列表 -> 多奖励物品列表
package com.alma.improved_original.jei;

import com.alma.improved_original.quest.QuestType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record QuestRecipe(
        List<ItemStack> targets,
        List<ItemStack> rewards,
        QuestType type,
        String nameKey,
        String descKey,
        List<Integer> countMins,
        List<Integer> countMaxs,
        List<Integer> rewardCountMins,
        List<Integer> rewardCountMaxs,
        int weight
) {}
