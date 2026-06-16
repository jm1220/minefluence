# Player Death Reset

MineFluence resets the single-player demo to its pre-tutorial state whenever
the player dies.

## Events

- `ServerLivingEntityEvents.AFTER_DEATH` resets persisted demo progress
  immediately and cleans up active invasion entities and support allies.
- `ServerPlayerEvents.AFTER_RESPAWN` reapplies the reset to the respawned player
  instance and ensures the inventory contains exactly one smartphone.
- Respawn copies with `alive=true`, such as non-death transitions, are ignored.

## Central Reset

`MineFluenceDemoFlow.resetDemoProgress(ServerPlayerEntity)` is the shared
server-authoritative reset entry point. It:

- Removes tracked invasion enemies and tagged support allies.
- Removes MineFluence fan villagers before syncing the reset follower tier.
- Stops the player's active mission-area particle guide.
- Clears transient mission container snapshots.
- Resets persisted player data to the tutorial-start state.
- Removes Farmer demo hoes and resets the stored weapon tier to Wood.
- Refreshes the custom HUD with no active objective or locator target.

The persisted reset clears:

- Tutorial/demo started flag
- Selected job
- Completed mission count
- Pending mission selection
- Active mission index and route
- Mission progress, baseline, Mission 5 craft/place counters, and supply marker
- Pending posting mission index and route
- Active invasion index, tracked UUIDs, total, and start tick
- Last completed invasion index
- Ending ID, ending flag, and exposure flag
- Followers, Social Credibility, and hidden Lie Value
- Stored weapon tier

`MineFluenceDemoFlow.startDemo(...)` and the debug demo setup share the same
cleanup path but finish with the demo-started flag enabled, preserving the
existing tutorial completion and ending Restart Demo behavior.
`/minefluence reset` uses the pre-tutorial reset and ensures a smartphone.

## Smartphone State

After a death respawn, `demoStarted` is false and all higher-priority mission,
upload, invasion, exposure, and ending flags are clear. The phone snapshot is
therefore `NOT_STARTED`, whose home screen shows only:

- `Tutorial`
- `Close`

Completing the tutorial starts a fresh demo and selects Farmer through the
existing server flow.

`MineFluenceItems.ensureSingleSmartphone` preserves the first smartphone already
in the respawned player's inventory, normalizes its stack count to one, removes
inventory duplicates, and gives a new phone only when none exists.

## Dropped Items

The mod does not cancel inventory drops and does not remove nearby
`ItemEntity` instances. Dropped-item cleanup is handled by the map's command
block and pressure-plate setup at spawn.
