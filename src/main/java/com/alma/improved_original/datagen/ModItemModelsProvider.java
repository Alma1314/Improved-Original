// 数据生成-物品模型：为5种宝石生成item/generated模型
// basicItem() 为每个物品生成 models/item/<name>.json，使用父模型 minecraft:item/generated
// 贴图路径自动指向 assets/improved_original/textures/item/<name>.png
package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelsProvider extends ItemModelProvider {
    public ModItemModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ImprovedOriginal.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.RUBY.get());
        basicItem(ModItems.SAPPHIRE.get());
        basicItem(ModItems.TOPAZ.get());
        basicItem(ModItems.AMETHYST.get());
        basicItem(ModItems.ONYX.get());
    }
}
