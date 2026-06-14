# Mission Supplies And Weapon Damage

## Mission Starter Supplies

Supplies are granted server-side when a mission becomes the active mission. Previewing the mission board, using posting choices, and using `complete_debug` do not grant supplies.

Good Farmer supplies:

- Mission 1, Garden Starter: `3x poppy`. Poppy is the chosen simple Minecraft flower item.
- Mission 2, Wheat for the Village: `5x wheat seeds`.
- Mission 3, Local Farmer Collab: no item required.
- Mission 4, Potato Giveaway: `10x potatoes`.
- Mission 5, Workstation Boost: no supplies. The objective is to obtain 2 composters after the mission starts, so directly giving composters would satisfy or undermine the mission.
- Mission 6, Shared Supplies: `1x hay bale`.
- Mission 7, Community Farm Plot: `1x water bucket`, `1x composter`, `8x wheat seeds`.

Mission 7 does not grant a basic hoe. MineFluence already uses vanilla hoes as managed Farmer weapons, so adding a weak hoe as a supply item could conflict with the weapon upgrade/removal logic or confuse tier detection.

Bad Farmer missions currently receive no supplies. Their current objectives are debug-only or destructive and do not have clean item prerequisites, so this is intentional for Stage 1.

## Duplicate Prevention

`MineFluencePlayerData` persists the last mission route/index that received supplies:

- `suppliesGrantedMissionIndex`
- `suppliesGrantedRoute`

The marker is cleared when a new mission starts and when mission flow is cleared, including `/minefluence start`, `/minefluence reset`, and posting cleanup. The supply system marks a mission as granted before giving items, so repeated calls while the same mission is active do not duplicate supplies.

If the inventory cannot accept a stack, the leftover stack is dropped near the player instead of crashing.

## Farmer Hoe Invasion Damage

Farmer hoe bonus damage is applied only to the primary player attack damage amount against active tracked MineFluence invasion invaders.

Placeholder bonus values:

- Wooden Farmer Hoe: `+2.0`
- Stone Farmer Hoe: `+4.0`
- Iron Farmer Hoe: `+6.0`
- Golden Farmer Hoe: `+8.0`
- Diamond Farmer Hoe: `+10.0`

The values live in `MineFluenceBalance` and are placeholders for later balancing.

## Detection And Limits

Valid attackers must be server-side players with the Farmer job who are holding a MineFluence Farmer hoe tier item: wooden, stone, iron, golden, or diamond hoe.

Valid targets must be active invasion targets tracked in `MineFluencePlayerData`, still alive, and either DDJ entities or entities with the MineFluence invasion tag.

The damage is injected into the vanilla primary `PlayerEntity.attack` damage call, so it is not applied every tick and does not apply to unrelated vanilla mobs. Crafted vanilla hoes held by a Farmer can match the same item-tier fallback because the current weapon system uses vanilla hoe items rather than custom item IDs or NBT markers.
