# 08 · Stats

**Practice log: total solves, improvement curve, PBs by difficulty. Free users see last 7 days; Pro users see everything + export.**

## Background

- **Unlocked (Pro or within free tier window):** `BG-1` (paper, light) + 1 small ink bleed behind the hero number (`ink_bleed_03.png`, 200.dp, alpha 0.08, centered behind the big number)
- **Locked (free user trying to access Pro-only section):** `BG-4` (ink night) — the deeper dark paper — to signal the section is gated

## Scroll behavior

**Scrollable.** Guaranteed to exceed viewport.

## Layout (unlocked, light mode)

```
┌────────────────────────────┐
│  ← Practice Log            │  header 48.dp
│                            │  gap 16.dp
│                            │
│          486               │  hero number Cormorant Italic 88sp
│                            │  gap 4.dp
│     puzzles solved,        │  subtitle Cormorant Italic 18sp
│       all time             │  inkSoft
│                            │  gap 40.dp
│                            │
│  THIS WEEK                 │  section label Inter 11sp UPPER WIDEST
│                            │  gap 12.dp
│  ┌────┬────┬────┬────┐     │  grid 2×2, cells 160×96.dp
│  │ 6  │7:12│ 3  │ 二 │     │  16.dp gap
│  │Slvd│Avg │Strk│Lvl │     │
│  └────┴────┴────┴────┘     │
│                            │  gap 40.dp
│                            │
│  IMPROVEMENT               │  section label
│                            │  gap 12.dp
│  ┌──────────────────────┐  │  card, radius md
│  │     [spark line]     │  │  240.dp tall
│  │                      │  │
│  │ 30 days              │  │
│  └──────────────────────┘  │
│                            │  gap 40.dp
│                            │
│  PERSONAL BESTS            │  section label
│  ┌──────────────────────┐  │
│  │ 一 Easy      4:12    │  │  rows 56.dp
│  │ 二 Medium    7:34    │  │
│  │ 三 Hard      12:08   │  │
│  │ 四 Expert    21:45   │  │
│  │ 五 Master    48:02   │  │
│  └──────────────────────┘  │
│                            │  gap 40.dp
│                            │
│  [ Export CSV ]            │  ghost button (Pro only)
│                            │
└────────────────────────────┘
```

## Sections

### Header

- Back arrow + title `Practice Log` (Cormorant Italic 22sp, ink)

### Hero number

- Number: Cormorant Garamond Italic 88sp, `Sumi.Color.ink`, center
- Subtitle: Cormorant Italic 18sp, `Sumi.Color.inkSoft`, center, 2 lines max
- The ink bleed sits behind; keep at 0.08 alpha

### This week grid (2×2)

Each cell:
- Top: value in Cormorant Italic 26sp ink (or kanji 28sp Shippori for Level)
- Bottom: label in Inter Medium 11sp UPPER WIDEST inkSoft
- Cell height 96.dp, radius md (8.dp), 1.dp border ink at 8% alpha

### Improvement card

- Height 240.dp, full-width, radius md, 1.dp ink border at 8%
- SVG/Canvas line chart — stroke `Sumi.Color.ink` at 2.dp, no fill, no grid lines
- X-axis labels (30 days) — tiny 11sp inkSoft inside the card bottom-left
- No tooltips, no dots on the line — it's a quiet trend line

### Personal bests list

Each row:
- Kanji (28sp Shippori) + label (Inter Medium 14sp) on left
- Best time (Cormorant Italic 22sp ink) on right
- Row height 56.dp
- Divider: 0.5.dp ink at 8% alpha between rows

### Locked state

For free users hitting Pro-only tiers:
- Background flips to BG-4 (ink night)
- Hero number replaced with kanji `錠` (lock) 96sp Shippori, `Sumi.Color.goldIvory`
- Message: "Full history is part of Sumi Pro."
- CTA: "See Pro" → paywall

## Resources used

| Resource | Where |
|---|---|
| `washi_paper_light.png` or `washi_ink_dark.png` | BG-1 or BG-4 |
| `ink_bleed_03.png` | behind hero number (unlocked) |
| Cormorant Italic | hero number, card values, PB times |
| Shippori Mincho | level kanji |
| Inter Medium | labels |

## Acceptance checklist

- [ ] Hero number is Cormorant Italic, 88sp, center
- [ ] Ink bleed sits behind the hero number, not floating elsewhere
- [ ] "This week" grid is 2×2 with equal cells
- [ ] Improvement line chart has no fill, no dots, no grid
- [ ] PB list shows 5 difficulties with kanji
- [ ] Free user sees last 7 days only; beyond that is a lock-gate
- [ ] Locked state uses BG-4 (dark ink) not BG-1
- [ ] Scroll works smoothly; no nested scroll
