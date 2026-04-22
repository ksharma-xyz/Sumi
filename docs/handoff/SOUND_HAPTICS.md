# Sound & Haptics

Sumi is silent by default. When sound is on, it's organic — brush, paper, water, wood.

## Global rules

- **Default: sound off.** Haptics on. User-togglable in Settings.
- No synthesized chimes, no "success!" fanfare, no loops.
- One sound per discrete action. Never layered.
- All sounds ≤ 600ms.

## Sound spec

Audio files live in `composeApp/src/commonMain/composeResources/files/sounds/` (or `raw/` depending on your resources plugin). Format: 44.1kHz mono MP3 or WAV, normalized to -6dB.

| Event | File | Duration | Source |
|---|---|---|---|
| Cell select | — | — | silent (haptic only) |
| Digit enter (correct) | `water-drop.wav` | 300ms | single water drop, near/wet |
| Mistake | `ink-splash.wav` | 140ms | tiny ink splash on paper |
| Note toggle | `brush-soft.wav` | 160ms | very soft brush stroke |
| Undo | `paper-rustle.wav` | 180ms | paper page turn, short |
| Hint reveal | `brush-dip.wav` | 280ms | brush dipping in ink |
| Menu open | `scroll-unroll.wav` | 240ms | scroll unrolling, ends abruptly |
| Pause / Resume | `bell-wood.wav` | 320ms | wooden temple bell, muted |
| House complete (row/col/box) | `woodblock-soft.wav` | 400ms | single woodblock tap |
| Puzzle solved | `woodblock.wav` | 600ms | two woodblock taps, spaced 180ms |
| Seal stamp (Win) | `seal-press.wav` | 400ms | leather press + paper crush |
| Pro unlock | `incense-strike.wav` | 500ms | one match strike + flutter |

Recording direction: record real sources where possible (water drop, brush on paper, woodblock). Avoid sampled libraries that sound synthesized.

## Implementation

Shared interface in `commonMain`:

```kotlin
expect class SumiAudio() {
    fun play(event: SumiSound)
    fun setEnabled(enabled: Boolean)
}

enum class SumiSound {
    WaterDrop, InkSplash, BrushSoft, PaperRustle, BrushDip,
    ScrollUnroll, BellWood, WoodblockSoft, Woodblock, SealPress, IncenseStrike,
}
```

- **androidMain:** `SoundPool` with 8-stream pool, pre-loaded on app start.
- **iosMain:** `AVAudioPlayer` instances per sound, cached.
- **desktopMain:** `javax.sound.sampled.Clip`.

## Haptic spec

| Event | Android | iOS |
|---|---|---|
| Cell select | `HapticFeedbackConstants.CLOCK_TICK` or light vibration 10ms | `.soft` |
| Digit enter | `CONFIRM` (light) | `.light` |
| Mistake | `REJECT` / strong 40ms | `UIImpactFeedbackGenerator.rigid` |
| Undo | tick 15ms | `.soft` |
| Note toggle | tick 10ms | `.soft` |
| House complete | `CONFIRM` | `UINotificationFeedbackGenerator.success` single |
| Puzzle solved | double CONFIRM (400ms apart) | `.success` then `.success` 400ms later |
| Seal stamp | strong 60ms | `UIImpactFeedbackGenerator.heavy` |
| Menu open | tick | `.soft` |

Shared API:

```kotlin
expect object SumiHaptics {
    fun perform(feedback: SumiHaptic)
}

enum class SumiHaptic {
    Soft, Light, Rigid, Heavy, Success, SuccessDouble,
}
```

## Settings

A single switch: "Sounds · on / off". Haptics inherit system-wide haptics preference (respect `Settings.System.HAPTIC_FEEDBACK_ENABLED` on Android; iOS auto-respects).
