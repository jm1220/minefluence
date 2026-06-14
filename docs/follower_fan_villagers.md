# Follower Fan Villagers

Stage 5 adds server-authoritative fan villagers as visible feedback for the player's follower count.

## Follower Tiers

The placeholder balance tiers are stored in `MineFluenceBalance`:

| Followers | Target fans |
|---|---:|
| 0-9 | 0 |
| 10-29 | 2 |
| 30-59 | 4 |
| 60-89 | 7 |
| 90+ | 10 |

`MineFluenceBalance.getTargetFanCount(int followers)` performs the mapping. These values are balance placeholders.

## Spawn Points

The fixed Overworld coordinates are stored in `MineFluenceDemoMapPreset`:

1. `(-2, -60, 8)`
2. `(2, -60, 8)`
3. `(6, -60, 12)`
4. `(10, -60, 16)`
5. `(14, -60, 20)`
6. `(18, -60, 24)`
7. `(22, -60, 20)`
8. `(18, -60, 12)`
9. `(10, -60, 4)`
10. `(4, -60, 2)`

The search center is `(10, -60, 15)` with an 80-block radius. Before spawning, the manager checks nearby horizontal offsets and up to eight blocks above or below the configured Y for solid ground, free feet/head blocks, and entity collision space.

## Identity And Persistence

Fans are vanilla `VillagerEntity` instances with:

- Custom name `MineFluence Fan`
- Visible custom name
- Persistent scoreboard/command tag `minefluence_fan`
- Per-point slot tag such as `minefluence_fan_slot_0`
- Vanilla entity persistence enabled
- A small position target around the selected spawn point

The main tag supports discovery and cleanup without storing UUIDs. Slot tags make repeated synchronization deterministic and avoid assigning multiple newly spawned fans to the same configured point.

## Synchronization

`MineFluenceFanVillagers.syncFanVillagers(ServerPlayerEntity)`:

- Reads the authoritative follower value from `MineFluenceWorldState`
- Calculates the target tier
- Counts only living, loaded `VillagerEntity` instances tagged `minefluence_fan`
- Removes tagged extras when the target decreases
- Spawns missing fans at unused configured slots
- Sends `New fans have arrived in the village.` only when at least one fan was spawned

Synchronization runs after:

- Normal or exaggerated posting rewards
- `/minefluence set follower <value>`
- `/minefluence add follower <value>`
- Ending test follower changes
- Player join/world load
- Demo start, demo setup, and stats reset
- Manual `/minefluence fans sync`

## Reset Behavior

`/minefluence start`, `/minefluence demo setup`, and `/minefluence reset` first remove only nearby villagers tagged `minefluence_fan`. They then reset player data and synchronize against the reset follower value. The default reset value is zero, so no fans remain.

Normal villagers and all other entities are untouched.

## Mission Interaction

Fan villagers are excluded from:

- Good Mission 3 and Good Mission 4 villager interaction progress
- Bad Mission 3 villager hit progress
- Bad Mission 7 villager kill progress

This keeps fans as social feedback rather than mission targets.

## Testing Commands

- `/minefluence fans sync`: reconcile current and target fan counts
- `/minefluence fans clear`: remove only tagged fan villagers near the village
- `/minefluence fans count`: show current count, target count, and follower value

The existing follower debug commands also synchronize fans automatically.

## Limitations

- Fan discovery is tag-based and searches loaded entities within 80 blocks of the fixed demo village center.
- No fan UUID registry is stored in persistent state.
- Fans use vanilla villager appearance and AI; the custom name and tags are their current distinction.
- Spawn coordinates and tiers are placeholders for the current fixed demo map.
