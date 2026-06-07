# MineFluence Custom HUD

## Assets

- `src/main/resources/assets/minefluence/textures/gui/hud/good_icon.png`
- `src/main/resources/assets/minefluence/textures/gui/hud/bad_icon.png`
- `src/main/resources/assets/minefluence/textures/gui/hud/invasion_icon.png`
- `src/main/resources/assets/minefluence/textures/gui/hud/lie_icon.png`

`good_icon.png` is used for Good missions and Social Credibility. `bad_icon.png` is used for Bad missions and as the current Followers placeholder. `invasion_icon.png` is used for invasion objectives. `lie_icon.png` is used for Lie Gauge.

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

## Stat Panel

The right-side stat panel shows:

- Followers
- Social Credibility
- Lie Gauge

Lie Gauge is calculated from the synced server value:

```text
liePercent = lieValue * 100 / MineFluenceBalance.LIE_VALUE_MAX
```

The client constant `SHOW_LIE_GAUGE` in `MineFluenceHudOverlay` can hide this row later without changing gameplay state.

## Sync

The server remains authoritative. `MineFluenceHud.refresh` still updates the existing scoreboard HUD and now also sends `MineFluenceHudStatePayload` to the player. A periodic server tick sync sends the same snapshot once per second so passive mission/invasion progress stays current.

The snapshot contains stats, mission display details, pending posting metadata, invasion index/progress, and ending display text. The client stores the latest snapshot in `MineFluenceHudState` and renders it in `MineFluenceHudOverlay`.

`/minefluence hud refresh` manually sends the current snapshot to the command user.

## Limitations

- The Followers row currently uses `bad_icon.png` as a placeholder because no dedicated follower icon was provided.
- The existing scoreboard sidebar remains enabled for now.
- The HUD is display-only; it does not accept input and does not mutate gameplay state.
