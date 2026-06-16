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

1. `(-858, 65, 719)`
2. `(-854, 65, 719)`
3. `(-850, 65, 723)`
4. `(-846, 65, 727)`
5. `(-842, 65, 731)`
6. `(-838, 65, 735)`
7. `(-834, 65, 731)`
8. `(-838, 65, 723)`
9. `(-846, 65, 715)`
10. `(-852, 65, 713)`

The search center is `(-846, 65, 726)` with an 80-block radius. These points
preserve the original relative fan layout around the current demo village.
Before spawning, the manager checks a deterministic five-block horizontal
radius and up to eight blocks above or below the configured Y for solid ground,
free feet/head blocks, and entity collision space.

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

Fan villagers count as valid villagers for villager-based mission objectives
while still being constrained to actual `VillagerEntity` instances. The
`minefluence_fan` tag does not make non-villager entities valid mission
targets.

They are accepted by:

- Good Mission 3 completed farmer-villager trade progress
- Good Mission 4 potato giveaway villager interaction progress
- Bad Mission 3 villager hit progress
- Bad Mission 7 villager kill progress

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
- Concise server logs report each sync's follower value, expected count, actual
  tagged count, spawned count, removed count, and any slot that has no safe
  position.
