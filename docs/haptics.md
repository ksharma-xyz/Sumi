# Sumi — Haptics

Haptics are controlled via the `HapticEngine` interface. They are gated behind the **Haptics** toggle in Settings (on by default).

## When haptics fire

| Event | Level | Android (API 28+) | iOS |
|---|---|---|---|
| Cell tap | `tick` | `CLOCK_TICK` | `UIImpactFeedbackStyleLight` |
| Tool press (undo / erase / notes toggle) | `tick` | `CLOCK_TICK` | `UIImpactFeedbackStyleLight` |
| Note pencil mark added | `tick` | `CLOCK_TICK` | `UIImpactFeedbackStyleLight` |
| Correct digit placed | `confirm` | `CONFIRM` (API 30+) / `VIRTUAL_KEY` | `UIImpactFeedbackStyleMedium` |
| Wrong digit placed | `error` | `REJECT` (API 30+) / `LONG_PRESS` | `UINotificationFeedbackTypeError` |
| Puzzle complete (game win) | `win` | `CONFIRM` (API 30+) / `VIRTUAL_KEY` | `UINotificationFeedbackTypeSuccess` |

## What does NOT fire haptics

- Navigating between screens
- Petal animations
- Timer ticks
- Aurora sweep animations

## Implementation

- `HapticEngine` — `composeApp/src/commonMain/.../haptic/HapticEngine.kt`
- Android impl — `HapticEngine.android.kt` (uses `View.performHapticFeedback`, no VIBRATE permission)
- iOS impl — `HapticEngine.ios.kt` (uses `UIImpactFeedbackGenerator` + `UINotificationFeedbackGenerator`)
- Wired at `GameEntry` (all in-game callbacks) and `WinEntry` (win event)
- Settings toggle persisted via DataStore (`ThemePreferences.observeHapticsEnabled()`)

## Adding a new haptic event

1. Identify which level fits: `tick` (light), `confirm` (medium), `error` (rejection), `win` (success)
2. Inject `HapticEngine` via `rememberHapticEngine()` in the Entry composable (never in a screen)
3. Wrap with the enabled gate: `if (hapticsEnabled) haptic.levelName()`
