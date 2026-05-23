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

        oreSmelting(recipeOutput, addSameProduct(ModBlocks.RUBY_ORE, ModBlocks.DEEPSLATE_RUBY_ORE),
                RecipeCategory.MISC, ModItems.RUBY, 0.25f, 200, "gemstone");
        oreBlasting(recipeOutput, addSameProduct(ModBlocks.RUBY_ORE, ModBlocks.DEEPSLATE_RUBY_ORE),
                RecipeCategory.MISC, ModItems.RUBY, 0.25f, 100, "gemstone");

        oreSmelting(recipeOutput, addSameProduct(ModBlocks.SAPPHIRE_ORE, ModBlocks.DEEPSLATE_SAPPHIRE_ORE),
                RecipeCategory.MISC, ModItems.SAPPHIRE, 0.25f, 200, "gemstone");
        oreBlasting(recipeOutput, addSameProduct(ModBlocks.SAPPHIRE_ORE, ModBlocks.DEEPSLATE_SAPPHIRE_ORE),
                RecipeCategory.MISC, ModItems.SAPPHIRE, 0.25f, 100, "gemstone");

        oreSmelting(recipeOutput, addSameProduct(ModBlocks.TOPAZ_ORE, ModBlocks.DEEPSLATE_TOPAZ_ORE),
                RecipeCategory.MISC, ModItems.TOPAZ, 0.25f, 200, "gemstone");
        oreBlasting(recipeOutput, addSameProduct(ModBlocks.TOPAZ_ORE, ModBlocks.DEEPSLATE_TOPAZ_ORE),
                RecipeCategory.MISC, ModItems.TOPAZ, 0.25f, 100, "gemstone");

        oreSmelting(recipeOutput, addSameProduct(ModBlocks.ONYX_ORE, ModBlocks.DEEPSLATE_ONYX_ORE),
                RecipeCategory.MISC, ModItems.ONYX, 0.25f, 200, "gemstone");
        oreBlasting(recipeOutput, addSameProduct(ModBlocks.ONYX_ORE, ModBlocks.DEEPSLATE_ONYX_ORE),
                RecipeCategory.MISC, ModItems.ONYX, 0.25f, 100, "gemstone");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RUBY_BLOCK)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.RUBY)
                .unlockedBy(getHasName(ModItems.RUBY), has(ModItems.RUBY))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RUBY, 9)
                .requires(ModBlocks.RUBY_BLOCK)
                .unlockedBy(getHasName(ModBlocks.RUBY_BLOCK), has(ModBlocks.RUBY_BLOCK))
                .save(recipeOutput, ImprovedOriginal.MOD_ID + ":ruby_from_block");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SAPPHIRE_BLOCK)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.SAPPHIRE)
                .unlockedBy(getHasName(ModItems.SAPPHIRE), has(ModItems.SAPPHIRE))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SAPPHIRE, 9)
                .requires(ModBlocks.SAPPHIRE_BLOCK)
                .unlockedBy(getHasName(ModBlocks.SAPPHIRE_BLOCK), has(ModBlocks.SAPPHIRE_BLOCK))
                .save(recipeOutput, ImprovedOriginal.MOD_ID + ":sapphire_from_block");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TOPAZ_BLOCK)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.TOPAZ)
                .unlockedBy(getHasName(ModItems.TOPAZ), has(ModItems.TOPAZ))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.TOPAZ, 9)
                .requires(ModBlocks.TOPAZ_BLOCK)
                .unlockedBy(getHasName(ModBlocks.TOPAZ_BLOCK), has(ModBlocks.TOPAZ_BLOCK))
                .save(recipeOutput, ImprovedOriginal.MOD_ID + ":topaz_from_block");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.AMETHYST_BLOCK)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.AMETHYST)
                .unlockedBy(getHasName(ModItems.AMETHYST), has(ModItems.AMETHYST))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.AMETHYST, 9)
                .requires(ModBlocks.AMETHYST_BLOCK)
                .unlockedBy(getHasName(ModBlocks.AMETHYST_BLOCK), has(ModBlocks.AMETHYST_BLOCK))
                .save(recipeOutput, ImprovedOriginal.MOD_ID + ":amethyst_from_block");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ONYX_BLOCK)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.ONYX)
                .unlockedBy(getHasName(ModItems.ONYX), has(ModItems.ONYX))
                .save(recipeOutput);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ONYX, 9)
                .requires(ModBlocks.ONYX_BLOCK)
                .unlockedBy(getHasName(ModBlocks.ONYX_BLOCK), has(ModBlocks.ONYX_BLOCK))
                .save(recipeOutput, ImprovedOriginal.MOD_ID + ":onyx_from_block");
    }
}
