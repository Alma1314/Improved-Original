// 世界生成数据提供器：为5种宝石矿石生成 ConfiguredFeature 和 PlacedFeature JSON
// 模仿原版绿宝石矿石生成逻辑：矿脉大小3，Y范围-16~480三角分布偏高处，每区块约100次尝试
// 通过 DatapackBuiltinEntriesProvider 在数据生成时自动输出到 data/improved_original/worldgen/
package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ModWorldGenProvider {

    // ResourceKey构造辅助
    private static ResourceKey<ConfiguredFeature<?, ?>> configuredKey(String gemName) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(ImprovedOriginal.MOD_ID, "ore_" + gemName));
    }

    private static ResourceKey<PlacedFeature> placedKey(String gemName) {
        return ResourceKey.create(Registries.PLACED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(ImprovedOriginal.MOD_ID, "ore_" + gemName));
    }

    // ---- ConfiguredFeature 引导 ----
    // 使用 TagMatchTest 匹配石头/深板岩可替换方块，与所有原版矿石一致
    public static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?, ?>> ctx) {
        var stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        var deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        String[] gems = {"ruby", "sapphire", "topaz", "amethyst", "onyx"};
        for (String gem : gems) {
            // 通过注册表查找对应的方块
            var holderGetter = ctx.lookup(Registries.BLOCK);
            var oreBlock = holderGetter.getOrThrow(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ImprovedOriginal.MOD_ID, gem + "_ore")));
            var deepslateOreBlock = holderGetter.getOrThrow(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ImprovedOriginal.MOD_ID, "deepslate_" + gem + "_ore")));

            List<OreConfiguration.TargetBlockState> targets = List.of(
                    OreConfiguration.target(stoneReplaceables, oreBlock.value().defaultBlockState()),
                    OreConfiguration.target(deepslateReplaceables, deepslateOreBlock.value().defaultBlockState())
            );

            ctx.register(configuredKey(gem),
                    new ConfiguredFeature<>(Feature.ORE,
                            new OreConfiguration(targets, 3)));
        }
    }

    // ---- PlacedFeature 引导 ----
    // 完全模仿原版绿宝石矿石放置：commonOrePlacement(100, triangle(-16, 480))
    // 即每区块约100次尝试（每区块有多个矿脉），高度使用三角分布偏高处
    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> ctx) {
        var configuredLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);

        String[] gems = {"ruby", "sapphire", "topaz", "amethyst", "onyx"};
        for (String gem : gems) {
            var configuredHolder = configuredLookup.getOrThrow(configuredKey(gem));

            PlacementUtils.register(ctx, placedKey(gem), configuredHolder,
                    CountPlacement.of(100),
                    InSquarePlacement.spread(),
                    HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(480)),
                    BiomeFilter.biome());
        }
    }
}
