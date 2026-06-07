# MineFluence Smartphone Home

The MineFluence smartphone is now the main in-game progression control screen for the demo. Right-clicking the smartphone opens the home screen instead of jumping directly to the mission board or upload screen.

The slash commands still exist as fallbacks for testing and debugging.

## Home Screen State

The home screen shows an on-demand server snapshot:

- Job
- Followers
- Social Credibility
- Lie Gauge percentage
- Mission count
- Weapon tier
- Active mission, pending route choice, pending upload, invasion, or ending state
- Next recommended action

The client only displays the snapshot. Gameplay state changes still happen on the server.

## Buttons

The visible buttons depend on the current state:

- `Start Demo` maps to `/minefluence start` behavior without automatically opening tutorial.
- `Tutorial` opens the existing tutorial screen.
- `Choose Farmer` maps to `/minefluence choose farmer`.
- `Start Next Mission` maps to `/minefluence mission next`.
- `Open Mission Board` opens the existing mission selection screen.
- `Choose Good` maps to `/minefluence mission choose good`.
- `Choose Bad` maps to `/minefluence mission choose bad`.
- `Open Upload Screen` opens the existing posting/upload screen.
- `Post Normally` maps to `/minefluence post normal`.
- `Post Exaggerated` maps to `/minefluence post exaggerate`.
- `Show Mission Area` maps to the existing mission area particle guide.
- `Play Ending Video` maps to `/minefluence ending video_test the_famous_villain` behavior when the triggered ending is The Famous Villain.
- `Restart Demo` maps to the same server start-demo reset used by the phone.

## Existing Screens

The home screen does not replace the detailed screens:

- The mission board still provides the Good/Bad mission card UI.
- The upload screen still provides the normal/exaggerated posting comparison UI.
- The tutorial screen is unchanged.
- The `M` key mission board behavior is unchanged.

## Current Limitations

- The phone uses vanilla buttons and text panels only.
- The phone does not render MP4 inside Minecraft; ending video playback remains external OS playback.
- The phone cannot complete mission objectives directly. Area missions still require the player to perform the existing gameplay actions.
- Bad mission detection remains whatever the existing mission system supports.
