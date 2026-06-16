# MineFluence Mission Selection

## Flow

`/minefluence mission next`

-> preview Good and Bad options for the next mission slot

-> `/minefluence mission choose good` or `/minefluence mission choose bad`

-> complete the selected objective

-> `/minefluence post normal` or `/minefluence post exaggerate`

Completed mission count increases only after posting. Invasions still trigger after posted mission counts 2, 5, and 7.

## Farmer Good Missions

1. Garden Starter: Plant 3 flowers in the configured garden area. Rewards: Follower +1, Social +10.
2. Wheat for the Village: Plant 5 wheat seeds in the configured farm area. Rewards: Follower +3, Social +30.
3. Local Farmer Collab: Complete 2 trades with a farmer villager. Rewards: Follower +5, Social +50.
4. Potato Giveaway: Right-click valid villagers with potatoes 10 times. Each click consumes 1 potato. Rewards: Follower +7, Social +70.
5. Workstation Boost: Craft and place 2 composters after starting the mission. Rewards: Follower +9, Social +90.
6. Shared Supplies: Place 1 hay bale in the configured shared space. Rewards: Follower +11, Social +110.
7. Community Farm Plot: Build a plot with 1 water, 8 farmland, and 1 composter. Rewards: Follower +14, Social +140.

## Farmer Bad Missions

1. False Alarm: Ring the village bell repeatedly as a false alarm. Rewards: Follower +2, Social -10.
2. Trample the Farm: Trample or destroy 5 farmland blocks. Rewards: Follower +5, Social -30.
3. Hit the Villagers: Hit villagers twice. Rewards: Follower +10, Social -50.
4. Steal from Villagers: Steal 10 items from a villager chest. Rewards: Follower +14, Social -70.
5. Break Farmer Job Blocks: Break 2 composters. Rewards: Follower +18, Social -90.
6. Destroy a Farm Plot: Destroy one villager farm plot. Rewards: Follower +23, Social -115.
7. Kill Villagers: Kill villagers. Rewards: Follower +29, Social -145.

## MVP Notes

- Good mission detection remains active through the existing area scans, villager interaction callbacks, and merchant trade completion hook.
- Bad mission detection is deferred; use `/minefluence mission complete_debug` for Bad missions in this stage.
- Exaggerated posting multiplies both Follower and Social rewards by `1.5`. Negative Social rewards become more negative.
- The future custom mission tab GUI or smartphone/camera item should call the same Good/Bad route selection flow instead of duplicating mission logic.
