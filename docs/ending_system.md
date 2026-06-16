# MineFluence Ending System

The MVP ending system has 9 endings from a 3 x 3 matrix:

| Follower Tier | Social Tier | Ending |
| --- | --- | --- |
| LOW | LOW | Nobody |
| LOW | MID | Quiet Neighbor |
| LOW | HIGH | Law-abiding Citizen |
| MID | LOW | Troublemaker |
| MID | MID | Local Creator |
| MID | HIGH | Honored Citizen |
| HIGH | LOW | The Famous Villain |
| HIGH | MID | Mega Influencer |
| HIGH | HIGH | Everyone's Role Model |

## Temporary Thresholds

- Follower LOW: 0-29
- Follower MID: 30-69
- Follower HIGH: 70-100
- Social Credibility LOW: -500 to -1
- Social Credibility MID: 0-199
- Social Credibility HIGH: 200-500

All thresholds live in `MineFluenceBalance` so they can be rebalanced later.

## Trigger

The ending triggers automatically once the player has completed all 7 demo missions and cleared Invasion 3. The player data stores `endingTriggered` and `endingId` so the ending does not repeat every tick or after reload.

## Media Plan

Each ending definition includes placeholder asset paths for future teammate-provided media:

- Follower media: `assets/minefluence/endings/follower_<tier>.png`
- Social media: `assets/minefluence/endings/social_<tier>.png`
- Combined ending media: `assets/minefluence/endings/ending_<follower_tier>_<social_tier>.png`

The Famous Villain ending uses a client-only in-game PNG frame sequence screen.
MP4 decoding is not used inside Minecraft.

## Ending Video Hook

The Famous Villain ending (`the_famous_villain`, HIGH follower tier + LOW social
tier) sends `minefluence:play_ending_video` to the client. The client opens
`EndingVideoScreen` and plays:

`assets/minefluence/textures/ending/the_famous_villain/frame_0000.png`

through:

`assets/minefluence/textures/ending/the_famous_villain/frame_0168.png`

Current settings are 169 frames at 10 fps, 640x360, no audio. The smartphone
`Play Ending Video` button replays the same in-game screen.

See `docs/ending_video_external.md` for setup and testing.
