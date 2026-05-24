// Quest recipe record for JEI: target items -> reward items
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
