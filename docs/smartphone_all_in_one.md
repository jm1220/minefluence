# MineFluence All-in-One Smartphone

The smartphone is the main player-facing progression and status tool. It requests a fresh server snapshot whenever it opens or refreshes. Buttons send action requests to the server; screen code never changes player data or applies rewards.

## Screen States

| State ID | Displayed state | Main next action |
| --- | --- | --- |
| `NOT_STARTED` | Not Started | Play the tutorial |
| `CHOOSE_JOB` | Choose Job | Choose Farmer |
| `READY` | Ready | Start the next mission |
| `MISSION_CHOICE` | Mission Choice | Open Mission Board |
| `MISSION_ACTIVE` | Mission Active | Complete the objective |
| `READY_TO_UPLOAD` | Ready to Upload | Open Upload Screen |
| `INVASION` | Invasion | Defend the village |
| `ENDING` | Ending | Play supported video or restart |
| `EXPOSED` | Exposed / Collapse | Play supported video or restart |

The player panel shows job, followers, signed Social Credibility, Lie Risk, current Farmer Hoe tier, and completed missions. Context details include mission title/objective/progress/area, invasion enemies and support allies, or the current ending.

## State-Aware Buttons

- Not started: `Tutorial`, `Close`
- Job selection fallback: `Choose Farmer`, `Help`, `Close`
- Ready: `Start Next Mission`, `Help`, `Close`
- Mission choice: `Open Mission Board`, `Help`, `Close`
- Active mission: conditional `Show Mission Area`, `Help`, `Close`
- Pending upload: `Open Upload Screen`, `Help`, `Close`
- Active invasion: `Help`, `Close`
- Ending/exposure: `Play Ending Video` when configured, `Restart Demo`, `Help`, `Close`

Impossible progression actions are not rendered. The server still validates every received action and returns a safe message when the saved state no longer matches the screen.

## Server Actions

The phone action IDs are:

- `START_DEMO`
- `OPEN_TUTORIAL`
- `CHOOSE_FARMER`
- `START_NEXT_MISSION`
- `CHOOSE_GOOD`
- `CHOOSE_BAD`
- `SHOW_MISSION_AREA`
- `POST_NORMAL`
- `POST_EXAGGERATE`
- `SHOW_INVASION_STATUS`
- `PLAY_ENDING_VIDEO`
- `RESTART_DEMO`

`MineFluenceNetworking` dispatches these requests on the server. It reuses:

- `MineFluenceDemoFlow.startDemo` and `chooseFarmer`
- `MineFluenceMissionSelectionService.prepareNextMission` and `chooseMission`
- `MineFluencePostingService.postMission`
- `MineFluenceAreaGuideManager.showArea`
- `MineFluenceInvasionManager` and `MineFluenceInvasionSupportManager` status queries
- `EndingVideoScreen`

The mission board, upload screen, and first-time tutorial completion also request a refreshed phone snapshot after their server action completes. Help replay is client-only and does not change player data.

## Lie Risk

The dashboard displays only the Stage 3 category:

- Stable
- Suspicious
- Dangerous
- Critical
- Exposed

The upload preview shows `None` for a normal post and `Hidden increase` for an exaggerated post. It does not display the exact per-mission Lie increase.

## Mission Areas

The server includes the configured area type for an active mission. `Show Mission Area` calls the existing particle guide. Missions without a fixed area show `Area: Not required` and do not render the guide button.

## Ending Video

`Play Ending Video` is shown only for an ending recognized as The Famous
Villain. It replays the same in-game PNG frame sequence used by the automatic
ending trigger: 169 frames, 10 fps, 640x360, no audio. Missing frame resources
show a safe in-screen fallback message instead of crashing.

## Rendering

The home screen renders in this order:

1. Background
2. Dashboard panel
3. Header and status text
4. Buttons

It does not call `super.render` after custom content, so a later background pass cannot cover or fade the dashboard.

## Persistence and Limitations

Player data now persists a `demoStarted` flag. Saves created before Stage 7 infer this flag from existing progression once, which preserves active demos while leaving untouched players in `NOT_STARTED`.

The old `M` key mission-board shortcut is no longer registered.

The smartphone snapshot is refreshed on demand and immediately after phone, mission-board, upload, and tutorial actions. Gameplay events such as mission completion and invasion cleanup continue updating the existing HUD; reopening or refreshing the smartphone reads their latest server state without forcing a screen open during combat.

Ending video playback is embedded in Minecraft through `EndingVideoScreen`.
Long objectives are wrapped and may be clipped at extremely small GUI
dimensions, while status and the next action are prioritized.
