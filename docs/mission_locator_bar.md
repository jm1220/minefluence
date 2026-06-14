# MineFluence Mission Locator Bar

## Scope

MineFluence targets Minecraft 1.21.1, so this feature is a custom HUD overlay. It does not use the vanilla Locator Bar APIs introduced in later Minecraft versions, and it never reads or writes the player's XP level, total XP, or `experienceProgress`.

The existing particle area guide is unchanged. Particles mark the configured destination when requested or when the mission flow starts a guide; the locator is automatically visible during an eligible active mission and shows which way to face.

## Missions

The server reuses `MineFluenceMissionAreas.getAreaForMission` as the single mission-to-area mapping:

| Route | Mission | Area |
| --- | ---: | --- |
| Good | 1 | `garden` |
| Good | 2 | `farm` |
| Good | 6 | `shared` |
| Good | 7 | `farm_build` |
| Bad | 2 | `farm` |
| Bad | 4 | `shared` |
| Bad | 6 | `farm` |

All other missions have no fixed target area and do not show the locator.

## Visibility

The server includes a locator target only when:

- the demo is started;
- a mission is active and incomplete;
- the player is not waiting to upload/post;
- no invasion or ending has replaced the mission flow;
- the central mission mapping returns an area type; and
- that area exists in `MineFluenceWorldState`.

The client also requires the synced area dimension to match the current world. Missing snapshots, mappings, area definitions, or mismatched dimensions safely produce no locator.

The locator uses the existing HUD policy and is hidden while any GUI screen is open, including the smartphone, tutorial, mission board, posting screen, and chat. It is also hidden when the HUD is disabled or the F3 debug HUD is visible.

## Authoritative Sync

`MineFluenceHudStatePayload` now carries:

- whether the active mission has a resolved area;
- area command name;
- area dimension ID; and
- inclusive minimum and maximum X/Y/Z block coordinates.

`MineFluenceHud.refresh` sends the snapshot immediately during existing state transitions. The existing periodic HUD sync repeats it every 20 server ticks. The client only calculates and renders the indicator; it does not change mission or area state.

## Direction Calculation

The client computes the target center with double precision:

```text
centerX = (minX + maxX) / 2.0
centerY = (minY + maxY) / 2.0
centerZ = (minZ + maxZ) / 2.0
```

Horizontal direction uses the current camera yaw:

```text
targetYaw = degrees(atan2(-(centerX - playerX), centerZ - playerZ))
deltaAngle = wrapDegrees(targetYaw - cameraYaw)
```

The total locator field of view is 120 degrees. `deltaAngle / 60` maps to the 182-pixel bar and is clamped to `-1..1`, so targets behind the player remain at the nearest left or right edge.

If the player is inside the inclusive area bounds, or within five horizontal blocks of its center, the marker is centered.

## Rendering

`MineFluenceHudOverlay` draws the locator at approximately `screenHeight - 49`, above the normal XP bar, without replacing it:

- thin 182-pixel translucent line;
- green marker for Good missions or red marker for Bad missions;
- radius 4 at 10 blocks or less, radius 3 at 30 blocks or less, and radius 2 farther away;
- small up/down hint when the area center differs from player Y by more than five blocks; and
- no text label, keeping the XP-bar area clear.

The Followers/Trust HUD is a centered horizontal icon row. It reuses `bad_icon.png` for Followers and `good_icon.png` for Trust, matching the old right-side panel, and draws only each numeric value. The row moves from `screenHeight - 65` to `screenHeight - 78` only while the locator is visible.

Mission title and progress remain in the top-right objective card. The previous per-progress action-bar line such as `Garden Starter: 0/3`, along with the locator's `Mission Area` and `Mission Area Nearby` labels, is no longer rendered. Mission-complete notifications remain unchanged.

## Particle Guide

The smartphone `Show Mission Area` action and automatic mission-start guide still use `MineFluenceAreaGuideManager`. The locator does not start, stop, or alter particles and does not place or remove blocks.

## Limitations

- The indicator is directional only; it does not display numeric distance.
- Area edits can take up to the existing one-second HUD sync interval to appear unless another state change triggers an immediate refresh.
- A target in another dimension is hidden rather than pointing toward a portal.
- Behind-target direction can switch edges when the wrapped angle crosses exactly 180 degrees.
