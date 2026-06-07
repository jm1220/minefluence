# Smartphone Posting

## Overview

The MineFluence Smartphone now supports upload/posting choices in addition to opening the mission board.

The command fallback still exists:

- `/minefluence post normal`
- `/minefluence post exaggerate`

Both command posting and GUI posting call the same server-side posting service.

## State-Based Smartphone Behavior

Right-clicking `minefluence:smartphone` asks the server for the current phone state.

- Pending mission selection: opens the existing mission selection screen.
- Active mission: opens a simple mission status screen with progress and guidance.
- Completed mission waiting for posting: opens the upload/posting screen.
- No pending mission flow: opens a guidance screen, usually prompting `/minefluence mission next`.

The existing `M` key still opens the mission board directly.

## Upload Screen

The upload screen displays:

- Mission slot number
- Mission route
- Mission title
- Mission objective
- Normal post rewards
- Exaggerated post rewards

Normal posting shows base mission rewards and `Lie Value +0`.

Exaggerated posting shows the same reward preview used by the server:

- Follower reward multiplied by `MineFluenceBalance.EXAGGERATED_POST_MULTIPLIER`
- Social Credibility reward multiplied by the same value, including negative rewards
- Lie Value increased by `MineFluenceBalance.LIE_VALUE_PER_EXAGGERATED_POST`

## Server Authority

The client only displays previews and sends the selected posting mode. The server applies rewards, clamps stats, increments mission completion, clears mission state, updates weapons, and starts invasions after missions 2, 5, and 7.

## Current Limitations

- The status and upload screens are simple vanilla-widget screens.
- The smartphone does not yet perform camera recording or content capture.
- The upload screen closes immediately after sending the posting choice; final results are shown through existing chat/action-bar feedback.
