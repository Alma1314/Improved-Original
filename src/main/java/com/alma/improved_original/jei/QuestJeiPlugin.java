// JEI plugin: registers a quest recipe category showing possible quests and their rewards
package com.alma.improved_original.jei;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.datagen.QuestPoolConfig;
import com.alma.improved_original.quest.QuestType;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class QuestJeiPlugin implements IModPlugin {

    public static final RecipeType<QuestRecipe> QUEST_RECIPE_TYPE =
            RecipeType.create(ImprovedOriginal.MOD_ID, "quest", QuestRecipe.class);

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            ImprovedOriginal.MOD_ID, "jei_plugin");

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        LOGGER.info("JEI: Registering quest recipe category");
        registration.addRecipeCategories(new QuestRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Only book as the single entry point — avoids cluttering the JEI sidebar
        registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK,
                new ItemStack(Items.BOOK), QUEST_RECIPE_TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Force-clear cache and reload to ensure latest config is read
        QuestPoolConfig.reloadCache();
        Path configDir = FMLPaths.CONFIGDIR.get();
        LOGGER.info("JEI: Loading quest recipes from {}", configDir);
        List<QuestPoolConfig.PoolEntry> pool = QuestPoolConfig.loadFromConfig(configDir);
        LOGGER.info("JEI: Loaded {} quest pool entries", pool.size());

        List<QuestRecipe> recipes = new ArrayList<>();
        for (QuestPoolConfig.PoolEntry entry : pool) {
            String entryName = entry.name() != null ? entry.name() : "(unnamed)";
            List<ItemStack> targetStacks = new ArrayList<>();
            for (var te : entry.targets()) {
                Item targetItem = getTargetItem(entry.type(), te.item());
                if (targetItem == null) {
                    LOGGER.warn("JEI: [{}] target {} not found in registry (type={})", entryName, te.item(), entry.type());
                    continue;
                }
                targetStacks.add(new ItemStack(targetItem));
            }
            if (targetStacks.isEmpty()) {
                LOGGER.warn("JEI: [{}] ALL {} targets missing — recipe skipped!", entryName, entry.targets().size());
                continue;
            }
            if (targetStacks.size() < entry.targets().size()) {
                LOGGER.warn("JEI: [{}] {}/{} targets resolved", entryName, targetStacks.size(), entry.targets().size());
            }

            List<ItemStack> rewardStacks = new ArrayList<>();
            for (var re : entry.rewards()) {
                Item rewardItem = BuiltInRegistries.ITEM.get(re.item());
                if (rewardItem == Items.AIR) {
                    LOGGER.warn("JEI: [{}] reward {} not found in registry", entryName, re.item());
                    continue;
                }
                rewardStacks.add(new ItemStack(rewardItem, re.countMax()));
            }
            if (rewardStacks.isEmpty()) {
                LOGGER.warn("JEI: [{}] ALL rewards missing — recipe skipped!", entryName);
                continue;
            }

            recipes.add(new QuestRecipe(
                    targetStacks, rewardStacks, entry.type(),
                    entry.name(), entry.description(),
                    entry.targets().stream().map(t -> t.countMin()).toList(),
                    entry.targets().stream().map(t -> t.countMax()).toList(),
                    entry.rewards().stream().map(r -> r.countMin()).toList(),
                    entry.rewards().stream().map(r -> r.countMax()).toList(),
                    entry.weight()
            ));
        }

        LOGGER.info("JEI: Registering {} quest recipes", recipes.size());
        registration.addRecipes(QUEST_RECIPE_TYPE, recipes);
    }

    private static Item getTargetItem(QuestType type, ResourceLocation targetId) {
        return switch (type) {
            case BREAK_BLOCK, CRAFT_ITEM, COLLECT_ITEM -> {
                Item item = BuiltInRegistries.ITEM.get(targetId);
                if (item == null) {
                    LOGGER.warn("JEI: Item not found in registry: {}", targetId);
                }
                yield item;
            }
            case KILL_ENTITY -> {
                var entityType = BuiltInRegistries.ENTITY_TYPE.get(targetId);
                yield entityType != null ? Items.PLAYER_HEAD : null;
            }
            case FIND_STRUCTURE -> Items.COMPASS;
        };
    }
}
