# Animations & Motion

All motion tokens in `SumiTokens.Duration` + `SumiTokens.Ease`. Defaults in `DESIGN_PRINCIPLES.md §9`.

---

## AuroraSweep — the completion ceremony

The signature motion of Sumi. Fires when a house (row/col/box) or the full board completes.

### Visual

A soft aurora-like gradient sweeps across the completed region. Multi-stop `LinearGradient` across 4–5 hues on paper, or full spectrum on win. Travels perpendicular to the house (row → left-to-right, column → top-to-bottom, box → diagonal).

### Parameters

```kotlin
sealed class BoardSweep {
    data class Row(val index: Int) : BoardSweep()
    data class Col(val index: Int) : BoardSweep()
    data class Box(val index: Int) : BoardSweep()
    object Win : BoardSweep()
}
```

### Timings

| Kind | Duration | Hue range | Ease |
|---|---|---|---|
| Row | 1000ms | 4 stops, soft | `Ease.bleed` |
| Column | 1000ms | 4 stops, soft | `Ease.bleed` |
| Box | 1200ms | 4 stops | `Ease.bleed` |
| Digit complete | 800ms | 2-hue pulse | `Ease.paper` |
| Win | 3000ms | full spectrum | `Ease.bleed` |
| Correct placement | 400ms | single cell, ink-bleed, no hue | `Ease.paper` |

### Hues

Paper mode (`AURORA_HUES_PAPER`):
```
#E8B4A8  (soft coral)
#B8D4C8  (mint)
#C8B4D8  (lavender)
#D4C88A  (wheat)
#B4C4D8  (sky)
```

Night mode (`AURORA_HUES_DARK`):
```
#6A4E42  (ember)
#425C52  (forest)
#524660  (plum)
#5C5638  (brass)
#3A4658  (indigo)
```

Alpha 0→0.55 center→0 across the travel axis, multiplied by an overall fade envelope (in/out quarters).

### Implementation sketch (Compose)

```kotlin
@Composable
fun AuroraSweep(
    kind: BoardSweep,
    cellSize: Dp,
    tone: AuroraTone = AuroraTone.Paper,
) {
    val t by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = when (kind) { is BoardSweep.Win -> 3000; else -> 1000 },
            easing = Sumi.Ease.bleed,
        ),
    )
    Canvas(...) { /* draw gradient band positioned at t along travel axis */ }
}
```

See `reference/sumi/aurora.jsx` for the exact CSS/SVG composition if you need a pixel reference.

---

## PaperBreath — ambient background animation

Very subtle. Runs at low intensity on splash, home idle, and pause screens.

- The WashiBG noise's displacement seed slowly rotates: 0.85Hz baseFrequency modulated ±0.02 over 8s, sinusoidal.
- Inner vignette shadow pulses from 60.dp → 68.dp → 60.dp over 8s.
- Off during active solves.

In Compose: drive a `rememberInfiniteTransition` that updates a `Float` passed to the WashiBG's noise shader and vignette radius.

---

## PetalFall — splash / opening moment

Used once on first-run splash, optionally on home idle if user lingers >20s.

- 8–12 cherry-blossom petal SVG shapes fall from above top edge.
- Each petal: random x start, random rotation speed (0.3–0.6 rev/s), random size (8–18.dp), random fall duration (4–8s), slight horizontal sway (cosine, amplitude 20.dp over full fall).
- Color: `#F0D4D0` at 60% opacity, or `#E8B4A8` at 40%.
- Never more than 12 on-screen at once.

Reference: `handoff/reference/sumi/petals.jsx`.

---

## InkSettle — digit placement

When user enters a number:
- Cell bg flashes to `rgba(teal, 0.15)` for 80ms, then fades to selected state over 160ms.
- Digit fades in 0→1 over 120ms, `Ease.paper`, with a tiny 0.95→1 scale (NO overshoot).

---

## Press states

All pressable surfaces use custom ink-darken:
- `onPress`: bg interpolates to current bg blended with `ink` at 8% over 60ms.
- `onRelease`: reverses over 140ms.

Do NOT use `ripple()` or any Material indication.

---

## Screen transitions

- Home → Game: crossfade 380ms, `Ease.paper`. No slide.
- Game → Win: see BOARD.md "Completion detection" — aurora → seal stamp → Win content fades in.
- Game → Pause: existing blurred snapshot under a paper overlay that slides up 18.dp + fades in 0→0.88 alpha over 300ms.
- Any → Paywall: fade to night, 560ms, `Ease.bleed`.

---

## The snap confirmation

Only used on one moment: the `Seal` stamping down on Win. Uses `Ease.snap` (slight overshoot past -5° to -7° then settles to -5°) over 400ms + a single soft shadow-bounce.

Nowhere else.
