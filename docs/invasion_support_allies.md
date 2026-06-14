# Invasion Support Allies

Stage 6 adds temporary, server-authoritative village defenders based on Social Credibility.

## Social Credibility Tiers

The placeholder values are stored in `MineFluenceBalance` and mapped by
`getInvasionSupportCount(int socialCredibility)`:

| Social Credibility | Support allies |
|---|---:|
| `<= -300` | 0 |
| `-299` to `-100` | 1 |
| `-99` to `99` | 2 |
| `100` to `299` | 3 |
| `>= 300` | 5 |

These are balance placeholders for the single-player demo.

## Ally Type And Identity

Support allies are vanilla `IronGolemEntity` instances named `MineFluence Defender`.
Each defender:

- Has its custom name visible
- Is marked player-created so it does not treat the player as a village threat
- Has persistence enabled
- Has the command tag `minefluence_invasion_support`
- Has a deterministic slot tag such as `minefluence_invasion_support_slot_0`

The support tag is separate from the Stage 5 fan tag `minefluence_fan`.

## Spawn Points

The fixed Overworld points are stored in `MineFluenceDemoMapPreset`:

1. `(0, -60, 10)`
2. `(8, -60, 10)`
3. `(16, -60, 16)`
4. `(8, -60, 22)`
5. `(20, -60, 10)`

The manager searches nearby horizontal offsets and up to eight blocks above or
below each configured Y. A valid point requires solid ground, three blocks of
clear non-fluid body space, and no block or entity collision for the golem.

## Invasion Start

All invasion entry points use `MineFluenceInvasionManager.startInvasion(...)`,
including automatic mission invasions and `/minefluence invasion start <index>`.

After invasion enemies are spawned and tracked, the support manager:

1. Removes stale tagged defenders.
2. Reads the player's current Social Credibility.
3. Computes the target support count.
4. Spawns defenders in unused configured slots.
5. Sends one trust or no-trust message.

Support allies are not spawned by a tick loop.

## Combat Targeting

DDJ is a custom hostile entity, so vanilla Iron Golem target selection is not
assumed to recognize it reliably. During an active invasion, the existing
20-tick invasion update assigns each support golem to the nearest living,
tracked MineFluence invader when it does not already have a valid invasion
target.

This targeting only selects active invasion enemies. It does not select the
player, normal villagers, or fan villagers. DDJ's existing AI is unchanged.

## Cleanup And Reload

Only Iron Golems tagged `minefluence_invasion_support` are removed. Cleanup runs:

- When an invasion succeeds
- When an invasion fails
- When `/minefluence invasion stop_debug` stops an active invasion
- During `/minefluence start`, `/minefluence demo setup`, and `/minefluence reset`
- On player join when no invasion is active

If an invasion is still active after reload, loaded tagged defenders are
reconciled to the Social Credibility target using their slot tags. UUIDs are not
stored in player data.

Normal Iron Golems, villagers, fan villagers, and unrelated entities are not
removed.

## Debug Commands

- `/minefluence support count`
- `/minefluence support spawn`
- `/minefluence support clear`

The existing workflow also exercises the full lifecycle:

1. `/minefluence set social <value>`
2. `/minefluence invasion start 1`
3. `/minefluence invasion stop_debug`

`/minefluence invasion status` also reports the current loaded defender count.

## Limitations

- Defender discovery covers loaded tagged Iron Golems within 96 blocks of the
  fixed demo village center.
- Support spawn points are fixed Overworld placeholders.
- A defender that leaves the search area or is in an unloaded chunk may not be
  discovered until it returns or loads.
- Debug-spawned defenders do not have invasion targets until a real invasion is
  active.
