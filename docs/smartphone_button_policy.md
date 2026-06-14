# Smartphone Button Policy

The smartphone home screen uses one primary action per gameplay state. `Close` is always available. `Help` is available after the first-time tutorial flow.

## Buttons by State

| State | Home buttons |
| --- | --- |
| Demo not started | `Tutorial`, `Close` |
| Demo started, Farmer not selected | `Choose Farmer`, `Help`, `Close` |
| Ready for next mission | `Start Next Mission`, `Help`, `Close` |
| Waiting for Good/Bad choice | `Open Mission Board`, `Help`, `Close` |
| Active mission with an area | `Show Mission Area`, `Help`, `Close` |
| Active mission without an area | `Help`, `Close` |
| Waiting for upload | `Open Upload Screen`, `Help`, `Close` |
| Invasion active | `Help`, `Close` |
| Ending or exposure/collapse | `Play Ending Video` when supported, `Restart Demo`, `Help`, `Close` |

The active-mission status text always shows mission number, route, title, objective, progress, and either the required area or `Area: Not required`. Invasion counts and ending names are also displayed directly in the status panel.

## Removed Home Buttons

The following controls are no longer shown on the smartphone home:

- `Start Demo`
- Repeated `Tutorial`
- `Choose Good Mission`
- `Choose Bad Mission`
- `Post Normally`
- `Post Exaggerated`
- `View Progress`
- `View Ending`
- `Refresh Status`
- `Invasion Status`

The underlying server services remain authoritative. Route selection is performed only in `MineFluenceMissionScreen`, and posting selection is performed only in `MineFluencePostingScreen`.

## First-Time Flow

1. Open the smartphone before the demo starts.
2. Select `Tutorial`.
3. Complete the final tutorial page.
4. The server calls the existing demo-start service and then the existing Farmer-selection service.
5. The refreshed smartphone state is ready for the first mission.

Closing the first-time tutorial early does not start the demo.

## Mission and Upload Screens

`Start Next Mission` creates the pending mission-selection state. The home screen then exposes `Open Mission Board`. Mission Board route payloads require that pending state and are rejected during invasions or after an ending.

After mission completion, the home exposes only `Open Upload Screen`. Normal and exaggerated posting remain server-validated inside the upload screen.

## M Key

The MineFluence `M` keybind registration was removed. `M` no longer opens the mission board or auto-prepares a mission. Players enter mission selection through the smartphone.

## Limitations

- Slash commands remain available as demo/debug fallbacks.
- The smartphone is snapshot-based. Reopening it requests current server state, and server actions send a refreshed snapshot.
- Ending video playback remains external.
