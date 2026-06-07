# External Ending Video Playback

This version does not play MP4 video inside Minecraft.

For The Famous Villain ending, MineFluence launches the MP4 with the operating system default video player using Java `Desktop.open`.

## Demo File Location

Copy the video file to:

`run/minefluence_videos/the_famous_villain.mp4`

Reason: `Desktop.open` needs a real file on disk. Files packaged inside a mod jar may not behave like normal files.

The development fallback path is:

`src/main/resources/assets/minefluence/textures/billboard/ending/the_famous_villain.mp4`

## Testing

Use:

`/minefluence ending video_test`

or:

`/minefluence ending video_test the_famous_villain`

Both commands attempt to launch `the_famous_villain.mp4` externally.

## Common Issues

- The file is missing from `run/minefluence_videos/the_famous_villain.mp4`.
- The OS has no default MP4 player installed.
- Java `Desktop` or `Desktop.Action.OPEN` is not supported in the current environment.
- Dedicated/headless server environments may not be able to open desktop applications.
