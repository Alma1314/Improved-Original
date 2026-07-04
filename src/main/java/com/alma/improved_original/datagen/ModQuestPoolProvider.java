// 数据生成-任务池：生成默认任务池JSON到 generated/resources/data/improved_original/quest_pool/
// buildDefaultEntries 包含五种任务类型的预设任务和宝石兑换钻石的特殊任务
// 输出为 data/improved_original/quest_pool/default.json，运行时由 QuestPoolConfig 加载
// buildDefaultEntries() 和 JSON 构建辅助方法抽取到 QuestsPoolBuilder，与 QuestPoolConfig 共享
package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ModQuestPoolProvider implements DataProvider {

    private final PackOutput packOutput;

    public ModQuestPoolProvider(PackOutput packOutput) {
        this.packOutput = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        JsonObject root = new JsonObject();
        JsonArray entries = QuestsPoolBuilder.buildDefaultEntries();
        int totalWeight = 0;
        for (JsonElement e : entries) {
            totalWeight += e.getAsJsonObject().get("weight").getAsInt();
        }
        root.addProperty("totalWeight", totalWeight);
        root.add("entries", entries);

        Path outPath = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(ImprovedOriginal.MOD_ID)
                .resolve("quest_pool")
                .resolve("default.json");

        return DataProvider.saveStable(cachedOutput, new GsonBuilder().setPrettyPrinting().create().toJsonTree(root), outPath);
    }

    @Override
    public String getName() {
        return "Quest Pool: " + ImprovedOriginal.MOD_ID;
    }
}
