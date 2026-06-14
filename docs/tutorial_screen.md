# MineFluence Tutorial Screen

## Assets

The tutorial uses five full-screen image pages:

- `src/main/resources/assets/minefluence/textures/gui/tutorial/tutorial_1.png`
- `src/main/resources/assets/minefluence/textures/gui/tutorial/tutorial_2.png`
- `src/main/resources/assets/minefluence/textures/gui/tutorial/tutorial_3.png`
- `src/main/resources/assets/minefluence/textures/gui/tutorial/tutorial_4.png`
- `src/main/resources/assets/minefluence/textures/gui/tutorial/tutorial_5.png`

Texture identifiers:

- `minefluence:textures/gui/tutorial/tutorial_1.png`
- `minefluence:textures/gui/tutorial/tutorial_2.png`
- `minefluence:textures/gui/tutorial/tutorial_3.png`
- `minefluence:textures/gui/tutorial/tutorial_4.png`
- `minefluence:textures/gui/tutorial/tutorial_5.png`

Replace the images later by overwriting those PNG files with the final tutorial artwork.

## Page Order

`MineFluenceTutorialScreen` starts on `tutorial_1.png` and advances through `tutorial_5.png`.

## Rendering

The screen draws a black fallback background first, then draws the current tutorial image over it. The image is scaled with a 16:9 cover fit, centered on the screen. This fills the viewport while preserving the tutorial art aspect ratio; very wide or very tall screens may crop the edges.

No vanilla button graphics or extra text are drawn on top of the tutorial art.

## Click Area

The tutorial art already contains the visible Next/Play button. The implementation uses an invisible click target:

- X: 62% to 85% of screen width
- Y: 82% to 92% of screen height

Clicking this area advances pages 1 through 4. On page 5, it closes the tutorial and sends a server packet to continue the demo.

Keyboard support:

- Enter: next/play
- Space: next/play
- Escape: close screen without sending Play

## Opening

Player commands:

- `/minefluence tutorial`
- `/minefluence tutorial open`

The not-started smartphone screen opens the tutorial as the player-facing start path. `/minefluence start` still performs the existing command-based demo reset/start behavior.

## Play Behavior

On the final page of the first-time tutorial, Play sends `MineFluenceTutorialPlayPayload` to the server. If the demo has not started, the server calls the existing demo-start service and then the shared Farmer-selection flow. The client does not mutate player data directly.

Help uses a separate replay mode. Replay does not send the completion payload and returns to Help, so it cannot reset the demo or alter current progress.

## Limitations

- The button hitbox is based on relative screen coordinates. If future art moves the button, update the ratios in `MineFluenceTutorialScreen`.
- The image scaling assumes 16:9 tutorial art.
- Closing with Escape skips the Play packet and does not auto-select Farmer.
