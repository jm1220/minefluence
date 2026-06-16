# MineFluence Lie System

Stage 3 changes Lie Value into a hidden mission-specific exposure risk.

## Posting Rules

- Normal posting applies the existing normal follower and Social Credibility rewards.
- Normal posting does not increase Lie Value.
- Exaggerated posting applies the existing exaggerated reward multiplier.
- Exaggerated posting increases Lie Value by a hidden value based only on the completed mission index.
- Good and Bad routes use the same Lie increase for the same mission index.
- Player-facing post messages do not show the exact Lie increase.

## Hidden Mission Values

- Mission 1: 18
- Mission 2: 31
- Mission 3: 14
- Mission 4: 37
- Mission 5: 22
- Mission 6: 28
- Mission 7: 40

Invalid mission indexes return 0.

## Risk Labels

- 0 to 29: Stable
- 30 to 59: Suspicious
- 60 to 89: Dangerous
- 90 to 99: Critical
- 100 or higher: Exposed

The normal gameplay HUD does not show exact Lie Value or a Lie Gauge. It continues to show only Followers and Trust/Social Credibility.

## Exposure / Collapse

When an exaggerated post raises Lie Value to 100 or higher, Exposure/Collapse triggers once.

Exposure does the following:

- Persists an `exposureTriggered` flag in player data.
- Forces the ending id to `the_famous_villain`.
- Marks the ending as triggered so normal ending selection cannot overwrite it.
- Clears active mission and invasion state.
- Shows dramatic messages to the player.
- Sends the in-game The Famous Villain PNG frame-sequence video packet to the client.

If the video file is missing or the desktop video launcher is unavailable, the launcher warns the player and does not crash.

## UI Policy

- Smartphone status shows only the Lie Risk label.
- Smartphone upload preview shows `Lie Risk: None` for normal posts.
- Smartphone upload preview shows `Lie Risk: Hidden increase` for exaggerated posts.
- Exact mission-specific Lie values are not shown in normal player-facing UI.
- Exact Lie Value remains available through debug stats, including `/minefluence stats`.

## Limitations

- The mission-specific values are placeholder balance numbers and can be adjusted later.
- Existing networking payloads still carry reward preview integers for compatibility, but normal UI rendering does not display exact hidden Lie increases.
- Debug commands such as `/minefluence set lie <value>` and `/minefluence add lie <delta>` remain available for testing and can show exact Lie Value through debug output.
