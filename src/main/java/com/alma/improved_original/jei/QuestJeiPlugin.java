// JEI插件：注册每日任务配方类别，显示所有可能任务的目标物品与奖励关系
// 实现 IModPlugin，使用 @JeiPlugin 自动注册
// 用书作为分类入口图标，避免JEI侧边栏出现大量杂项图标
// 读取 config/improved_original/quests/ 目录下所有JSON作为任务数据源
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
        // 只用书作为分类入口，避免JEI侧边栏出现大量图标
        registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK,
                new ItemStack(Items.BOOK), QUEST_RECIPE_TYPE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Path configDir = FMLPaths.CONFIGDIR.get();
        LOGGER.info("JEI: Loading quest recipes from {}", configDir);
        List<QuestPoolConfig.PoolEntry> pool = QuestPoolConfig.loadFromConfig(configDir);
        LOGGER.info("JEI: Loaded {} quest pool entries", pool.size());

        int MAX_ITEMS = 6; // 三行两列最多6个
        List<QuestRecipe> recipes = new ArrayList<>();
        for (QuestPoolConfig.PoolEntry entry : pool) {
            List<ItemStack> targetStacks = new ArrayList<>();
            List<QuestType> targetTypes = new ArrayList<>();
            List<Integer> tMin = new ArrayList<>();
            List<Integer> tMax = new ArrayList<>();
            int ti = 0;
            for (var te : entry.targets()) {
                if (ti >= MAX_ITEMS) break;
                // 每target自带独立类型
                QuestType tType = te.type();
                Item item = getTargetItem(tType, te.item());
                if (item == null) continue;
                targetStacks.add(new ItemStack(item));
                targetTypes.add(tType);
                tMin.add(te.countMin());
                tMax.add(te.countMax());
                ti++;
            }
            if (targetStacks.isEmpty()) continue;

            List<ItemStack> rewardStacks = new ArrayList<>();
            List<Integer> rMin = new ArrayList<>();
            List<Integer> rMax = new ArrayList<>();
            int ri = 0;
            for (var re : entry.rewards()) {
                if (ri >= MAX_ITEMS) break;
                Item item = BuiltInRegistries.ITEM.get(re.item());
                if (item == Items.AIR) continue;
                rewardStacks.add(new ItemStack(item, re.countMax()));
                rMin.add(re.countMin());
                rMax.add(re.countMax());
                ri++;
            }
            if (rewardStacks.isEmpty()) continue;

            recipes.add(new QuestRecipe(
                    targetStacks, rewardStacks, targetTypes,
                    entry.name(), entry.description(),
                    tMin, tMax, rMin, rMax, entry.weight()
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
