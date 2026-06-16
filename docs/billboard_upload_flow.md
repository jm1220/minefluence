# MineFluence Billboard Upload Flow

The MineFluence billboard system displays uploaded mission thumbnails on village billboard anchors.

## Current Behavior

When a player successfully posts a completed mission, the shared server posting service updates every loaded billboard anchor in group `main`.

Command posting and smartphone posting both call `MineFluencePostingService.postMission`, so both paths use the same billboard upload resolver.

## Mission Thumbnail Mapping

Automatic uploads use the 28 packaged mission-specific billboard images:
7 missions x Good/Bad route x Normal/Exaggerated posting style. The resolver
maps mission number, route, and posting style directly to the filename
convention documented in `docs/billboard_upload_mapping.md`.

If the expected upload texture is missing, the server logs the requested
mission, route, posting style, and path, then falls back safely to
`assets/minefluence/textures/billboard/default.png`. The client renderer also
falls back to the same default image if a manually selected texture is missing.

## Posting Integration

After rewards are applied, mission flow is cleared, completed mission count is incremented, and the weapon update runs, the posting service resolves the uploaded mission thumbnail from:

- mission index
- mission route
- posting style

Then it calls the billboard group updater for group `main`.

This avoids duplicating upload logic in commands or GUI networking.

## Placing The Main Billboard

Use:

`/minefluence billboard give`

Place the `MineFluence Billboard Anchor` block in the village. Look at the placed anchor and assign it to the main upload group:

`/minefluence billboard group main`

Manual testing commands:

- `/minefluence billboard list`
- `/minefluence billboard set mission_1_good_normally`
- `/minefluence billboard info`
- `/minefluence billboard size 12 6`
- `/minefluence billboard group_set main mission_1_good_normally`

## Billboard Size

Use this command while looking at a placed billboard anchor:

`/minefluence billboard size <width> <height>`

Examples:

- `/minefluence billboard size 4 3`
- `/minefluence billboard size 8 4`
- `/minefluence billboard size 12 6`
- `/minefluence billboard size 16 9`

Preset shortcuts are also available:

- `/minefluence billboard preset small` -> `4x3`
- `/minefluence billboard preset medium` -> `8x4`
- `/minefluence billboard preset large` -> `12x6`
- `/minefluence billboard preset huge` -> `16x9`

Recommended building-wall sizes:

- Small shop sign: `4x3`
- Village notice board: `8x4`
- Large wall ad: `12x6`
- Wide building advertisement: `16x9`

Size limits:

- Width: `1` to `32` blocks
- Height: `1` to `18` blocks

The billboard anchor is the bottom-center of the rendered image. The image extends left and right from the anchor and grows upward from it. Place the anchor at the bottom middle of the wall area where the advertisement should appear.

The renderer draws the image slightly in front of the wall-facing side to avoid z-fighting.

## MVP Limitations

Group updates scan loaded chunks around online players. Billboards in unloaded chunks are not updated by this MVP pass.
