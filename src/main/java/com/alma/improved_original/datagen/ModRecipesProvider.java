// 数据生成-配方：5种宝石的烧炼/高炉配方、3x3合成块配方、分解配方
// oreSmelting/oreBlasting 为每种矿石+深层矿石变体生成对应的烧炼和高炉配方
// 使用 GemRecipeData 记录数组统一处理所有宝石，消除每颗宝石重复 20+ 行代码
package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.block.ModBlocks;
import com.alma.improved_original.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {

    // 每种宝石的数据：普通矿石、深层矿石、方块、物品
    private record GemRecipe(DeferredBlock<Block> ore, DeferredBlock<Block> deepslateOre,
                             DeferredBlock<Block> block, DeferredItem<Item> item) {}

    private static final float ORE_XP = 0.25f;
    private static final int SMELTING_TIME = 200;
    private static final int BLASTING_TIME = 100;
    private static final int DECOMPOSE_COUNT = 9;

    private static final GemRecipe[] GEMS = {
        new GemRecipe(ModBlocks.RUBY_ORE, ModBlocks.DEEPSLATE_RUBY_ORE, ModBlocks.RUBY_BLOCK, ModItems.RUBY),
        new GemRecipe(ModBlocks.SAPPHIRE_ORE, ModBlocks.DEEPSLATE_SAPPHIRE_ORE, ModBlocks.SAPPHIRE_BLOCK, ModItems.SAPPHIRE),
        new GemRecipe(ModBlocks.TOPAZ_ORE, ModBlocks.DEEPSLATE_TOPAZ_ORE, ModBlocks.TOPAZ_BLOCK, ModItems.TOPAZ),
        new GemRecipe(ModBlocks.AMETHYST_ORE, ModBlocks.DEEPSLATE_AMETHYST_ORE, ModBlocks.AMETHYST_BLOCK, ModItems.AMETHYST),
        new GemRecipe(ModBlocks.ONYX_ORE, ModBlocks.DEEPSLATE_ONYX_ORE, ModBlocks.ONYX_BLOCK, ModItems.ONYX),
    };

    public ModRecipesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @SafeVarargs
    public static List<ItemLike> addSameProduct(DeferredBlock<Block>... inputs) {
        List<ItemLike> group = new ArrayList<>();
        for (DeferredBlock<Block> block : inputs) {
            if (block != null) {
                group.add(block.get());
            }
        }
        return group;
    }

    protected static void oreSmelting(
            RecipeOutput recipeOutput, List<ItemLike> ingredients, RecipeCategory category,
            ItemLike result, float experience, int cookingTime, String group
    ) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new,
                ingredients, category, result, experience, cookingTime, group, "_from_smelting");
    }

    protected static void oreBlasting(
            RecipeOutput recipeOutput, List<ItemLike> ingredients, RecipeCategory category,
            ItemLike result, float experience, int cookingTime, String group
    ) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new,
                ingredients, category, result, experience, cookingTime, group, "_from_blasting");
    }

    protected static <T extends AbstractCookingRecipe> void oreCooking(
            RecipeOutput recipeOutput, RecipeSerializer<T> serializer,
            AbstractCookingRecipe.Factory<T> recipeFactory, List<ItemLike> ingredients,
            RecipeCategory category, ItemLike result, float experience, int cookingTime,
            String group, String suffix) {
        for (ItemLike itemlike : ingredients) {
            SimpleCookingRecipeBuilder.generic(
                    Ingredient.of(itemlike), category, result, experience, cookingTime, serializer, recipeFactory)
                    .group(group)
                    .unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, ImprovedOriginal.MOD_ID + ":" + getItemName(result) + suffix + "_" + getItemName(itemlike));
        }
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        for (GemRecipe gem : GEMS) {
            List<ItemLike> ores = addSameProduct(gem.ore, gem.deepslateOre);
            ItemLike item = gem.item.get();

            // 烧炼 + 高炉
            oreSmelting(recipeOutput, ores, RecipeCategory.MISC, item, ORE_XP, SMELTING_TIME, "gemstone");
            oreBlasting(recipeOutput, ores, RecipeCategory.MISC, item, ORE_XP, BLASTING_TIME, "gemstone");

            // 3x3 合成块
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, gem.block)
                    .pattern("###")
                    .pattern("###")
                    .pattern("###")
                    .define('#', item)
                    .unlockedBy(getHasName(item), has(item))
                    .save(recipeOutput);

            // 分解（9:1）
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item, DECOMPOSE_COUNT)
                    .requires(gem.block)
                    .unlockedBy(getHasName(gem.block), has(gem.block))
                    .save(recipeOutput, ImprovedOriginal.MOD_ID + ":" + getItemName(item) + "_from_block");
        }
    }
}
