# 06 · Pause

**Modal overlay shown when the player taps the pause icon mid-game. Blurs the game underneath, places the kanji 休 and a gentle rest message, offers Resume / End session.**

## Background (the one that was wrong)

`BG-5` — see BACKGROUNDS.md §BG-5 for the full recipe.

**Critical:** the scrim alpha is **0.82** (light) / **0.88** (dark), NOT 0.4. If the board is readable behind the pause screen, the scrim is wrong. Rebuild.

Three layers:
```
z=0  Live game snapshot (frozen)
z=1  24.dp gaussian blur of z=0
z=2  paper scrim at 0.82 / 0.88 alpha
z=3  content: kanji 休, label, timer, buttons
```

Implementation is in BACKGROUNDS.md §BG-5. Copy it verbatim.

## Scroll behavior

**Not scrollable.** Modal fits viewport by construction. If a test device makes it overflow, shrink button heights to 48.dp and reduce gaps.

## Layout (390 × 844 reference)

```
┌────────────────────────────┐
│                            │
│                            │
│                            │
│            休              │  kanji 160sp, Shippori, red
│                            │  gap 12.dp
│    A moment of rest        │  Cormorant Italic 28sp
│                            │  gap 8.dp
│  Your time is paused.      │  body 15sp, inkSoft, center
│  Return when you are       │
│  ready.                    │
│                            │
│                            │  gap 40.dp
│          4:32              │  timer, Cormorant Italic 40sp
│                            │
│                            │  gap 48.dp
│    [   Resume   ]          │  primary, full-width
│                            │  gap 12.dp
│    [ End session ]         │  ghost, full-width
│                            │
│                            │
└────────────────────────────┘
```

## Sections

### Kanji 休

- Shippori Mincho Medium, 160sp
- Color: `Sumi.Color.red` = #8B1D1F (the seal red, not ink)
- Centered, no background
- On mount: scale from 0.92 → 1.0 over 380ms with `Sumi.Ease.bleed`, alpha 0 → 1
- Reduced-motion: instant

### Label

- `A moment of rest` — Cormorant Garamond Italic 28sp, `Sumi.Color.ink`
- Centered, single line

### Body copy

- `Your time is paused. Return when you are ready.`
- Source Serif 4 Regular 15sp, `Sumi.Color.inkSoft`, center, line-height 22sp
- Max-width 280.dp

### Timer

- Cormorant Italic 40sp, `Sumi.Color.ink`, center
- Format `m:ss` — ticks are frozen during pause (show the elapsed at pause moment, do not continue)

### Buttons

1. Primary `Resume` — full-width minus 32.dp margin, 52.dp tall
2. Ghost `End session` — full-width, 52.dp, 1.dp border `Sumi.Color.ink` at 40% alpha
3. Gap 12.dp between

## Animation timing

```
t=0ms    overlay mounts: blur + scrim appear (instant, no fade)
t=0ms    kanji starts scale-in
t=380ms  kanji settled
t=200ms  label fades in (260ms)
t=360ms  body copy fades in (260ms)
t=480ms  timer fades in (200ms)
t=600ms  buttons fade in (200ms)
```

Dismiss: reverse order, 180ms blur removal + scrim fade.

## Resources used

| Resource | Where |
|---|---|
| Shippori Mincho Medium | kanji 休 |
| Cormorant Italic | label, timer |
| Source Serif 4 Regular | body copy |
| Inter | button labels |

No background image — the blurred game + scrim IS the background.

## Acceptance checklist

- [ ] The game behind is blurred ~24.dp — shapes suggest, details do not read
- [ ] Scrim alpha is 0.82 (light) / 0.88 (dark). The board is **not** readable through it.
- [ ] Kanji 休 is in red seal color, not ink
- [ ] Timer is frozen, not counting
- [ ] Two buttons stacked, primary on top
- [ ] Reduced-motion: all content appears instantly, no sequencing
- [ ] Tapping outside the content (but inside the modal) does NOT dismiss — only Resume/End session do
- [ ] Back gesture maps to Resume
