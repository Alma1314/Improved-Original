# Quest System Efficiency Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate server-thread blocking I/O, reduce per-tick overhead, and trim allocations in the quest system.

**Architecture:** Five independent optimization tasks, each touching 1-3 files. Tasks 1-3 eliminate main-thread blocking (async I/O, cache, primitive collections). Tasks 4-5 reduce per-tick overhead (dirty tracking, tick optimization). Task 6 (incremental sync) is deferred.

**Tech Stack:** Java 21, NeoForge 1.21.1, fastutil (bundled with Minecraft), Gson, CompletableFuture

## Global Constraints

- Target branch: `1.21.1-neoforge`
- All quest engine API surfaces remain unchanged
- fastutil is already a Minecraft dependency — no new deps
- Build must pass: `./gradlew build`
- No new config keys or translation keys

---

### Task 1: CompletedChainStore Async Write

**Files:**
- Modify: `src/main/java/com/alma/improved_original/quest/engine/CompletedChainStore.java`

**Interfaces:**
- Consumes: `QuestData.ChainProgress` (existing record)
- Produces: `CompletedChainStore.writeCompletedChain(UUID, String, ChainProgress)` — same public signature, now async internally

- [ ] **Step 1: Cache directory path**

Replace `getDir()` with a lazy-initialized cached version. In `CompletedChainStore.java`, replace:

```java
private static Path getDir() {
    Path dir = FMLPaths.CONFIGDIR.get().resolve("improved_original").resolve("completed_chains");
    try {
        Files.createDirectories(dir);
    } catch (IOException e) {
        LOGGER.error("Failed to create completed_chains directory", e);
    }
    return dir;
}
```

With:

```java
private static volatile Path cachedDir;

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
```

- [ ] **Step 2: Add in-memory dedup set**

Add the field and a method in `CompletedChainStore.java`:

```java
private static final java.util.concurrent.ConcurrentHashMap<UUID, java.util.Set<String>> knownCompletedChains =
        new java.util.concurrent.ConcurrentHashMap<>();

private static boolean isAlreadyRecorded(UUID playerUuid, String chainId) {
    java.util.Set<String> set = knownCompletedChains.get(playerUuid);
    return set != null && set.contains(chainId);
}

private static void markRecorded(UUID playerUuid, String chainId) {
    knownCompletedChains.computeIfAbsent(playerUuid, k -> java.util.concurrent.ConcurrentHashMap.newKeySet()).add(chainId);
}
```

- [ ] **Step 3: Refactor writeCompletedChain to async**

Replace the existing `writeCompletedChain` method with the split-phased version:

```java
public static void writeCompletedChain(UUID playerUuid, String chainId, QuestData.ChainProgress progress) {
    // Dedup check — if already recorded, skip entirely
    if (isAlreadyRecorded(playerUuid, chainId)) return;
    markRecorded(playerUuid, chainId);

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

    // Phase 2 (async): read-modify-write file off the server thread
    Path file = getFile(playerUuid);
    CompletableFuture.runAsync(() -> {
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
            // Duplicate check (file may have been written by another async task)
            for (JsonElement elem : chains) {
                if (elem.getAsJsonObject().get("chainId").getAsString().equals(chainId)) {
                    return;
                }
            }
            chains.add(entry);

            try (Writer writer = new FileWriter(file.toFile())) {
                GSON.toJson(root, writer);
            }
            LOGGER.info("Saved completed chain '{}' for player {}", chainId, playerUuid);
        } catch (IOException e) {
            LOGGER.error("Failed to write completed chain for player {}", playerUuid, e);
        }
    });
}
```

Add the import at the top:

```java
import java.util.concurrent.CompletableFuture;
```

- [ ] **Step 4: Remove the unused Gson instance field**

The field `private static final Gson GSON = ...` is only used in async now, but still valid. Add `import java.io.Writer;` if missing. Check all imports compile.

- [ ] **Step 5: Build and verify**

```bash
./gradlew build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/alma/improved_original/quest/engine/CompletedChainStore.java
git commit -m "perf: async completed chain writes with in-memory dedup

Move file I/O off the server thread via CompletableFuture.runAsync.
Cache directory path to avoid repeated Files.createDirectories calls.
Add ConcurrentHashMap-based dedup to skip redundant async dispatches.

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 2: ChainResolver countChainSteps Cache

**Files:**
- Modify: `src/main/java/com/alma/improved_original/quest/engine/QuestGenerator.java`
- Modify: `src/main/java/com/alma/improved_original/quest/engine/ChainResolver.java`

**Interfaces:**
- Produces: `QuestGenerator.getChainStepCount(String rootId) -> int` (new public static method)
- Consumes: Called from `ChainResolver.countChainSteps()` which becomes a map lookup

- [ ] **Step 1: Add cache field to QuestGenerator**

In `QuestGenerator.java`, after the `questPool` field, add:

```java
private static Map<String, Integer> chainStepCounts = Map.of();
```

- [ ] **Step 2: Populate cache in reloadPool() and getPool() lazy init**

In `QuestGenerator.java`, modify `reloadPool()`:

```java
public static void reloadPool() {
    questPool = QuestPoolConfig.loadFromConfig(FMLPaths.CONFIGDIR.get());
    chainStepCounts = computeChainStepCounts(questPool);
}
```

Also update the lazy init in `getPool()`:

```java
public static List<QuestPoolConfig.PoolEntry> getPool() {
    if (questPool == null) {
        questPool = QuestPoolConfig.loadFromConfig(FMLPaths.CONFIGDIR.get());
        chainStepCounts = computeChainStepCounts(questPool);
    }
    return questPool;
}
```

- [ ] **Step 3: Add computeChainStepCounts and getChainStepCount methods**

Add to `QuestGenerator.java`:

```java
private static Map<String, Integer> computeChainStepCounts(List<QuestPoolConfig.PoolEntry> pool) {
    Map<String, Integer> counts = new java.util.HashMap<>();
    for (var entry : pool) {
        if (entry.unlocks().isEmpty() && entry.conditions().isEmpty()) continue;
        // Determine root: if entry has no conditions, it IS a root
        String rootId;
        if (entry.conditions().isEmpty()) {
            rootId = entry.id();
        } else {
            rootId = entry.conditions().getFirst().requiredQuestId();
        }
        counts.merge(rootId, 1, Integer::sum);
    }
    return counts;
}

public static int getChainStepCount(String rootId) {
    return chainStepCounts.getOrDefault(rootId, 0);
}
```

- [ ] **Step 4: Replace countChainSteps in ChainResolver**

In `ChainResolver.java`, replace the `countChainSteps` method:

```java
private static int countChainSteps(String rootId) {
    return QuestGenerator.getChainStepCount(rootId);
}
```

- [ ] **Step 5: Build and verify**

```bash
./gradlew build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/alma/improved_original/quest/engine/QuestGenerator.java \
        src/main/java/com/alma/improved_original/quest/engine/ChainResolver.java
git commit -m "perf: cache chain step counts to avoid pool streaming

Pre-compute chain step counts in QuestGenerator.reloadPool().
Replace ChainResolver.countChainSteps() stream with map lookup.

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 3: IntArrayList for Progress Tracking

**Files:**
- Modify: `src/main/java/com/alma/improved_original/quest/QuestSlotData.java`
- Modify: `src/main/java/com/alma/improved_original/quest/engine/ProgressTracker.java`
- Modify: `src/main/java/com/alma/improved_original/quest/QuestData.java`

**Interfaces:**
- `QuestSlotData.perTargetProgress()` changes return type from `List<Integer>` to `IntArrayList`
- `QuestData.setProgress(int index, List<Integer> progress)` signature stays, but internally converts
- CODEC/StreamCodec for QuestSlotData updated to handle IntArrayList
- ProgressTracker.handle() uses IntArrayList.clone() instead of new ArrayList<>(progress)

- [ ] **Step 1: Change QuestSlotData.perTargetProgress to IntArrayList**

In `QuestSlotData.java`, change the record component type:

```java
import it.unimi.dsi.fastutil.ints.IntArrayList;

public record QuestSlotData(
        Optional<com.alma.improved_original.quest.task.IQuestTask> quest,
        IntArrayList perTargetProgress,
        boolean locked,
        boolean complete,
        QuestSlotType slotType,
        Optional<String> chainId
) {
```

Update `empty()` and `emptyChain()`:

```java
public static QuestSlotData empty() {
    return new QuestSlotData(Optional.empty(), new IntArrayList(), false, false,
            QuestSlotType.DAILY, Optional.empty());
}

public static QuestSlotData emptyChain() {
    return new QuestSlotData(Optional.empty(), new IntArrayList(), false, false,
            QuestSlotType.CHAIN, Optional.empty());
}
```

Update `progress()` method:

```java
public int progress() {
    int sum = 0;
    for (int i = 0; i < perTargetProgress.size(); i++) {
        sum += perTargetProgress.getInt(i);
    }
    return sum;
}
```

- [ ] **Step 2: Update QuestSlotData CODEC for IntArrayList**

Replace the `perTargetProgress` field in the CODEC:

```java
public static final Codec<QuestSlotData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                com.alma.improved_original.quest.task.IQuestTask.CODEC.optionalFieldOf("quest").forGetter(QuestSlotData::quest),
                Codec.INT.listOf().xmap(IntArrayList::new, java.util.ArrayList::new)
                        .fieldOf("progress").forGetter(QuestSlotData::perTargetProgress),
                Codec.BOOL.optionalFieldOf("locked", false).forGetter(QuestSlotData::locked),
                Codec.BOOL.optionalFieldOf("complete", false).forGetter(QuestSlotData::isComplete),
                QuestSlotType.CODEC.optionalFieldOf("slotType", QuestSlotType.DAILY).forGetter(QuestSlotData::slotType),
                Codec.STRING.optionalFieldOf("chainId").forGetter(QuestSlotData::chainId)
        ).apply(instance, QuestSlotData::new)
);
```

- [ ] **Step 3: Update QuestSlotData STREAM_CODEC for IntArrayList**

Replace the `perTargetProgress` field in the STREAM_CODEC:

```java
public static final StreamCodec<FriendlyByteBuf, QuestSlotData> STREAM_CODEC =
        StreamCodec.composite(
                ByteBufCodecs.optional(com.alma.improved_original.quest.task.IQuestTask.STREAM_CODEC),
                        QuestSlotData::quest,
                StreamCodec.of(
                        (buf, list) -> {
                            buf.writeVarInt(list.size());
                            for (int i = 0; i < list.size(); i++) {
                                buf.writeVarInt(list.getInt(i));
                            }
                        },
                        buf -> {
                            int size = buf.readVarInt();
                            IntArrayList list = new IntArrayList(size);
                            for (int i = 0; i < size; i++) {
                                list.add(buf.readVarInt());
                            }
                            return list;
                        }
                ), QuestSlotData::perTargetProgress,
                ByteBufCodecs.BOOL, QuestSlotData::locked,
                ByteBufCodecs.BOOL, QuestSlotData::isComplete,
                QuestSlotType.STREAM_CODEC, QuestSlotData::slotType,
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), QuestSlotData::chainId,
                QuestSlotData::new
        );
```

- [ ] **Step 4: Update compact constructor**

The compact constructor does an `IntStream.range(0, perTargetProgress.size())` check using `get(i)` which returns boxed Integer. Update to use `getInt()`:

```java
public QuestSlotData {
    if (quest.isPresent()
            && perTargetProgress.size() == quest.get().targets().size()
            && IntStream.range(0, perTargetProgress.size())
                    .allMatch(i -> perTargetProgress.getInt(i) >= quest.get().targets().get(i).count())) {
        complete = true;
    }
}
```

- [ ] **Step 5: Update QuestData.setQuest to use IntArrayList**

In `QuestData.java`, update `setQuest`:

```java
public void setQuest(int index, IQuestTask quest) {
    QuestSlotData old = slots.get(index);
    IntArrayList initProgress = new IntArrayList(quest.targets().size());
    for (int i = 0; i < quest.targets().size(); i++) initProgress.add(0);
    Optional<String> chainId = quest.isChainTask() ? Optional.of(quest.id()) : old.chainId();
    QuestSlotType type = chainId.isPresent()
            ? QuestSlotType.CHAIN : QuestSlotType.DAILY;
    slots.set(index, new QuestSlotData(Optional.of(quest), initProgress, old.locked(), false, type, chainId));
}
```

Add import at top of QuestData.java:
```java
import it.unimi.dsi.fastutil.ints.IntArrayList;
```

- [ ] **Step 6: Update QuestData.setProgress to accept IntArrayList-compatible list**

`setProgress` takes `List<Integer>` — change to accept any `List<Integer>` (stays compatible) but internally convert for the record:

```java
public void setProgress(int index, List<Integer> progress) {
    QuestSlotData old = slots.get(index);
    IntArrayList ial = progress instanceof IntArrayList ia ? ia : new IntArrayList(progress);
    slots.set(index, new QuestSlotData(old.quest(), ial, old.locked(), old.isComplete(),
            old.slotType(), old.chainId()));
}
```

- [ ] **Step 7: Update ProgressTracker.handle to use IntArrayList.clone()**

In `ProgressTracker.java`, update `handle()`:

```java
import it.unimi.dsi.fastutil.ints.IntArrayList;

public static void handle(ServerPlayer player, QuestType type, ResourceLocation targetId, int amount) {
    QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
    if (!data.isActive()) return;
    boolean changed = false;

    for (int i = 0; i < data.getSlots().size(); i++) {
        var questOpt = data.getQuest(i);
        if (questOpt.isEmpty()) continue;
        IQuestTask quest = questOpt.get();

        IntArrayList progress = data.getSlot(i).perTargetProgress();
        IntArrayList modified = null;
        for (int t = 0; t < quest.targets().size(); t++) {
            var target = quest.targets().get(t);
            if (target.type() != type) continue;
            if (!target.item().equals(targetId)) continue;

            int current = t < progress.size() ? progress.getInt(t) : 0;
            int newProg = Math.min(current + amount, target.count());
            if (newProg == current) continue;

            if (modified == null) {
                modified = progress.clone();
            }
            modified.set(t, newProg);
            changed = true;
        }

        if (modified != null) {
            data.setProgress(i, modified);
            if (data.isSlotComplete(i)) {
                QuestEngine.get().completeQuest(player, data, i);
            }
        }
    }

    if (changed) {
        data.markDirty();
    }
}
```

- [ ] **Step 8: Build and verify**

```bash
./gradlew build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/alma/improved_original/quest/QuestSlotData.java \
        src/main/java/com/alma/improved_original/quest/QuestData.java \
        src/main/java/com/alma/improved_original/quest/engine/ProgressTracker.java
git commit -m "perf: use IntArrayList for quest progress to eliminate boxing

Replace List<Integer> with fastutil IntArrayList in QuestSlotData,
QuestData, and ProgressTracker. Defer allocation in ProgressTracker
until a target actually matches. Update CODEC/StreamCodec accordingly.

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 4: Dirty Player Tracking

**Files:**
- Modify: `src/main/java/com/alma/improved_original/quest/QuestData.java`
- Modify: `src/main/java/com/alma/improved_original/quest/engine/QuestEngine.java`
- Modify: `src/main/java/com/alma/improved_original/quest/engine/ProgressTracker.java`
- Modify: `src/main/java/com/alma/improved_original/quest/engine/ChainResolver.java`
- Modify: `src/main/java/com/alma/improved_original/quest/engine/DailyRefreshScheduler.java`

**Interfaces:**
- Produces: `QuestEngine.markPlayerDirty(ServerPlayer)` — new public method, replaces `data.markDirty()` calls
- Produces: `QuestEngine.getDirtyPlayers() -> Set<ServerPlayer>` — package-private, for DailyRefreshScheduler
- Produces: `QuestEngine.clearDirtyPlayers()` — package-private, for DailyRefreshScheduler
- Removes: `QuestData.dirty` field, `markDirty()`, `clearDirty()`, `isDirty()` — replaced by engine-level tracking

**Design:** Remove the per-QuestData `transient boolean dirty` field and replace it with a `ConcurrentHashMap.newKeySet<ServerPlayer>()` in QuestEngine. All callers of `data.markDirty()` have a `ServerPlayer` reference, so each call site is changed to `QuestEngine.get().markPlayerDirty(player)`. DailyRefreshScheduler.flushDirtyPlayers iterates only the dirty set instead of all online players.

- [ ] **Step 1: Add dirtyPlayers set and accessors to QuestEngine**

In `QuestEngine.java`, add field and methods:

```java
private final java.util.Set<ServerPlayer> dirtyPlayers =
        java.util.concurrent.ConcurrentHashMap.newKeySet();

public void markPlayerDirty(ServerPlayer player) {
    dirtyPlayers.add(player);
}

java.util.Set<ServerPlayer> getDirtyPlayers() {
    return java.util.Set.copyOf(dirtyPlayers);
}

void clearDirtyPlayers() {
    dirtyPlayers.clear();
}
```

- [ ] **Step 2: Replace data.markDirty() with markPlayerDirty(player) in QuestEngine methods**

In `QuestEngine.java`, replace all `data.markDirty()` calls:

In `lockSlot()` — replace `data.markDirty();` with `markPlayerDirty(player);`

In `unlockSlot()` — replace `data.markDirty();` with `markPlayerDirty(player);`

In `ensureQuestsInitialized()` — replace `data.markDirty();` with `markPlayerDirty(player);`

In `completeQuest()` — add `markPlayerDirty(player);` after `data.clearSlot(slot);` (this method doesn't explicitly call markDirty but the slot state changes need syncing)

- [ ] **Step 3: Replace data.markDirty() in ProgressTracker**

In `ProgressTracker.handle()`, change the final block from:

```java
if (changed) {
    data.markDirty();
}
```

To:

```java
if (changed) {
    QuestEngine.get().markPlayerDirty(player);
}
```

- [ ] **Step 4: Replace data.markDirty() in ChainResolver**

In `ChainResolver.resolve()`, on the line `data.markDirty();` replace with:

```java
QuestEngine.get().markPlayerDirty(player);
```

- [ ] **Step 5: Replace data.markDirty() in DailyRefreshScheduler**

In `DailyRefreshScheduler.refreshPlayerSlots()`, replace `data.markDirty();` with:

```java
QuestEngine.get().markPlayerDirty(player);
```

- [ ] **Step 6: Clean up QuestData — remove dirty field and related methods**

In `QuestData.java`, remove:
- Field: `private transient boolean dirty;`
- Method: `public boolean isDirty() { return dirty; }`
- Method: `public void markDirty() { this.dirty = true; }`
- Method: `public void clearDirty() { this.dirty = false; }`

- [ ] **Step 7: Update DailyRefreshScheduler.flushDirtyPlayers**

In `DailyRefreshScheduler.java`, replace the method:

```java
private static void flushDirtyPlayers(MinecraftServer server) {
    QuestEngine engine = QuestEngine.get();
    for (ServerPlayer player : engine.getDirtyPlayers()) {
        QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
        engine.syncToPlayerSilent(player, data);
    }
    engine.clearDirtyPlayers();
}
```

- [ ] **Step 8: Build and verify**

```bash
./gradlew build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/alma/improved_original/quest/QuestData.java \
        src/main/java/com/alma/improved_original/quest/engine/QuestEngine.java \
        src/main/java/com/alma/improved_original/quest/engine/ProgressTracker.java \
        src/main/java/com/alma/improved_original/quest/engine/ChainResolver.java \
        src/main/java/com/alma/improved_original/quest/engine/DailyRefreshScheduler.java
git commit -m "perf: track dirty players via set instead of per-tick scan

Replace QuestData.dirty boolean with a ConcurrentHashMap-based
dirtyPlayers set in QuestEngine. flushDirtyPlayers now iterates
only dirty players instead of all online players every tick.

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 5: QuestFragment Tick Optimization

**Files:**
- Modify: `src/main/java/com/alma/improved_original/quest/screen/QuestFragment.java`

**Interfaces:**
- No external interface changes — internal tick scheduling refactor only

- [ ] **Step 1: Remove redundant version check from tickCountdown**

In `QuestFragment.java`, replace the `tickCountdown` method:

```java
private void tickCountdown() {
    View view = getView();
    if (view != null && isAdded()) {
        updateCountdownText();
        // Schedule next update at the next minute boundary
        long nextDelay = computeMsToNextMinute();
        view.postDelayed(this::tickCountdown, nextDelay);
    }
}

private long computeMsToNextMinute() {
    if (Minecraft.getInstance().level == null) return 60_000;
    long tick = Minecraft.getInstance().level.getGameTime();
    long iv = 20L * 60 * Config.QUEST_REFRESH_INTERVAL_MINUTES.getAsInt();
    if (iv <= 0) iv = 1;
    long secRemaining = (iv - tick % iv) / 20;
    long secInCurrentMinute = secRemaining % 60;
    if (secInCurrentMinute == 0) secInCurrentMinute = 60;
    return secInCurrentMinute * 1000;
}
```

- [ ] **Step 2: Update onViewCreated initial delay**

In `onViewCreated`, change:

```java
// Old: view.postDelayed(this::tickCountdown, 1000);
// New:
view.postDelayed(this::tickCountdown, computeMsToNextMinute());
```

- [ ] **Step 3: Build and verify**

```bash
./gradlew build
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/alma/improved_original/quest/screen/QuestFragment.java
git commit -m "perf: reduce QuestFragment tick rate to per-minute

Remove redundant version polling from tickCountdown (listener already
handles data change detection). Schedule countdown updates at minute
boundaries instead of every second.

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 6 (Deferred): Incremental Quest Sync

**Deferred.** Implement after profiling confirms full-sync payload overhead is significant. The spec documents the approach: add `QuestDeltaPayload` carrying changed slot index + `QuestSlotData` + active chains, client-side merge logic in `QuestClientEvents`. Not implemented in this plan.
