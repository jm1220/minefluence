# Mission Detection

## Good Mission 3

Good Mission 3 is `Local Farmer Collab` with the objective:

`Complete 2 trades with a farmer villager.`

Opening a villager trade screen does not progress the mission. Completed trades
are detected from `TradeOutputSlot.onTakeItem` through `TradeOutputSlotMixin`,
immediately after Minecraft calls `Merchant.trade(...)`. That point is reached
only after the selected offer can deplete the required input items.

The detector requires:

- A server-side `ServerPlayerEntity`
- Farmer selected
- Good Mission 3 active
- No pending posting choice
- No triggered ending
- A valid mission villager merchant
- A farmer villager, or a MineFluence fan villager tagged `minefluence_fan`

## Good Mission 4

Good Mission 4 is `Potato Giveaway` with the objective:

`Right-click villagers with potatoes 10 times.`

Distribution is detected from `UseEntityCallback` on the server. The target must
be a valid mission villager, and the stack in the hand that triggered the
interaction must be `Items.POTATO`. Each valid right-click decrements exactly
one potato with Minecraft's normal creative-mode exemption and increments
mission progress by one.

Invalid targets, empty hands, non-potato items, completed missions, and pending
upload states do not consume potatoes.

## Good Mission 5

Good Mission 5 is `Workstation Boost` with the objective:

`Craft and place 2 composters after starting this mission.`

Two separate server-authoritative counters are stored in
`MineFluencePlayerData`:

- Composters crafted while Good Mission 5 is active
- Composters successfully placed while Good Mission 5 is active

Crafting is detected from `ItemStack.onCraftByPlayer` through
`ItemStackCraftMixin`. The reported crafted amount is added only when the
crafted stack is a composter.

Placement is detected after a successful `BlockItem.place` through
`BlockItemPlaceMixin`. The final placed block must be a composter.

Both hooks require:

- A server-side `ServerPlayerEntity`
- Farmer selected
- Good Mission 5 active
- No pending posting choice
- No triggered ending

The mission completes only when:

`craftedComposters >= 2 && placedComposters >= 2`

Inventory possession no longer progresses this mission. Composters obtained
before mission start, supplied by commands, or picked up from the ground do not
count as crafted. Composters placed before mission start do not count as
placed. Bad Mission 5 breaking detection remains separate and unchanged.

## Progress Display

Detailed command/status text reports both values:

`Crafted 1/2, Placed 0/2`

The existing HUD and smartphone payload support one numeric progress value, so
they display placed composters as `0/2`, `1/2`, or `2/2`. Reaching `2/2` placed
does not complete the mission until the crafted counter is also `2/2`.

The English objective and Korean smartphone help text now state that the player
must craft and place the composters.

## Persistence And Reset

Both counters are persisted in MineFluence player data. They reset when a new
mission starts or mission flow is cleared. Debug completion uses the existing
completion service and does not increment either counter.
