# MineFluence Mission GUI

## Opening

The Mission Board opens from the smartphone only while a Good/Bad route choice is pending. The old `M` keybind registration has been removed.

## Purpose

The mission board chooses the Good or Bad route for the next Farmer mission slot. It mirrors the command flow:

`/minefluence mission next` -> `/minefluence mission choose good|bad`

The command flow remains supported for presentation fallback.

## Card UI

The current screen uses a card-based layout over the blurred/dimmed Minecraft background:

- Centered `Mission N` title.
- Left Good mission card with `good_icon.png`, green border, objective text, and reward rows.
- Right Bad mission card with `bad_icon.png`, red border, objective text, and reward rows.
- `Select Good` and `Select Bad` buttons align under their cards.

The icon assets are loaded from:

- `assets/minefluence/textures/gui/hud/good_icon.png`
- `assets/minefluence/textures/gui/hud/bad_icon.png`

## Server Authority

The client screen requests mission-board data from the server and displays the same Farmer mission definitions used by commands. Clicking Good or Bad sends only the selected route to the server. The server requires a pending mission choice and validates selected job, invasion/ending state, mission state, pending posting state, mission count, and required mission areas before starting the mission.

The card buttons still call the existing client networking method, which sends the same mission-choice payload used by the earlier screen implementation.

## Current Limitations

- Bad mission objective detection is still debug-only; use `/minefluence mission complete_debug`.
- Posting can be completed with commands or the smartphone upload screen.
