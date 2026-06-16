# MineFluence

MineFluence is a Minecraft Fabric 1.21.1 mod prototype about influencer culture, social credibility, exaggerated posting, and community consequences.

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Java 21
- Fabric API for Minecraft 1.21.1
- GeckoLib for Fabric 1.21.1

## How to play the demo

For playtesting, download the latest `MineFluence_Playtest_Package.zip` from the Releases page.

1. Put the files inside `mods/` into your Minecraft `mods` folder.
2. Put `MINEFLUENCE_v1` into your Minecraft `saves` folder.
3. Put `minefluence_videos/` into your Minecraft game directory.
4. Launch Minecraft 1.21.1 with Fabric.
5. Open the `MINEFLUENCE_v1` world.
6. Use the MineFluence Smartphone in-game to progress through the demo.

Followers produce tagged Fan Villagers by tier, while Social Credibility
controls tagged Iron Golem support during invasions. MineFluence only applies
special behavior to `minefluence_fan` and `minefluence_invasion_support`
entities; normal villagers and Iron Golems keep vanilla behavior.

## Build from source

Windows:

```powershell
.\gradlew.bat build
