# MineFluence Smartphone Home

The MineFluence smartphone is now the main in-game progression control screen for the demo. Right-clicking the smartphone opens the home screen instead of jumping directly to the mission board or upload screen.

The slash commands still exist as fallbacks for testing and debugging.

## Home Screen State

The home screen shows an on-demand server snapshot:

- Job
- Followers
- Social Credibility with signed formatting
- Lie Risk level instead of exact Lie Value
- Weapon tier
- Completed mission count
- Active mission, pending route choice, pending upload, invasion, or ending state
- Next recommended action

The client only displays the snapshot. Gameplay state changes still happen on the server.

Lie Risk labels:

- `Stable`: 0 to 29
- `Suspicious`: 30 to 59
- `Dangerous`: 60 to 89
- `Critical`: 90 to 99
- `Exposed`: 100+

The exact Lie Value is intentionally hidden from normal gameplay HUDs. It remains available through debug/status commands that are explicitly meant for testing.

## Detailed Task Area

The smartphone is the detailed status/control screen. Depending on server state, the current task section shows:

- Pending mission selection: `Mission Choice: N/7` and `Choose Good or Bad.`
- Active mission: mission index, Good/Bad route, title, objective, progress, and required area when applicable.
- Pending posting: `Ready to Upload`, mission index/route, and content title.
- Active invasion: invasion index and invaders remaining.
- Ending reached: ending display name.

The `Next:` line gives a concise recommended action, such as playing the first tutorial, starting the next mission, opening the Mission Board, completing the objective, opening the Upload Screen, defending the village, or playing/restarting after an ending.

## Buttons

The visible buttons depend on the current state. First-time players receive `Tutorial`; completing it starts the demo and selects Farmer. Later states expose one primary action such as `Start Next Mission`, `Open Mission Board`, conditional `Show Mission Area`, or `Open Upload Screen`.

`Help` replaces the repeated Tutorial button after startup. Ending states expose only supported video playback, restart, Help, and Close.

## Existing Screens

The home screen does not replace the detailed screens:

- The mission board is the only smartphone UI that provides Good/Bad selection.
- The upload screen is the only smartphone UI that provides normal/exaggerated posting selection.
- Help can replay the tutorial without changing server progression.
- The `M` key mission-board shortcut has been removed.

## Current Limitations

- The phone uses vanilla buttons and text panels only.
- The phone does not decode MP4 inside Minecraft. The Famous Villain ending video replays through the in-game PNG frame sequence screen.
- The phone cannot complete mission objectives directly. Area missions still require the player to perform the existing gameplay actions.
- Bad mission detection remains whatever the existing mission system supports.
- The phone shows rough Lie Risk only, not exact Lie Value.
