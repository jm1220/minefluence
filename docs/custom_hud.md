# MineFluence Custom HUD

## Assets

- `src/main/resources/assets/minefluence/textures/gui/hud/good_icon.png`
- `src/main/resources/assets/minefluence/textures/gui/hud/bad_icon.png`
- `src/main/resources/assets/minefluence/textures/gui/hud/invasion_icon.png`
- `src/main/resources/assets/minefluence/textures/gui/hud/lie_icon.png`

`good_icon.png` is used for Good missions. `bad_icon.png` is used for Bad missions. `invasion_icon.png` is used for invasion objectives. `lie_icon.png` remains available for debug/future UI but is not used by the normal gameplay HUD.

## Objective Card

The client renders a top-right objective card during normal gameplay. It is hidden while a menu is open, while the HUD is hidden, or while the debug screen is visible.

When an invasion is active, the card shows:

- `Under Invasion`
- `Defeat all monsters`
- defeated/total DDJ invader progress

When a mission is active and no invasion is active, the card shows:

- `Mission N - Good` or `Mission N - Bad`
- the current mission objective
- current/target mission progress

If no mission or invasion is active, the objective card is not rendered.

## Compact Public Stats

The old right-side stat panel is disabled by default with `SHOW_RIGHT_STAT_PANEL = false` in `MineFluenceHudOverlay`.

The normal gameplay HUD now renders a compact horizontal icon-and-value row above the vanilla hotbar:

- Followers uses `textures/gui/hud/bad_icon.png`, matching the old right-side status panel.
- Trust/Social Credibility uses `textures/gui/hud/good_icon.png`, matching the old right-side status panel.

Example:

```text
[follower icon] 60     [trust icon] +300
```

The row is centered with `screenWidth / 2` style positioning and normally drawn around `screenHeight - 65`, above the hotbar/health area. While the mission locator is visible, it moves to `screenHeight - 78` so the two overlays do not overlap. Only numeric values are drawn; the old bracketed Followers/Trust labels are removed. Social Credibility uses signed formatting: positive values show `+`, zero shows `0`, and negatives keep `-`.

Exact Lie Value and Lie Gauge are not shown in the normal gameplay HUD. Debug commands may still expose exact values for demo/testing.

## Scoreboard Sidebar

The old MineFluence scoreboard sidebar is disabled by default with `SHOW_SCOREBOARD_SIDEBAR = false` in `MineFluenceHud`. `MineFluenceHud.refresh` still sends the client HUD snapshot immediately, but clears the old `mf_hud` sidebar if it is present.

## Sync

The server remains authoritative. `MineFluenceHud.refresh` sends `MineFluenceHudStatePayload` to the player. A periodic server tick sync sends the same snapshot once per second so passive mission/invasion progress stays current.

The snapshot contains stats, mission display details, active mission area bounds, pending posting metadata, invasion index/progress, and ending display text. The client stores the latest snapshot in `MineFluenceHudState` and renders it in `MineFluenceHudOverlay`.

`/minefluence hud refresh` manually sends the current snapshot to the command user.

## Mission Locator

Area-based active missions also render a small custom locator near the vanilla XP bar. It is a MineFluence client overlay for Minecraft 1.21.1 and does not replace or mutate Minecraft experience. See `docs/mission_locator_bar.md` for the visibility rules and direction calculation.

The locator is label-free. Mission progress is displayed by the top-right objective card, so per-progress action-bar text and the bottom `Mission Area`/`Mission Area Nearby` labels are not rendered.

## Limitations

- The compact HUD does not show exact Lie Value by design.
- The old right-side stat panel and scoreboard sidebar are disabled but still have guarded code paths for debugging/future UI work.
- The HUD is display-only; it does not accept input and does not mutate gameplay state.
