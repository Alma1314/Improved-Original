// JSON任务池配置：加载/保存/生成默认任务条目
// 支持多目标多奖励格式，名称和简介使用翻译键支持多语言
// JSON 构建委托给 QuestsPoolBuilder（消除与 ModQuestPoolProvider 的代码重复）
package com.alma.improved_original.datagen;

import com.alma.improved_original.quest.QuestType;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class QuestPoolConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static List<PoolEntry> cachedPool = null;

    public static void reloadCache() {
        cachedPool = null;
    }

    // 目标条目：类型（必需）、物品ID、数量范围
    public record TargetEntry(QuestType type, ResourceLocation item, int countMin, int countMax) {}
    public record RewardEntry(ResourceLocation item, int countMin, int countMax) {}

    // 池条目：目标/奖励列表，权重，翻译键
    public record PoolEntry(
            List<TargetEntry> targets,
            List<RewardEntry> rewards,
            int weight,
            String name,
            String description
    ) {}

    public static List<PoolEntry> loadFromConfig(Path configDir) {
        LOGGER.info("QuestConfig: loadFromConfig called with {}", configDir);
        Path questsDir = configDir.resolve("quests");
        List<PoolEntry> allEntries = new ArrayList<>();

        try {
            Files.createDirectories(questsDir);
        } catch (IOException e) {
            LOGGER.error("QuestConfig: Failed to create quests directory", e);
            return allEntries;
        }

        File[] files = questsDir.toFile().listFiles(f -> f.getName().endsWith(".json"));
        LOGGER.info("QuestConfig: Found {} json files in {}", files != null ? files.length : 0, questsDir);
        if (files == null || files.length == 0) {
            LOGGER.info("QuestConfig: No files found, generating defaults");
            generateDefaults(questsDir);
            files = questsDir.toFile().listFiles(f -> f.getName().endsWith(".json"));
        }

        if (files != null) {
            allEntries = parseAllFiles(files);
        }

        return allEntries;
    }

    private static List<PoolEntry> parseAllFiles(File[] files) {
        List<PoolEntry> allEntries = new ArrayList<>();
        Gson gson = new Gson();
        int totalParsed = 0;
        int totalSkipped = 0;

        for (File file : files) {
            try (Reader reader = new FileReader(file)) {
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                if (obj.has("entries")) {
                    JsonArray entriesArr = obj.getAsJsonArray("entries");
                    LOGGER.info("QuestConfig: File {} has {} JSON entries", file.getName(), entriesArr.size());
                    for (JsonElement elem : entriesArr) {
                        JsonObject entry = elem.getAsJsonObject();
                        String entryName = entry.has("name") ? entry.get("name").getAsString() : "(unnamed)";

                        if (!entry.has("targets")) {
                            LOGGER.warn("QuestConfig: Skipping entry '{}' — no targets field", entryName);
                            totalSkipped++;
                            continue;
                        }
                        if (!entry.has("rewards")) {
                            LOGGER.warn("QuestConfig: Skipping entry '{}' — no rewards field", entryName);
                            totalSkipped++;
                            continue;
                        }

                        List<TargetEntry> targets = parseTargetEntries(entry.getAsJsonArray("targets"));
                        List<RewardEntry> rewards = parseRewardEntries(entry.getAsJsonArray("rewards"));
                        int weight = entry.has("weight") ? entry.get("weight").getAsInt() : 10;
                        String name = entry.has("name") ? entry.get("name").getAsString() : "";
                        String description = entry.has("description") ? entry.get("description").getAsString() : "";
                        allEntries.add(new PoolEntry(targets, rewards, weight, name, description));
                        totalParsed++;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("QuestConfig: Failed to parse entries in {}", file.getName(), e);
            }
        }
        LOGGER.info("QuestConfig: Parsed {} entries, skipped {}", totalParsed, totalSkipped);
        return allEntries;
    }

    // 解析targets数组，每个target必须有"type"字段
    private static List<TargetEntry> parseTargetEntries(JsonArray arr) {
        List<TargetEntry> result = new ArrayList<>();
        for (JsonElement e : arr) {
            JsonObject o = e.getAsJsonObject();
            ResourceLocation item = ResourceLocation.parse(o.get("item").getAsString());
            int cMin = o.has("countMin") ? o.get("countMin").getAsInt() : 1;
            int cMax = o.has("countMax") ? o.get("countMax").getAsInt() : 1;
            QuestType targetType = QuestType.valueOf(o.get("type").getAsString().toUpperCase());
            result.add(new TargetEntry(targetType, item, cMin, cMax));
        }
        return result;
    }

    private static List<RewardEntry> parseRewardEntries(JsonArray arr) {
        List<RewardEntry> result = new ArrayList<>();
        for (JsonElement e : arr) {
            JsonObject o = e.getAsJsonObject();
            ResourceLocation item = ResourceLocation.parse(o.get("item").getAsString());
            int cMin = o.has("countMin") ? o.get("countMin").getAsInt() : 1;
            int cMax = o.has("countMax") ? o.get("countMax").getAsInt() : 1;
            result.add(new RewardEntry(item, cMin, cMax));
        }
        return result;
    }

    private static void generateDefaults(Path questsDir) {
        Path defaultFile = questsDir.resolve("default_pool.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject root = new JsonObject();
        JsonArray entries = QuestsPoolBuilder.buildDefaultEntries();

        int totalWeight = 0;
        for (JsonElement e : entries) {
            totalWeight += e.getAsJsonObject().get("weight").getAsInt();
        }
        root.addProperty("totalWeight", totalWeight);
        root.add("entries", entries);

        try (Writer writer = new FileWriter(defaultFile.toFile())) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            LOGGER.error("QuestConfig: Failed to write default pool", e);
        }
    }
}
