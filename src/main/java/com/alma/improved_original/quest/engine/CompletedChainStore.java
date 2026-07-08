// 已完成链持久化：写入/读取 config/improved_original/completed_chains/<uuid>.json
package com.alma.improved_original.quest.engine;

import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.QuestData.StepInfo;
import com.alma.improved_original.quest.QuestData.StepStatus;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CompletedChainStore {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile Path cachedDir;
    private static final java.util.concurrent.ConcurrentHashMap<UUID, java.util.Set<String>> knownCompletedChains =
            new java.util.concurrent.ConcurrentHashMap<>();
    // Tracks the tail of each player's write chain so writes to the same file are serialized.
    private static final java.util.concurrent.ConcurrentHashMap<UUID, CompletableFuture<Void>> writeChains =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static boolean isAlreadyRecorded(UUID playerUuid, String chainId) {
        java.util.Set<String> set = knownCompletedChains.get(playerUuid);
        return set != null && set.contains(chainId);
    }

    private static void markRecorded(UUID playerUuid, String chainId) {
        knownCompletedChains.computeIfAbsent(playerUuid, k -> java.util.concurrent.ConcurrentHashMap.newKeySet()).add(chainId);
    }

    private static Path getDir() {
        if (cachedDir == null) {
            synchronized (CompletedChainStore.class) {
                if (cachedDir == null) {
                    Path dir = FMLPaths.CONFIGDIR.get().resolve("improved_original").resolve("completed_chains");
                    try {
                        Files.createDirectories(dir);
                    } catch (IOException e) {
                        LOGGER.error("Failed to create completed_chains directory", e);
                    }
                    cachedDir = dir;
                }
            }
        }
        return cachedDir;
    }

    private static Path getFile(UUID playerUuid) {
        return getDir().resolve(playerUuid.toString() + ".json");
    }

    public static void writeCompletedChain(UUID playerUuid, String chainId, QuestData.ChainProgress progress) {
        // Fast-path dedup — if already known-written this session, skip re-dispatching
        if (isAlreadyRecorded(playerUuid, chainId)) return;

        // Phase 1 (server thread): build the JSON entry in-memory
        JsonObject entry = new JsonObject();
        entry.addProperty("chainId", chainId);
        entry.addProperty("completedAt", System.currentTimeMillis());
        entry.addProperty("totalSteps", progress.totalStepCount());
        JsonArray stepsArr = new JsonArray();
        for (var si : progress.steps()) {
            JsonObject step = new JsonObject();
            step.addProperty("questId", si.questId());
            step.addProperty("status", si.status().name());
            stepsArr.add(step);
        }
        entry.add("steps", stepsArr);

        // Phase 2 (async): read-modify-write file off the server thread.
        // Serialize writes per player UUID by chaining onto the tail of that player's write chain,
        // so concurrent completions of different chains cannot race on the same file.
        Path file = getFile(playerUuid);
        writeChains.compute(playerUuid, (k, prev) ->
                (prev == null ? CompletableFuture.<Void>completedFuture(null) : prev).thenRunAsync(() -> {
                    try {
                        JsonObject root;
                        if (Files.exists(file)) {
                            try (Reader reader = new FileReader(file.toFile())) {
                                root = GSON.fromJson(reader, JsonObject.class);
                            }
                        } else {
                            root = new JsonObject();
                            root.add("completedChains", new JsonArray());
                        }

                        JsonArray chains = root.getAsJsonArray("completedChains");
                        // Duplicate check (file may already contain this chain)
                        for (JsonElement elem : chains) {
                            if (elem.getAsJsonObject().get("chainId").getAsString().equals(chainId)) {
                                return;
                            }
                        }
                        chains.add(entry);

                        try (Writer writer = new FileWriter(file.toFile())) {
                            GSON.toJson(root, writer);
                        }
                        // Only mark recorded after the write succeeds so transient I/O errors can retry
                        markRecorded(playerUuid, chainId);
                        LOGGER.info("Saved completed chain '{}' for player {}", chainId, playerUuid);
                    } catch (IOException e) {
                        LOGGER.error("Failed to write completed chain for player {}", playerUuid, e);
                    }
                }));
    }

    public static List<QuestData.ChainProgress> loadCompletedChains(UUID playerUuid) {
        List<QuestData.ChainProgress> result = new ArrayList<>();
        Path file = getFile(playerUuid);
        if (!Files.exists(file)) return result;

        try (Reader reader = new FileReader(file.toFile())) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            JsonArray chains = root.getAsJsonArray("completedChains");
            for (JsonElement elem : chains) {
                JsonObject obj = elem.getAsJsonObject();
                String chainId = obj.get("chainId").getAsString();
                int totalSteps = obj.get("totalSteps").getAsInt();
                JsonArray stepsArr = obj.getAsJsonArray("steps");
                List<StepInfo> steps = new ArrayList<>();
                for (JsonElement se : stepsArr) {
                    JsonObject so = se.getAsJsonObject();
                    steps.add(new StepInfo(
                            so.get("questId").getAsString(),
                            StepStatus.valueOf(so.get("status").getAsString())));
                }
                result.add(new QuestData.ChainProgress(chainId, totalSteps, totalSteps, "", steps));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load completed chains for player {}", playerUuid, e);
        }
        return result;
    }
}
