# MineFluence Map Area Preset

MineFluence uses mission areas for Farmer objectives such as planting flowers, planting wheat, placing hay bales, and building the final farm plot.

The demo map is reused, so fixed coordinates avoid manually standing in each area and running radius commands every time.

## Fixed Demo Coordinates

Preset coordinates are stored in:

`src/main/java/net/jeongmin/modid/area/MineFluenceDemoMapPreset.java`

The preset dimension is `minecraft:overworld`. The previous preset near the origin was replaced.

The shared village center used by Followers-based fan villagers and invasion
support allies is `(-846,65,726)`. Their spawn slots are arranged around that
center and use the same current-map coordinate region as the mission areas.

Fixed areas:

- `garden`: corners=`(-827,65,723)` and `(-839,65,711)`, stored min=`(-839,62,711)`, max=`(-827,71,723)`
- `farm`: corners=`(-857,64,689)` and `(-839,64,709)`, stored min=`(-857,61,689)`, max=`(-839,70,709)`
- `shared`: corners=`(-846,64,754)` and `(-833,65,764)`, stored min=`(-846,61,754)`, max=`(-833,71,764)`
- `farm_build`: corners=`(-859,68,729)` and `(-867,68,723)`, stored min=`(-867,65,723)`, max=`(-859,74,729)`

The stored Y range uses three blocks of padding below the lower corner and six blocks above the upper corner. Area bounds are inclusive.

## Mission Mapping

- Good Mission 1 -> `garden`
- Good Mission 2 -> `farm`
- Good Mission 6 -> `shared`
- Good Mission 7 -> `farm_build`
- Bad Mission 2 -> `farm`
- Bad Mission 4 -> `shared`
- Bad Mission 6 -> `farm`

Missions without a required area do not show an area guide.

## Load The Demo Preset

Run:

`/minefluence area load_preset`

This loads all required areas from the new fixed demo map preset and overwrites existing configured areas. Repeated runs update the same four named entries rather than creating duplicates.

`/minefluence start` and `/minefluence demo setup` also load missing preset areas automatically. They only fill missing areas and do not overwrite existing manual areas.

Starting an area-based mission and using the smartphone `Show Mission Area` action also restore missing preset definitions. Existing custom or old saved definitions are preserved by automatic loading; run `/minefluence area load_preset` once to migrate them to the new map.

Check the result with:

`/minefluence area list`

## Visual Guidance

Mission area guidance uses particles only. It does not modify map blocks.

When a mapped mission starts or a player requests a guide, MineFluence highlights the configured box for about five seconds. Good routes use `happy_villager`, Bad routes use `angry_villager`, and manual commands use neutral `end_rod` boundary particles. Every guide also marks the center with an `end_rod` pillar.

The guide searches the configured Y range for open space above solid ground near the area center.

## Show An Area Manually

Use:

- `/minefluence area show garden`
- `/minefluence area show farm`
- `/minefluence area show shared`
- `/minefluence area show farm_build`

The selected area is highlighted briefly with the same particle guide used by active missions.

Use `/minefluence area info <area>` to inspect one saved area.

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

- `garden: BOX min=(-839,62,711), max=(-827,71,723), dimension=minecraft:overworld`
- `farm: BOX min=(-857,61,689), max=(-839,70,709), dimension=minecraft:overworld`
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
