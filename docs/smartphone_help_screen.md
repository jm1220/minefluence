# Smartphone Help Screen

`MineFluenceSmartphoneHelpScreen` is available from normal smartphone states after the first-time tutorial flow.

## Current Layout

Title:

`MineFluence Help`

Description:

`Choose a help topic.`

Buttons:

- `Replay Tutorial`
- `Mission Help`
- `Game System Help`
- `Back`
- `Close`

`Back` returns to smartphone home. `Close` exits the smartphone UI. The centered panel and vertical topic list can be extended later with dedicated Lie System Help, Invasion Help, and Posting Help without changing mission or server state.

## Replay Tutorial

Replay mode opens the existing five-page tutorial screen without sending the tutorial-completion packet. Completing or closing a replay returns to Help. It does not start or reset the demo, select a job, clear missions, change rewards, or overwrite progress.

The first-time tutorial is separate. It is opened from the not-started smartphone screen and sends the existing completion packet on its final page. The server then starts the demo and selects Farmer through the shared services.

## Mission Help

`Mission Help` opens `MineFluenceSmartphoneMissionHelpScreen`.

The screen contains:

- `Farmer Missions (Good)` with all seven current Good mission titles and Korean completion instructions.
- `Farmer Missions (Bad)` with all seven current Bad mission titles and Korean completion instructions.
- `Back`, which returns to Help.
- `Close`, which exits the smartphone UI.

Mission headings come from `FarmerMissions`, and applicable target counts are read from each current `MineFluenceMission`. Bad Mission 7 intentionally tells the player to follow the current objective display instead of hardcoding its target.

The content is one scrollable document. Minecraft's `TextRenderer.wrapLines` wraps the UTF-8 Korean text to the current panel width. Mouse-wheel input changes a bounded scroll offset, text is clipped with a scissor rectangle, and a scrollbar indicates the current position. Buttons remain outside the scroll viewport.

The supplied guide wording differed from current gameplay detection in three places, so the displayed help follows the implemented behavior:

- Good Mission 3 currently counts two right-click interactions with farmer villagers, not completed trades.
- Good Mission 4 consumes one held potato per villager right-click; dropping potatoes with Q is not detected.
- Good Mission 7 requires one composter in the configured farm-build area. The two-composter/14-slab requirement applies to Good Mission 5.
- Bad Mission 6 currently completes after one valid farmland/crop block is destroyed in the configured farm area, not after destroying an entire plot.

## Game System Help

`Game System Help` opens `MineFluenceSmartphoneGameSystemHelpScreen`.

It uses the same scrollable panel behavior as Mission Help and contains:

1. `Weapon System`
2. `Invasion System`
3. `Villagers and Followers`
4. `Stats`
   - `Followers`
   - `Social Credibility`
   - `Lie Gauge`

Terminology consistently uses `Followers`, `Social Credibility`, `Lie Gauge`, `Invasion`, and `Weapon`. The compact gameplay HUD may still use `Trust` as a short label, but Help uses the official `Social Credibility` name.

### Weapon Thresholds

The screen reads the thresholds from `MineFluenceWeaponTier`, which currently resolves to:

| Weapon | Followers |
| --- | ---: |
| Wood | 0 |
| Stone | 5 |
| Iron | 30 |
| Gold | 60 |
| Diamond | 90 |

These match the requested values. Stronger Farmer Hoe tiers apply increasing bonus damage to tracked Invasion monsters.

### Invasion Strength

The screen reads the current monster counts from `MineFluenceBalance`:

| Strength | Monsters |
| --- | ---: |
| Weak | 1 |
| Medium | 3 |
| Strong | 5 |

These match the requested values. Invasions currently trigger after completed missions 2, 5, and 7. Each Invasion has separate Social Credibility thresholds:

| Invasion | Weak at or above | Strong at or below |
| --- | ---: | ---: |
| 1 | 30 | -21 |
| 2 | 150 | -150 |
| 3 | 200 | -200 |

Values between the weak and strong thresholds produce a Medium Invasion. Higher Social Credibility can also produce more village support defenders.

### Villagers And Followers

`Followers` is clamped to a maximum of 100. The Help text treats this as the size of the potential village audience and explains that Followers represent villagers influenced by the player's posts and actions.

The current world does not guarantee 100 loaded villager entities. Visible fan villagers are tiered feedback and currently scale only to a maximum of 10 fan entities, so Help does not claim that one entity is spawned for every Follower.

### Stats And Posting

Exaggerated posting uses the current `1.5x` multiplier for both Followers and Social Credibility rewards, rounded to a whole number. Negative Social Credibility rewards are also multiplied, making the penalty more negative. This matches `MineFluencePostingService`.

Exaggerated posts secretly increase the Lie Gauge by a mission-specific amount. The exact exposure threshold remains hidden from player-facing Help.

The requested wording said exposure removes all Followers and Social Credibility. Current `triggerExposureCollapse` does not reset those stored values; it clears mission/invasion flow and triggers the Famous Villain ending. The Help screen describes this implemented behavior instead of claiming a stat reset that does not occur.

## Navigation

Pressing Escape on Help returns to smartphone home. Pressing Escape on Mission Help or Game System Help returns to Help. The explicit `Close` buttons exit to gameplay. No navigation action changes player data.

## Rendering

The Help, Mission Help, and Game System Help screens render in this order:

1. Background
2. Panel and scroll frame
3. Title, description, and clipped help text
4. Buttons
