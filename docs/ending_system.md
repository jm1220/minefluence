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

In-game MP4 rendering and custom ending screens are deferred. The MVP uses title, chat, and actionbar output, with a simple external OS video launch for The Famous Villain.

## External Video Hook

The Famous Villain ending (`the_famous_villain`, HIGH follower tier + LOW social tier) launches an external MP4 with the OS default player.

See `docs/ending_video_external.md` for setup and testing.
