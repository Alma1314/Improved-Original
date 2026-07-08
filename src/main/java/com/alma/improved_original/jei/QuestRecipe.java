// JEI任务配方记录：多目标物品列表 -> 多奖励物品列表
// 包含所有JEI渲染所需数据：目标/奖励物品栈、数量范围、类型、翻译键、权重
// targetTypes 与 targets 并行，索引 i 的 type 对应索引 i 的 ItemStack
package com.alma.improved_original.jei;

import com.alma.improved_original.quest.QuestType;
import com.alma.improved_original.quest.component.Rarity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record QuestRecipe(
        List<ItemStack> targets,
        List<ItemStack> rewards,
        List<QuestType> targetTypes,
        String nameKey,
        String descKey,
        List<Integer> countMins,
        List<Integer> countMaxs,
        List<Integer> rewardCountMins,
        List<Integer> rewardCountMaxs,
        int weight,
        Rarity rarity
) {}
