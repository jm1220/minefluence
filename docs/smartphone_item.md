# MineFluence Smartphone Item

## Item

- ID: `minefluence:smartphone`
- Display name: `MineFluence Smartphone`
- Registered in `MineFluenceItems`

## Placeholder Assets

- Model: `src/main/resources/assets/minefluence/models/item/smartphone.json`
- Texture: `src/main/resources/assets/minefluence/textures/item/smartphone.png`

The current texture is a temporary 16x16 phone icon. The final icon can be added later by replacing only:

`src/main/resources/assets/minefluence/textures/item/smartphone.png`

Keep the same filename and path so the existing item model continues to work.

## How To Obtain

- `/minefluence phone give`
- `/minefluence start`
- `/minefluence choose farmer`
- `/minefluence demo setup`

The setup commands normalize the player inventory to one MineFluence Smartphone instead of adding duplicates.

## Right-Click Behavior

Right-clicking the smartphone opens the existing MineFluence mission board screen on the client. The screen requests the mission board state from the server using the existing mission board networking.

If a mission choice is not ready, the existing mission board messaging explains the current state, such as choosing Farmer first, previewing the next mission slot, completing an active mission, or posting a finished mission.

## Existing M Key

The `M` keybind still opens the same mission board screen. The smartphone is an in-game item route to the same UI, not a replacement for the keybind.

## Current Limitations

- The texture is placeholder art.
- The smartphone currently opens the mission board only; it does not record or upload mission content yet.
- `/minefluence phone remove` removes smartphone stacks from the player's inventory slots, not from external containers.

## Future Plan

Use the smartphone as the recording/upload item for mission completion and posting flows once those interactions are ready.
