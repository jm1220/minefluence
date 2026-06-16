# MineFluence Minimal Implementation Plan

This plan intentionally avoids gameplay implementation for the current documentation pass. Future Java work should keep the existing package name, Fabric versions, and dependency set unless a later task explicitly changes them.

## 1. Core Stat Data Model and Persistence

- Add a per-player MineFluence state model with Follower, Social Credibility, Lie Value, selected job, current mission index, posting state, and invasion state.
- Persist the state in a Fabric/Minecraft-appropriate server-side storage path for single-player worlds.
- Keep Lie Value hidden from normal UI.
- Create one central balance/config class for all rewards, thresholds, multipliers, weapon tier values, ending thresholds, and invasion values.

## 2. Debug Commands

- Add commands to inspect and modify MineFluence state during development.
- Include readouts for hidden Lie Value only in debug commands.
- Add reset/progress commands to speed up mission, invasion, and ending testing.

## 3. Job Selection Skeleton

- Create a job enum or registry with Farmer, Architect, and Cook.
- Make only Farmer selectable in the demo.
- Represent Architect and Cook as locked/unimplemented placeholders.
- Store the selected job in player state.

## 4. Farmer Mission Framework

- Define a mission interface or data structure with id, display name, description, base rewards, progress target, completion detection, and next-step behavior.
- Add a Farmer mission list containing exactly 7 missions.
- Keep mission progression separate from UI output and stat reward application.

## 5. Farmer Good Missions

- Implement the good Farmer route first:
  1. Plant 3 flowers in the garden.
  2. Plant 5 wheat seeds in a villager farm plot.
  3. Trade with a farmer villager twice.
  4. Distribute 10 potatoes to villagers.
  5. Craft 2 farmer job blocks.
  6. Craft and place 1 hay bale block in a shared village space.
  7. Build 1 farm plot for villagers.
- Leave bad Farmer mission ideas documented only until explicitly requested.

## 6. Normal/Exaggerated Posting Choice

- After each mission completion, pause progression until the player chooses a posting style.
- Normal posting applies base rewards.
- Exaggerated posting applies the central multiplier and increases Lie Value.
- Keep the posting system generic so future jobs can reuse it.

## 7. Invasion Manager

- Trigger invasions after Farmer missions 2, 5, and 7.
- Spawn Zombies for the first Farmer demo.
- Scale spawn count or wave strength from Social Credibility using central thresholds.
- Track active invasion mobs.
- Mark success when all spawned mobs are killed.
- Mark failure if the player dies during an active invasion.

## 8. Weapon Tier System

- Implement follower-based tier calculation:
  - Wood: 0+
  - Stone: 5+
  - Iron: 30+
  - Gold: 60+
  - Diamond: 90+
- Start with pure tier calculation and UI notification.
- Add automatic weapon replacement later after inventory behavior is decided.

## 9. Ending System

- Add Follower low/mid/high tier calculation.
- Add Social Credibility low/mid/high tier calculation.
- Combine both tiers into 9 endings.
- Prefer an `EndingType` and `EndingRegistry` style structure so future video/image assets can attach cleanly.
- Do not implement in-game MP4 decoding in the demo MVP; The Famous Villain uses PNG frame-sequence playback in Minecraft.

## 10. Basic UI Feedback

- Use chat for major events and choices.
- Use action bar for short progress messages.
- Use scoreboard/sidebar for Follower and Social Credibility.
- Use boss bar for invasion progress.
- Keep UI calls wrapped so custom GUI screens can replace them later.

## 11. Future Custom GUI and Asset Integration

- Replace chat-based job selection and posting choices with custom screens when gameplay is stable.
- Add ending image/video placeholders through the ending registry.
- Integrate teammate-provided ending assets after file formats, naming, and playback strategy are confirmed.
- Add Architect and Cook jobs after the Farmer route is complete and tested.
