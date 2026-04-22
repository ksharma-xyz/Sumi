# Brand

## Name

**Sumi** (墨) — Japanese for "ink stick" / "sumi ink", the ground-pine ink used for calligraphy and ink-wash painting.

Pronunciation: *SOO-mee*. Two beats, soft.

Tagline options (pick one at launch):
- *A quiet Sudoku.*
- *The daily practice.*
- *Nine lines. Nine columns. One patience.*

## Kanji

Three characters carry meaning in the app. They are part of the brand, not decoration.

| Kanji | Meaning | Where it appears |
|---|---|---|
| **墨** | Sumi / ink | App icon seal, wordmark subscript, Salon chop |
| **休** | Rest | Pause overlay |
| **完** | Complete | Win screen seal |

All rendered in **Shippori Mincho** (or Noto Serif JP fallback). Always in `SUMI.color.red` on a light ground or `SUMI.color.paper` inverted on red.

## Wordmark

"Sumi" in Cormorant Garamond, italic, medium weight, letter-spacing -0.02em, paired with 墨 in Shippori Mincho at ~40% of the Latin x-height, in red.

```
   Sumi  墨
  —————   (italic serif + red kanji)
```

SVG file: `svg/wordmark.svg`.

## Logo variations

Five marks available in `svg/`:

1. **Enso** (`logo-enso.svg`) — the zen brush circle with a "9" hidden in its curve. Primary mark. Use for app icon, splash, marketing hero.
2. **Grid** (`logo-grid.svg`) — nine-dot 3×3, center in red, one corner missing (the puzzle). Use for small/compact contexts and favicons.
3. **Chop** (`logo-chop.svg`) — red square seal with 墨. Use as a brand stamp on completion screens, marketing signatures.
4. **Nine** (`logo-nine.svg`) — a hand-brushed numeral 9. Alternative mark for game-focused contexts.
5. **Strokes** (`logo-strokes.svg`) — two horizontal brush strokes with a red center dot. Minimalist, editorial.

**Primary: Enso.** Use everywhere unless there's a specific reason to swap.

## App icon

Three variants in `svg/`:
- `app-icon-paper.svg` — Enso in ink on paper, red chop bottom-right. Use as default.
- `app-icon-ink.svg` — Inverted: paper Enso on ink. Use for night-mode Pro theme.
- `app-icon-red.svg` — Paper Enso on vermilion red. Use for launch marketing only.

App icon spec: 1024×1024, corner radius 22% (iOS convention; Android adaptive mask handles its own).

## Color (short version — see `kotlin/SumiTokens.kt` for full)

| Token | Hex | Role |
|---|---|---|
| ink | #1A1410 | Primary foreground |
| paper | #F4ECE0 | Primary surface |
| red | #A8342A | Chop / today / completion ONLY |
| teal | #2A5A6E | User-entered digits ONLY |
| gold | #8A6B2A | Pro surfaces ONLY |

## Voice & tone

Quiet. Attentive. Craft-focused. Slightly literary.

Read `DESIGN_PRINCIPLES.md §8` for copywriting rules. Short version: write like a craft magazine, not a mobile game.

## Don'ts

- Don't translate the kanji inside the UI ("rest 休" is wrong — 休 stands alone; English sits below in a smaller, muted label).
- Don't rotate or animate the wordmark.
- Don't place the Enso on a gradient or photographic background.
- Don't use the red chop on anything that isn't completion, "today", or The Salon.
