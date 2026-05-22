package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.block.ModBlocks;
import com.alma.improved_original.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {
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
        oreCooking(
                recipeOutput,
                RecipeSerializer.SMELTING_RECIPE,
                SmeltingRecipe::new,
                ingredients,
                category,
                result,
                experience,
                cookingTime,
                group,
                "_from_smelting");
    }

    protected static void oreBlasting(
            RecipeOutput recipeOutput, List<ItemLike> ingredients, RecipeCategory category,
            ItemLike result, float experience, int cookingTime, String group
    ) {
        oreCooking(
                recipeOutput,
                RecipeSerializer.BLASTING_RECIPE,
                BlastingRecipe::new,
                ingredients,
                category,
                result,
                experience,
                cookingTime,
                group,
                "_from_blasting"
        );
    }

    protected static <T extends AbstractCookingRecipe> void oreCooking(
            RecipeOutput recipeOutput,
            RecipeSerializer<T> serializer,
            AbstractCookingRecipe.Factory<T> recipeFactory,
            List<ItemLike> ingredients,
            RecipeCategory category,
            ItemLike result,
            float experience,
            int cookingTime,
            String group,
            String suffix) {
        for(ItemLike itemlike : ingredients) {
            SimpleCookingRecipeBuilder.generic(
                    Ingredient.of(new ItemLike[]{itemlike}), category, result, experience, cookingTime, serializer, recipeFactory
                    )
                    .group(group)
                    .unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, ImprovedOriginal.MOD_ID + ":" + getItemName(result) + suffix + "_" + getItemName(itemlike)
                    );
        }

    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        oreSmelting(recipeOutput, addSameProduct(ModBlocks.AMETHYST_ORE, ModBlocks.DEEPSLATE_AMETHYST_ORE),
                RecipeCategory.MISC, ModItems.AMETHYST, 0.25f, 200, "gemstone");
        oreBlasting(recipeOutput, addSameProduct(ModBlocks.AMETHYST_ORE, ModBlocks.DEEPSLATE_AMETHYST_ORE),
                RecipeCategory.MISC, ModItems.AMETHYST, 0.25f, 100, "gemstone");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.AMETHYST_BLOCK)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.AMETHYST)
                .unlockedBy(getHasName(ModItems.AMETHYST), has(ModItems.AMETHYST))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.AMETHYST_BLOCK, 9)
                .requires(ModItems.AMETHYST)
                .unlockedBy(getHasName(ModBlocks.AMETHYST_BLOCK), has(ModBlocks.AMETHYST_BLOCK))
                .save(recipeOutput);
    }
}
