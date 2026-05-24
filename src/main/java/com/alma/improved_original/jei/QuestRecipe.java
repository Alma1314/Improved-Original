// Quest recipe record for JEI: target item -> reward item
package com.alma.improved_original.jei;

import com.alma.improved_original.quest.QuestType;
import net.minecraft.world.item.ItemStack;

public record QuestRecipe(
        ItemStack target,
        ItemStack reward,
        QuestType type,
        String nameKey,
        String descKey,
        int countMin,
        int countMax,
        int rewardCountMin,
        int rewardCountMax,
        int weight
) {}
