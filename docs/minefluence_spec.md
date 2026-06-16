# MineFluence Demo Spec

## Project Baseline

- Minecraft version: 1.21.1
- Mod loader: Fabric
- Java target: 21+
- Mod ID: `minefluence`
- Current main package: `net.jeongmin.modid`
- Scope: single-player demo

Do not rename packages, change Gradle/Fabric versions, add gameplay dependencies, or implement multiplayer behavior during the initial demo.

## Core Concept

MineFluence is a Minecraft influencer-simulation and social-dilemma mod. The player becomes an influencer in a village and tries to gain followers and social credibility to help defend the village from invasions.

The central dilemma is whether the player gains influence honestly or by using exaggerated and provocative content. Honest content keeps trust stable. Exaggerated content grows faster but increases a hidden lie score that can trigger later penalties.

## Core Stats

### Follower

- Public follower count.
- Used for weapon upgrade tiers.
- Can be shown in scoreboard/sidebar, chat summaries, or action bar feedback.

### Social Credibility

- Public social trust score.
- Used for invasion difficulty and villager support.
- Can be shown in scoreboard/sidebar, chat summaries, or action bar feedback.

### Lie Value

- Hidden score.
- Increased by exaggerated posting and future dishonest actions.
- Should not be shown directly to the player in normal UI.
- Can be inspected later through debug commands.
- Penalty thresholds may use temporary values for the demo, but all values must live in a central balance/config class so they are easy to rebalance.

## Balance Strategy

All numeric gameplay values should be centralized in one balance/config class before gameplay implementation, for example `MineFluenceBalance` under the existing package tree. Avoid scattering hard-coded reward, threshold, spawn, or tier values through mission and invasion code.

Initial placeholder values can be simple and temporary:

- Initial follower: `0`
- Initial social credibility: `50`
- Initial lie value: `0`
- Exaggerated posting multiplier: `1.5`
- Exaggerated posting lie increase: temporary constant, for example `10`
- Ending follower tiers: low/mid/high thresholds to be finalized
- Ending credibility tiers: low/mid/high thresholds to be finalized
- Invasion difficulty thresholds: temporary values based on social credibility

## Demo Flow

1. Player starts or joins a single-player world with MineFluence installed.
2. Player selects a job.
3. For the first demo, only Farmer is selectable.
4. Player completes 7 Farmer missions.
5. After each mission, player chooses normal or exaggerated posting.
6. Invasions occur after mission 2, mission 5, and mission 7.
7. After mission 7 and invasion 3, the ending is calculated.

## Job Selection

The demo should include a job-selection concept.

- Farmer: implemented and selectable first.
- Architect: locked or unimplemented placeholder later.
- Cook: locked or unimplemented placeholder later.

The job system should be structured so future jobs can add their own mission lists, reward tables, and invasion flavor without rewriting the core progression system.

## Farmer Missions

There are exactly 7 Farmer missions in the demo. The first implementation should use the good Farmer route first.

### Good Farmer Mission Route

1. Plant 3 flowers in the garden.
2. Plant 5 wheat seeds in a villager farm plot.
3. Trade with a farmer villager twice.
4. Distribute 10 potatoes to villagers.
5. Craft 2 farmer job blocks.
6. Craft and place 1 hay bale block in a shared village space.
7. Build 1 farm plot for villagers.

### Bad Farmer Mission Ideas

These are future mission concepts and should not be implemented in the first gameplay pass unless explicitly requested.

1. Ring the village bell repeatedly as a false alarm.
2. Trample or destroy 5 farmland blocks.
3. Hit a villager twice.
4. Steal 10 items from a villager chest.
5. Break 2 farmer job blocks.
6. Destroy one villager farm plot.
7. Kill villagers.

## Mission Posting

After each completed mission, the player chooses how to post the content.

### Normal Posting

- Rewards are applied as defined by the mission.
- Lie Value does not increase.
- Tone should feel honest and stable.

### Exaggerated Posting

- Follower and credibility rewards are multiplied by `1.5`.
- Lie Value increases by a central balance constant.
- The UI should communicate that the post was exaggerated without exposing the exact Lie Value.

All reward values are temporary placeholders for the first demo. The code should allow each mission to define base rewards while the posting system applies the normal or exaggerated result.

## Invasions

Invasions are scripted demo events tied to Farmer mission progression.

- Invasion 1 occurs after mission 2.
- Invasion 2 occurs after mission 5.
- Invasion 3 occurs after mission 7.
- Farmer demo invasions should use Zombies first.
- Invasion strength depends on Social Credibility.
- Temporary thresholds are allowed.

MVP success condition:

- All spawned invasion monsters are killed.

MVP failure condition:

- Player dies during the invasion.

The first implementation can use simple spawn counts and waves. Later versions can add villager support, varied monsters, defense objectives, and penalties from high Lie Value.

## Weapon Upgrade Design

Weapon tier depends on Follower count:

| Tier | Follower Requirement |
| --- | ---: |
| Wood | 0+ |
| Stone | 5+ |
| Iron | 30+ |
| Gold | 60+ |
| Diamond | 90+ |

Implement later as an automatic upgrade or replacement system. The first design should keep tier calculation separate from inventory mutation so the UI, commands, and final weapon implementation can share the same tier logic.

## Ending System

There are 9 total endings. The ending is determined by combining:

- Follower tier: low, mid, high
- Social Credibility tier: low, mid, high

This creates a 3 x 3 matrix of endings.

Ending thresholds may be temporary for the demo, but should be centralized in the balance/config class. The ending system should be designed with a future-friendly `EndingRegistry` and `EndingType` style structure so video or image assets can be attached later.

Ending video playback does not decode MP4 inside Minecraft. The Famous Villain demo ending uses packaged PNG frames for in-game playback, while other endings can use chat, title, actionbar output, or future image/video assets.

## UI Strategy

The first implementation should use vanilla-friendly UI feedback:

- Chat messages for mission start, completion, posting choice, and ending summaries.
- Action bar for short progress updates.
- Scoreboard/sidebar for public stats such as Follower and Social Credibility.
- Boss bar for active invasion status.

Do not build a polished custom GUI yet. Structure UI calls behind small service/helper methods so custom GUI screens can replace chat/action bar/scoreboard/boss bar output later.
