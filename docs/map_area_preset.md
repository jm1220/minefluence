# MineFluence Map Area Preset

MineFluence uses mission areas for Farmer objectives such as planting flowers, planting wheat, placing hay bales, and building the final farm plot.

The demo map is reused, so fixed coordinates avoid manually standing in each area and running radius commands every time.

## Fixed Demo Coordinates

Preset coordinates are stored in:

`src/main/java/net/jeongmin/modid/area/MineFluenceDemoMapPreset.java`

The preset dimension is `minecraft:overworld`.

Fixed areas:

- `garden`: min=`(-15,-64,11)`, max=`(-6,-50,21)`
- `farm`: min=`(5,-64,9)`, max=`(35,-50,37)`
- `shared`: min=`(11,-64,-18)`, max=`(20,-50,-5)`
- `farm_build`: min=`(-16,-64,23)`, max=`(-5,-50,34)`

## Mission Mapping

- Good Mission 1 -> `garden`
- Good Mission 2 -> `farm`
- Good Mission 6 -> `shared`
- Good Mission 7 -> `farm_build`

Missions without a required area do not show an area guide.

## Load The Demo Preset

Run:

`/minefluence area load_preset`

This loads all required areas from the fixed demo map preset and overwrites existing configured areas.

`/minefluence start` and `/minefluence demo setup` also load missing preset areas automatically. They only fill missing areas and do not overwrite existing manual areas.

Check the result with:

`/minefluence area list`

## Visual Guidance

Mission area guidance uses particles only. It does not modify map blocks.

While an active Good mission requires an area, MineFluence periodically highlights the configured box boundary with `happy_villager` particles and marks the area center with a small `end_rod` particle pillar.

The guide renders at the area's center Y. For the fixed demo map boxes, this is around Y `-57`.

## Show An Area Manually

Use:

- `/minefluence area show garden`
- `/minefluence area show farm`
- `/minefluence area show shared`
- `/minefluence area show farm_build`

The selected area is highlighted briefly with the same particle guide used by active missions.

## Set An Exact Box

Use:

`/minefluence area set_box <type> <x1> <y1> <z1> <x2> <y2> <z2>`

Examples:

- `/minefluence area set_box garden 120 64 -30 135 70 -15`
- `/minefluence area set_box farm 80 63 20 100 70 45`

The command normalizes the two corners automatically, so either corner can be entered first.

## Existing Radius Command

The old center/radius workflow still works:

`/minefluence area set garden 8`

This stores a radius area centered on the player position.

## Area List Output

`/minefluence area list` shows shape and coordinates:

- `garden: BOX min=(-15,-64,11), max=(-6,-50,21), dimension=minecraft:overworld`
- `farm: BOX min=(5,-64,9), max=(35,-50,37), dimension=minecraft:overworld`
- `shared: MISSING`

## Finding Coordinates

Use Minecraft's F3 debug screen and read the player's block position while standing at two opposite corners of the intended area.

Record:

- minimum/first corner position
- maximum/opposite corner position
- dimension, currently preset to `minecraft:overworld`

Then update `MineFluenceDemoMapPreset` or run `/minefluence area set_box` directly.

## Persistence

Preset-loaded and set-box areas are saved through the existing MineFluence world-state persistence. They should remain configured after saving and reloading the world.
