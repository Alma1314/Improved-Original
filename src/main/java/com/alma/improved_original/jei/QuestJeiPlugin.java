// JEI plugin: registers a quest recipe category showing possible quests and their rewards
package com.alma.improved_original.jei;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.datagen.QuestPoolConfig;
import com.alma.improved_original.quest.QuestType;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class QuestJeiPlugin implements IModPlugin {

    public static final RecipeType<QuestRecipe> QUEST_RECIPE_TYPE =
            RecipeType.create(ImprovedOriginal.MOD_ID, "quest", QuestRecipe.class);

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            ImprovedOriginal.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new QuestRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<QuestPoolConfig.PoolEntry> pool = QuestPoolConfig.loadFromConfig(
                Path.of("config", ImprovedOriginal.MOD_ID));

        List<QuestRecipe> recipes = new ArrayList<>();
        for (QuestPoolConfig.PoolEntry entry : pool) {
            Item targetItem = getTargetItem(entry.type(), entry.target());
            Item rewardItem = BuiltInRegistries.ITEM.get(entry.rewardItem());
            if (targetItem == null || rewardItem == Items.AIR) continue;

            recipes.add(new QuestRecipe(
                    new ItemStack(targetItem),
                    new ItemStack(rewardItem, entry.rewardCountMax()),
                    entry.type(),
                    entry.name(),
                    entry.description(),
                    entry.countMin(),
                    entry.countMax(),
                    entry.rewardCountMin(),
                    entry.rewardCountMax(),
                    entry.weight()
            ));
        }

        registration.addRecipes(QUEST_RECIPE_TYPE, recipes);

        // Also add info ingredients so items show up in JEI with "used in quest" context
        for (QuestRecipe recipe : recipes) {
            registration.addIngredientInfo(recipe.target(), VanillaTypes.ITEM_STACK,
                    Component.translatable("quest.improved_original.jei.quest_target_info"));
        }
    }

    private static Item getTargetItem(QuestType type, ResourceLocation targetId) {
        return switch (type) {
            case BREAK_BLOCK, CRAFT_ITEM, COLLECT_ITEM ->
                    BuiltInRegistries.ITEM.get(targetId);
            case KILL_ENTITY -> {
                var entityType = BuiltInRegistries.ENTITY_TYPE.get(targetId);
                yield entityType != null ? Items.PLAYER_HEAD : null;
            }
            case FIND_STRUCTURE -> Items.COMPASS;
        };
    }
}
