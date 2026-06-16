# Bad Farmer Mission Detection

Farmer Bad missions now use server-side gameplay detection for all seven demo missions. All detectors reuse the existing mission progress flow: progress updates the active mission, reaching the target marks it ready to post, and rewards/billboard/Lie Value changes still happen only through posting.

## Shared Active Mission Rules

Progress only counts when:

- The acting player is a `ServerPlayerEntity`.
- The world/action is server-side.
- The selected job is Farmer.
- The ending has not already been triggered.
- A mission is active.
- The active route is Bad.
- The active mission index matches the detector.
- The mission is not already waiting for posting.
- The mission target is positive and current progress is below the target.

## Implemented Bad Missions

- Bad Mission 1: ring the village bell repeatedly.
- Bad Mission 2: trample or destroy farmland.
- Bad Mission 3: hit villagers.
- Bad Mission 4: steal items from a villager chest.
- Bad Mission 5: break farmer job blocks.
- Bad Mission 6: destroy a villager farm plot.
- Bad Mission 7: kill villagers.

## Events And Hooks

- Bell ringing: `UseBlockCallback`
- Farmland direct breaking: `PlayerBlockBreakEvents.AFTER`
- Farmland trampling: `FarmlandBlockMixin` injects into vanilla `FarmlandBlock.setToDirt`
- Villager hit: `AttackEntityCallback`
- Chest/barrel stealing: `GenericContainerScreenHandlerMixin` snapshots container item count on open and compares on close
- Double chest support: `DoubleInventoryAccessor` reads both chest inventories
- Composter/farm block breaking: `PlayerBlockBreakEvents.AFTER`
- Villager death: `ServerLivingEntityEvents.AFTER_DEATH`

Normal vanilla villagers and MineFluence fan villagers tagged
`minefluence_fan` both count for villager-based mission detectors. The target
must still be a `VillagerEntity`, so DDJ enemies, support iron golems, players,
animals, and unrelated tagged entities do not count.

## Bad Mission 2: Trample The Farm

Target comes from the mission definition: `FARMER_BAD_MISSION_2_TARGET` is 5.

Direct breaking counts when the pre-break block state from `PlayerBlockBreakEvents.AFTER` is `Blocks.FARMLAND`. The event fires after a successful server-side break, so cancelled breaks do not count.

Trampling counts through `FarmlandBlockMixin`, because Fabric does not expose a specific farmland-trampled event. The mixin observes vanilla `FarmlandBlock.setToDirt(...)` before the block changes. It counts only when the causing entity is a non-spectator `ServerPlayerEntity`, the provided state is farmland, and the current world state at the position is still farmland. Mob trampling, random drying, and non-player block updates do not count.

Area policy: Bad Mission 2 counts farmland breaking and player-caused trampling only inside the configured `farm` area.

## Bad Mission 4: Steal From Villagers

Target comes from the mission definition: `FARMER_BAD_MISSION_4_TARGET` is 10 items.

Valid containers:

- Chest
- Trapped chest
- Double chest
- Barrel

The detector snapshots total item quantity in the opened chest/barrel inventory. When the player closes the same inventory, it compares the new total item quantity with the original snapshot. A net decrease increments progress by the number of removed items, capped by the existing mission target. Net increases and swaps that do not reduce total item quantity do not count.

Area policy: the container must be inside the configured `shared` area. The fixed preset is restored before this mission starts if the area is missing.

## Bad Mission 6: Destroy A Farm Plot

Target comes from the mission definition: `FARMER_BAD_MISSION_6_TARGET` is 1. The existing target is used as requested, so one valid farm-related block break inside the farm area completes the mission.

Valid destroyed blocks:

- Farmland
- Wheat
- Carrots
- Potatoes
- Beetroots

Area policy: Bad Mission 6 requires the destroyed block to be inside the configured `farm` area. If the farm area is missing or points at an unavailable dimension, the detector does not count progress and does not crash. There is no anywhere fallback because the MineFluence area system exists for this project and the demo start flow loads the preset farm area.

## Progress And Completion Sync

All Bad mission detectors call `MineFluenceMissionProgressManager.incrementBadMissionProgress(...)`. That helper applies the active mission guard, updates `activeMissionProgress`, refreshes the custom HUD with `MineFluenceHud.refresh(...)`, and calls the existing ready-to-post transition when the target is reached.

The smartphone status screen reads the same `activeMissionProgress` and mission target through `MineFluenceNetworking.phoneState(...)`, so Bad Mission 2/4/6 progress appears as current/target while active and as ready-to-post when complete.

## Limitations

- Bad Mission 4 uses net item-count decrease. If the player removes 10 items and adds 10 other items before closing, progress is 0 because the container total did not decrease.
- Bad Mission 4 is scoped to vanilla chest/trapped chest/double chest/barrel inventories. Other storage blocks are ignored.
- Bad Mission 6 uses the existing target of 1, so the first valid farm-related block break inside `farm` completes the mission.
- The project does not currently have villager-owned container metadata. Area membership is the ownership proxy.
- MineFluence fan villagers count for Bad Mission 3 and Bad Mission 7 because
  they are vanilla `VillagerEntity` instances with the `minefluence_fan` tag.

## Manual Test Steps

Bad Mission 2:

- Start the demo, choose Farmer, start Mission 2 Bad.
- Break five farmland blocks inside the configured `farm` area.
- Verify progress reaches 5/5 and the mission becomes ready to post.
- Repeat with jumping/falling on farmland until vanilla converts it to dirt.
- Verify mobs trampling farmland do not progress the mission.

Bad Mission 4:

- Start Mission 4 Bad.
- Place a chest, trapped chest, double chest, or barrel inside `shared`.
- Put at least 10 total items inside it.
- Open it, remove 7 items, close it, and verify progress is 7/10.
- Open it again, remove at least 3 more items, close it, and verify the mission becomes ready to post.
- Put items into the container without reducing total count and verify progress does not increase.

Bad Mission 6:

- Start Mission 6 Bad.
- Break farmland, wheat, carrots, potatoes, or beetroots inside the configured `farm` area.
- Verify progress reaches the existing 1/1 target and the mission becomes ready to post.
- Break the same block types outside the `farm` area and verify they do not count.

Debug commands remain available:

- `/minefluence mission complete_debug`
- `/minefluence stats`
