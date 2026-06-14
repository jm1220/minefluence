# Fixed Map Areas and Particle Guidance

MineFluence uses the existing persistent `MineFluenceArea` registry. Stage 8 adds one central mission mapping and short-lived particle guides; it does not create a second region system.

## Fixed Preset

The preset is stored in `MineFluenceDemoMapPreset` for `minecraft:overworld`. The old preset near the origin was replaced; only the new map coordinates below are active.

| Area name | Survey corner 1 | Survey corner 2 | Stored inclusive minimum | Stored inclusive maximum |
| --- | --- | --- | --- | --- |
| `garden` | `(-827, 65, 723)` | `(-839, 65, 711)` | `(-839, 62, 711)` | `(-827, 71, 723)` |
| `farm` | `(-857, 64, 689)` | `(-839, 64, 709)` | `(-857, 61, 689)` | `(-839, 70, 709)` |
| `shared` | `(-846, 64, 754)` | `(-833, 65, 764)` | `(-846, 61, 754)` | `(-833, 71, 764)` |
| `farm_build` | `(-859, 68, 729)` | `(-867, 68, 723)` | `(-867, 65, 723)` | `(-859, 74, 729)` |

The preset subtracts three blocks from the lower survey Y and adds six blocks to the upper survey Y. `MineFluenceArea.box` then normalizes X, Y, and Z. Bounds are inclusive: `contains`, mission scans, center calculation, and particle boundaries all include both stored endpoints.

## Mission Mapping

`MineFluenceMissionAreas.getAreaForMission(route, missionIndex)` is the shared mapping:

| Route and mission | Area |
| --- | --- |
| Good 1 | `garden` |
| Good 2 | `farm` |
| Good 6 | `shared` |
| Good 7 | `farm_build` |
| Bad 2 | `farm` |
| Bad 4 | `shared` |
| Bad 6 | `farm` |

All other missions have no fixed area.

## Commands

- `/minefluence area load_preset`: overwrites the four named definitions with the new-map fixed boxes. It is safe to run repeatedly and does not create duplicate area entries.
- `/minefluence area list`: lists every required area and its saved box.
- `/minefluence area info <area>`: shows one saved area or reports it missing.
- `/minefluence area show <area>`: starts a neutral five-second particle guide.
- `/minefluence area set_box ...`: remains available for exact custom boxes.
- `/minefluence area set ...`: remains available for player-centered radius areas.

The valid `<area>` values are `garden`, `farm`, `shared`, and `farm_build`.

## Particle Guide

Guides are server-managed and last 100 ticks, approximately five seconds. They render every 10 ticks and then remove their tracking entry.

- The X/Z rectangle boundary is sampled every two blocks.
- A vertical `END_ROD` center marker is rendered for five particle positions.
- Good missions use `HAPPY_VILLAGER` boundary particles.
- Bad missions use `ANGRY_VILLAGER` boundary particles.
- Manual `/area show` commands use neutral `END_ROD` particles.
- Display Y searches within the configured Y range for open space above solid ground near the area center.

Particles do not place, break, or permanently alter blocks. Static guide state is cleared when the server stops.

## Automatic Loading and Guidance

`/minefluence start` loads any missing preset definitions. `/minefluence area load_preset` remains the manual repair and overwrite command, including for a saved world that still contains customized or old-map definitions.

Starting any mapped Good or Bad mission also restores missing preset definitions before validating the mission area. The relevant guide is shown once when the mission becomes active with:

`[MineFluence] Follow the particles to the mission area.`

The Stage 7 smartphone `Show Mission Area` action uses the same guide. It reports pending mission selection, no-area missions, missing/unavailable areas, and successful area names without changing player data on the client.

## Enforcement

The following missions enforce their configured boxes:

- Good 1: flowers counted in `garden`
- Good 2: wheat counted in `farm`
- Good 6: hay bales counted in `shared`
- Good 7: farm plot components counted in `farm_build`
- Bad 2: farmland breaking or trampling counted in `farm`
- Bad 4: chest/barrel theft counted in `shared`
- Bad 6: farm plot destruction counted in `farm`

Good 3, Good 4, Good 5, and Bad 1, Bad 3, Bad 5, Bad 7 have no fixed area and therefore receive no area guide or area restriction.

## Limitations

- A single display Y is selected from the area center column, so highly uneven terrain may make part of a rectangular boundary less precise vertically.
- Guides are global server particles near the configured area rather than a private client-only effect.
- Custom area definitions remain supported, but `/minefluence area load_preset` intentionally overwrites them.
- Automatic loading fills missing definitions only; it does not replace an existing custom or old saved definition. Run `/minefluence area load_preset` once to migrate such a saved world to the new map preset.
