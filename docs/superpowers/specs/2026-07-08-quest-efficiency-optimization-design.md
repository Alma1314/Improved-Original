# Quest System Efficiency Optimization Design

## Date: 2026-07-08

## Scope

Optimize server-thread execution efficiency of the quest system across three layers: blocking I/O elimination, per-tick overhead reduction, and network payload trimming. All changes are internal to the quest engine — no API surface changes.

---

## Layer 1: Main-Thread Blocking Elimination

### 1.1 CompletedChainStore Async Write

**Current state** (`CompletedChainStore.writeCompletedChain`):
- Called synchronously from `ChainResolver.updateChainProgress()` on the server thread
- Reads entire JSON file, parses, modifies in-memory, writes back — all blocking

**Change**:
1. Cache `getDir()` result — `Files.createDirectories` is called every write, but the directory only needs to be created once. Store as `private static Path cachedDir` with lazy init.
2. Split `writeCompletedChain` into two phases:
   - **Server thread**: Build the `JsonObject` tree (all in-memory, no I/O)
   - **Async**: Read existing file if present, merge, write back — run via `CompletableFuture.runAsync(() -> { ... })` using the common ForkJoinPool
3. Deduplication check: Before dispatching async, check an in-memory `ConcurrentHashMap<UUID, Set<String>>` to see if this chain was already written for this player — if so, skip the async dispatch entirely. Add to set after successful write.

**Trade-off**: Async writes mean a server crash between chain completion and write may lose the record. Acceptable — Minecraft saves happen on autosave and world unload, not per-tick.

### 1.2 ChainResolver.countChainSteps() Cache

**Current state** (`countChainSteps`):
- Streams entire quest pool (potentially hundreds of entries) every time a chain step completes
- Called from `updateChainProgress()` which runs on every chain step completion

**Change**:
1. Add `private static Map<String, Integer> chainStepCounts` to `QuestGenerator`
2. Populate in `reloadPool()`: for each entry with a non-empty `conditions()` list, find the root via `conditions().getFirst().requiredQuestId()`, increment count for that root. Also count the root entry itself.
3. Expose `QuestGenerator.getChainStepCount(String rootId)` returning the cached count
4. `countChainSteps()` becomes a single map lookup

### 1.3 ProgressTracker Allocation Reduction

**Current state** (`ProgressTracker.handle`):
- `new ArrayList<>(progress)` copies the entire per-target progress list even if only one target matches
- `List<Integer>` causes boxing for every progress value

**Change**:
1. Replace `List<Integer>` with `it.unimi.dsi.fastutil.ints.IntArrayList` for `perTargetProgress` in QuestSlotData (single primitive int array under the hood, no boxing)
2. In `handle()`, defer the copy: only `clone()` the `IntArrayList` when a target actually matches and progress changes. Use `IntArrayList.clone()` which is a direct array copy without boxing.
3. Update CODEC/StreamCodec for `IntArrayList` — use `Codec.INT.listOf().xmap(IntArrayList::new, ArrayList::new)` for codec, and manual stream codec

**Trade-off**: Introduces fastutil dependency (already present in Minecraft). `IntArrayList` is not an `ImmutableList` — but `QuestSlotData` is a record with a `compact constructor` that doesn't mutate the list, so this is safe.

---

## Layer 2: Per-Tick Overhead Reduction

### 2.1 Dirty Player Tracking

**Current state** (`DailyRefreshScheduler.flushDirtyPlayers`):
- Every server tick iterates all online players to check `data.isDirty()`
- 99.9% of ticks have zero dirty players — wasted work

**Change**:
1. Add `private final Set<ServerPlayer> dirtyPlayers = ConcurrentHashMap.newKeySet()` to `QuestEngine`
2. `QuestData.markDirty()` is called in multiple places but has no way to reach QuestEngine. Replace the `transient boolean dirty` flag with a callback: add `Consumer<ServerPlayer>` as a field on QuestData (transient, not serialized). `markDirty()` calls this callback if set.
3. `QuestEngine` sets this callback when accessing QuestData: `data.setDirtyCallback(p -> dirtyPlayers.add(p))`
4. `flushDirtyPlayers` iterates only `dirtyPlayers`, clears dirty flag + syncs, then removes from set
5. On player disconnect, remove from set

### 2.2 QuestFragment Tick Optimization

**Current state** (`tickCountdown`):
- Runs every second via `postDelayed(this::tickCountdown, 1000)`
- Checks version number (redundant — listener already handles this)
- Updates countdown text every second (only needs per-minute granularity)

**Change**:
1. Remove version check from `tickCountdown` — the `ClientQuestCache.setListener` callback already triggers `scheduleRefresh()` when data changes
2. Compute delay until next minute boundary: `long secRemaining = (iv - tick % iv) / 20; long msToNextMinute = (secRemaining % 60) * 1000` → first post at `msToNextMinute`, subsequent posts at 60_000ms intervals
3. Alternatively: use a single scheduled post that updates countdown and recalculates the next boundary — no fixed-interval polling

---

## Layer 3: Network Payload Trimming

### 3.1 Incremental Quest Sync

**Current state**: Every sync sends full `QuestData` — all slots, all active chains. For silent background syncs (dirty flush), only one slot may have changed.

**Change**:
1. Add `quest.improved_original.quest.network.QuestDeltaPayload` — carries only the changed slot index + `QuestSlotData`, plus the full active chains map (chains are small and infrequent)
2. Add `syncDelta(ServerPlayer player, int slotIndex, QuestSlotData slotData, Map<String, ChainProgress> chains)` to `QuestEngine`
3. `flushDirtyPlayers` uses delta sync when only one slot changed; uses full sync when multiple slots changed or active chains were updated
4. Client-side: `QuestClientEvents` handles delta payload by updating the cached QuestData in-place (copy-on-write the slot list) and notifying listeners

**Trade-off**: Adds a second network packet type and client-side merge logic. Only worthwhile if full sync payloads are measurably large. Defer to after profiling confirms full-sync overhead is significant.

---

## Implementation Order

| # | Item | Files Changed | Risk |
|---|------|--------------|------|
| 1 | 1.1 CompletedChainStore async write | CompletedChainStore.java | Low |
| 2 | 1.2 ChainResolver count cache | QuestGenerator.java, ChainResolver.java | Low |
| 3 | 1.3 IntArrayList for progress | QuestSlotData.java, ProgressTracker.java, QuestData.java | Medium (serialization changes) |
| 4 | 2.1 Dirty player tracking | QuestEngine.java, QuestData.java, DailyRefreshScheduler.java | Low |
| 5 | 2.2 Fragment tick optimization | QuestFragment.java | Low |
| 6 | 3.1 Incremental sync (deferred) | QuestSyncPayload.java, QuestClientEvents.java, QuestEngine.java | Medium |

---

## Verification

After each layer:
1. `./gradlew build` — compilation succeeds
2. Manual test: complete a chain quest, verify chain history persists
3. Manual test: break blocks rapidly, verify quest progress updates correctly
4. Manual test: wait for daily refresh, verify slots refresh
5. Check server log for no new ERROR entries
