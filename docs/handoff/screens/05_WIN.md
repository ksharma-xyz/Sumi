# 05 · Win

**Shown after the final digit is placed correctly. Celebratory but quiet — a seal stamp, time, difficulty, and a "Continue" / "Share" choice. Scrollable because stats can be long.**

## Background

`BG-1` (paper, light).

**Two ink bleeds:**

| Bleed | File | Size | Position | Rotation | Alpha |
|---|---|---|---|---|---|
| A | `ink_bleed_02.png` | 280.dp | top-left, 0.dp off the edge (partially clipped) | 0° | 0.08 |
| B | `ink_bleed_03.png` | 220.dp | bottom-right, 0.dp off the edge (partially clipped) | 0° | 0.10 |

Neither is tinted — they are the same neutral ink as on other screens. Do not color-shift them to gold or red.

## Scroll behavior

**Scrollable.** The hero + seal + stats + buttons sometimes exceed 640 height on short phones.

## Layout (390 × 844 reference)

```
┌────────────────────────────┐ <- safe top
│ [ink bleed A peek]         │
│                            │
│                            │
│        完 · Complete       │  kanji 28sp + label
│                            │  gap 4.dp
│          Sumi              │  wordmark italic 18sp
│                            │  gap 48.dp
│                            │
│      [SEAL — red]          │  120.dp seal with kanji
│                            │  gap 40.dp
│                            │
│     The grid is quiet      │  Cormorant Italic 24sp
│         again.             │  2 lines, center
│                            │
│                            │  gap 32.dp
│                            │
│    ┌──────┬──────┬──────┐  │  stats row — 3 cells
│    │ Time │ Moves│ Level│  │  cell 96.dp tall
│    │ 8:42 │  213 │ 二   │  │
│    └──────┴──────┴──────┘  │
│                            │  gap 32.dp
│                            │
│  [  Next puzzle  ]         │  primary, full-width
│                            │  gap 12.dp
│  [    Share     ]          │  ghost, full-width
│                            │
│ [ink bleed B peek]         │
└────────────────────────────┘
```

## Sections

### Kanji heading

- `完 · Complete`
  - Kanji 完 in Shippori 28sp, `Sumi.Color.seal` (#8B1D1F)
  - Separator `·` Inter 14sp, `Sumi.Color.inkSoft`
  - Label `Complete` Inter Medium 13sp UPPER WIDEST, `Sumi.Color.inkSoft`
- Below: wordmark `Sumi` in Cormorant Italic 18sp, `Sumi.Color.ink`
- Block centered, 4.dp between kanji line and wordmark
- 32.dp top padding after safe area

### Seal

See BRAND.md §Seal for exact draw.

- 120.dp × 120.dp
- `Sumi.Color.seal` (#8B1D1F) red-clay fill
- Kanji inside (rotates per difficulty): 一 二 三 四 五 at Shippori 64sp, `Sumi.Color.paper` (cream)
- Slight rotation: -4° for handcraft
- Shadow: none; edge texture via a 4.dp semi-transparent stroke at 40% seal color
- Appears with a 340ms scale-in (0.88 → 1.0) + 200ms alpha fade, `Sumi.Ease.snap`
- Haptic: single heavy tap synchronized to scale peak

### Quote

- Text: `The grid is quiet again.` (static — not rotated; always this copy)
- Cormorant Italic 24sp, line-height 32sp, `Sumi.Color.ink`, center align, max-width 280.dp

### Stats row

Three equal cells. Each cell:

- Label (top): Inter Medium 11sp UPPER WIDEST, `Sumi.Color.inkSoft`
- Value (below): Cormorant Italic 28sp, `Sumi.Color.ink`
- For `Level`, value is the difficulty kanji (一/二/三/四/五) in Shippori 28sp
- Cell height 96.dp, no dividers — whitespace separates them
- Horizontal: equal weight (`Row { Modifier.weight(1f) }`)

Values:

| Label | Example |
|---|---|
| TIME | `8:42` |
| MOVES | `213` |
| LEVEL | `二` |

If it's a new personal best, add a small red dot (4.dp, `Sumi.Color.seal`) to the right of the value.

### Buttons

1. Primary: `Next puzzle` — full-width, 52.dp, `SumiButton.Primary`
2. Ghost: `Share` — full-width, 52.dp, `SumiButton.Ghost` (no fill, 1.dp ink border at 40% alpha, text `Sumi.Color.ink`)
3. 12.dp between

## Resources used

| Resource | Where |
|---|---|
| `washi_paper_light.png` | BG-1 |
| `ink_bleed_02.png`, `ink_bleed_03.png` | corner bleeds |
| Shippori Mincho | 完, level kanji, seal kanji |
| Cormorant Italic | quote, stats values, wordmark |
| Inter Medium | labels |

## State variations

- **Personal best:** red 4.dp dot after time and/or moves value
- **Last puzzle in series / Daily:** swap `Next puzzle` → `Back to Home`
- **Share not available on device:** hide Share button, upsize primary to keep center alignment

## Animations

- On mount: paper appears instantly; seal animates in at t=200ms; quote fades in at t=400ms; stats at t=600ms; buttons at t=800ms
- Easing: paper for text, snap for seal
- Reduced motion: all appear instantly, no sequencing

## Acceptance checklist

- [ ] Background is paper, not dark. Warm cream (#F4ECE0).
- [ ] Two ink bleeds sit at opposite corners, partially off-canvas, low alpha
- [ ] Seal is red-clay, not burgundy; has the correct kanji for the difficulty solved
- [ ] "The grid is quiet again." — exact copy, italic, center
- [ ] Stats row reads Time / Moves / Level, each label UPPER WIDEST
- [ ] Two buttons stacked, Primary on top
- [ ] Screen scrolls on a 340×600 viewport; nothing clips
- [ ] Seal scale-in animation honors reduced-motion
- [ ] No confetti, no particles, no gradients
