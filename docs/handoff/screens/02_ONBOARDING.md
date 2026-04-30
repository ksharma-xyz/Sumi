# 02 · Onboarding

**Three swipeable slides shown to new users before first Home. Each slide is a single poetic statement + one visual. Horizontal pager with dot indicator.**

## Background

`BG-1` (paper, light) on all 3 slides.

Each slide gets **one ink bleed** positioned differently so the set feels hand-arranged:

| Slide | Ink bleed | Size | Position | Rotation | Alpha |
|---|---|---|---|---|---|
| 1 | `ink_bleed_02.png` | 320.dp | top-right, 40.dp inset | 12° | 0.10 |
| 2 | `ink_bleed_03.png` | 300.dp | bottom-left, 60.dp inset | -8° | 0.12 |
| 3 | `ink_bleed_01.png` | 360.dp | centered behind final kanji | 0° | 0.08 |

## Scroll behavior

**Horizontal swipe** between slides via `HorizontalPager`. **Vertical scroll within each slide is enabled** — on short phones the copy can run long. `Modifier.verticalScroll(rememberScrollState())` inside each page.

## Global layout (applies to every slide)

```
┌────────────────────────────┐ <- safe top
│                    Skip →  │  top bar: 52.dp tall
│                            │
│                            │
│   [kanji/visual, ~200.dp]  │  visual block
│                            │
│                            │
│                            │  gap 40.dp
│   Display headline         │  Cormorant Italic 34sp
│                            │  max 2 lines
│                            │
│                            │  gap 16.dp
│   Supporting copy over     │  Source Serif 16sp
│   two or three lines.      │  max 3 lines
│                            │
│                            │
│                            │  flex spacer
│                            │
│      ● ● ○                 │  dot indicator
│                            │  gap 24.dp
│   [  Continue  ]           │  primary button 52.dp
│                            │
└────────────────────────────┘ <- safe bottom 40.dp
```

- Horizontal padding: 32.dp (wider than default 24.dp — this is a marketing screen)
- Text alignment: **center** for headline and supporting copy
- Visual block: centered horizontally
- Button: full-width minus 32.dp padding, height 52.dp

## Slide 01 — "The practice"

**Visual:** the Ensō circle, 200.dp, drawn once on slide mount (same algorithm as splash, but no kanji inside).

**Copy:**

> *Sudoku as quiet practice.*
>
> Ink on paper. No chrome. No noise.
> Nine by nine, every day.

- Headline: `Sudoku as quiet practice.` (Cormorant Italic 34sp, `Sumi.Color.ink`)
- Supporting: 3 lines above, Source Serif 16sp, `Sumi.Color.inkSoft`, line-height 24sp

**Button:** "Continue" (primary)

## Slide 02 — "The hand"

**Visual:** three `Caveat`-drawn digits `3 5 9` at 96sp, scattered with slight rotation: `3` rotated -6°, `5` rotated 0°, `9` rotated 8°. Color `Sumi.Color.ink`. 24.dp gap between digits.

**Copy:**

> *Every mark is yours.*
>
> Tap to enter. Hold to pencil.
> Your hand, your pace, your pause.

**Button:** "Continue"

## Slide 03 — "The rest"

**Visual:** the kanji `休` at 160sp, Shippori Mincho Medium, color `Sumi.Color.red` (`#8B1D1F`), centered. Behind it, the ink_bleed_01 at 360.dp alpha 0.08.

**Copy:**

> *Rest is part of the practice.*
>
> Pause without penalty. Return without cost.
> The grid waits.

**Button:** "Begin" (primary, note copy change from "Continue")

## Top bar

- Height 52.dp
- No background fill (sits directly over paper)
- Right-aligned "Skip" ghost button (body 15sp, `Sumi.Color.inkSoft`)
- Skip advances straight to Home

## Dot indicator

- 3 dots, 8.dp diameter, 8.dp gap
- Active dot: `Sumi.Color.ink`, full opacity
- Inactive: `Sumi.Color.ink` at 24% alpha
- Animate crossfade 240ms with `Sumi.Ease.paper` when slide changes

## Transition between slides

- Default swipe provided by `HorizontalPager`
- Override `pageSpacing` to 0, set `flingBehavior` with a stiff decay so slides feel inked-in, not flicky
- On slide change, fade the visual 180ms then fade in the new one over 320ms

## Resources used

| Resource | Where |
|---|---|
| `washi_paper_light.png` | BG-1 |
| `ink_bleed_01.png` / `02.png` / `03.png` | per-slide accents |
| Shippori Mincho Medium | slide-3 kanji |
| Cormorant Garamond Italic | all headlines |
| Source Serif 4 Regular | all body copy |
| Caveat Regular | slide-2 hand digits |

## State variations

- Skip from any slide → Home
- Last slide "Begin" → Home with a `paperSlide` transition
- Back gesture goes to previous slide; on slide 1, swallowed (no back-out to splash)

## Acceptance checklist

- [ ] All three slides use the same paper background; the ink bleeds differ in placement per slide
- [ ] Headlines are italic (Cormorant Italic)
- [ ] Body copy is serif (Source Serif), not sans
- [ ] Dot indicator shows current position clearly
- [ ] On a 340×640 device, supporting copy does NOT clip — vertical scroll inside the slide kicks in if needed
- [ ] "Skip" is always reachable in the top-right; never hidden under status bar
- [ ] "Begin" on slide 3 uses different copy from "Continue" on slides 1 and 2
- [ ] Reduced-motion: slide transitions become instant; Ensō draw becomes instant
