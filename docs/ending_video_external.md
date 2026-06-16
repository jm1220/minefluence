# Ending Video Playback

The Famous Villain ending now plays in-game using a PNG frame sequence. MineFluence
does not decode MP4 video inside Minecraft and does not require FFmpeg, VLC,
JCodec, or native video dependencies.

## Frame Assets

Frames are packaged at:

`src/main/resources/assets/minefluence/textures/ending/the_famous_villain/`

Current playback asset settings:

- 169 frames
- `frame_0000.png` through `frame_0168.png`
- 10 fps
- 640x360
- No audio

The in-game texture identifiers use:

`minefluence:textures/ending/the_famous_villain/frame_0000.png`

through:

`minefluence:textures/ending/the_famous_villain/frame_0168.png`

## Playback Flow

When The Famous Villain ending is triggered, the server keeps the normal ending
state update and sends `minefluence:play_ending_video` to the client. The client
opens `EndingVideoScreen`, which renders the frames full-screen with black
letterboxing/pillarboxing as needed.

The smartphone `Play Ending Video` button sends the existing phone action to the
server. If the current ending is The Famous Villain, the server sends the same
`minefluence:play_ending_video` packet so playback restarts from frame 0000.

`/minefluence ending video_test` also uses this in-game packet for quick manual
testing.

## Missing Frames

If a frame is missing from the loaded resource pack, playback does not crash.
The screen shows the missing frame path and logs a warning once for that frame.

The old MP4 file under `textures/billboard/ending/` is not used by the in-game
ending playback path.
