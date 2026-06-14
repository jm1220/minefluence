# Mission Clear Feedback

## Trigger

`MineFluenceMissionCompletionService.complete` is the shared server-authoritative transition from an active mission to the pending-upload state.

It is used by:

- normal Good mission completion;
- normal Bad mission completion; and
- `/minefluence mission complete_debug`.

The effect does not run for mission selection, previews, partial progress, posting, HUD refreshes, smartphone refreshes, or an already pending mission.

## Title And Sound

When the transition succeeds, only the player who completed the mission receives:

```text
Mission <N> Clear!
Open your smartphone to upload your post.
```

Title timing:

- fade in: 10 ticks;
- stay: 40 ticks;
- fade out: 10 ticks.

The sound is the vanilla `SoundEvents.UI_TOAST_CHALLENGE_COMPLETE` event at volume `1.0` and pitch `1.0`, sent directly to the completing player through the master sound category.

## Duplicate Prevention

The completion service requires an active mission and rejects players already waiting for a posting choice. In the same successful call it:

1. sets progress to the mission target;
2. copies the active mission into pending posting state;
3. clears the active mission state; and
4. sends the title and sound.

After that transition, later detector ticks, status refreshes, smartphone requests, and relogs cannot satisfy the active-mission guard. No additional persisted feedback flag is required.

Starting another mission creates a new active mission transition, allowing the next mission to produce its own one-time feedback.

## Rewards

Mission clear feedback does not grant Followers, Social Credibility, or Lie Gauge changes. Those values continue to be applied only by `MineFluencePostingService` after the player selects a posting style.

The existing pending-upload smartphone state, Upload Screen, chat guidance, HUD refresh, and locator-hiding behavior remain unchanged.
