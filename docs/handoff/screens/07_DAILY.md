# 07 · Daily

**A calendar of the month with solved / unsolved / today markers, and quick access to today's puzzle. Always scrollable.**

## Background

`BG-1` (paper, light).

**One small ink bleed:** `ink_bleed_02.png` at 120.dp, alpha 0.06, positioned at top-right 24.dp inset. Intentionally subtle — this screen is about the calendar grid, not decoration.

## Scroll behavior

**Scrollable.** List of past days can extend beyond viewport; the "Previous months" section appends rows on scroll.

## Layout

```
┌────────────────────────────┐
│  ←  Daily                  │  header, 48.dp
│                            │  gap 16.dp
│    April                   │  h2 Cormorant Italic 36sp
│    14 of 22 days solved    │  meta Inter 13sp inkSoft
│                            │  gap 24.dp
│                            │
│   M T W T F S S            │  day letters, Inter 11sp WIDER
│   1 2 3 4 5 6 7            │  numbers — each cell 40×40.dp
│   8 ● ● ● ● ● ●            │  ● = solved (filled seal dot)
│  ● ● ● ○ · · ·             │  ○ = today, · = future
│                            │
│                            │  gap 32.dp
│   Today                    │  section label Inter 11sp UPPER WIDEST
│                            │  gap 12.dp
│   ┌──────────────────────┐ │
│   │ 二 · Medium · #1420  │ │  card, 88.dp tall
│   │ Unstarted  ·  Play → │ │  radius md, 8% ink border
│   └──────────────────────┘ │
│                            │  gap 32.dp
│   Previous months          │  section label
│                            │  gap 12.dp
│   March · 28 of 31         │  collapsible row 56.dp
│   February · 26 of 28      │
│   ...                      │
│                            │
└────────────────────────────┘
```

## Sections

### Header (48.dp)

- Back arrow (24.dp ink) 48.dp tap target, left
- Title `Daily` — Cormorant Italic 22sp, ink
- No fill

### Month hero

- Month name (`April`) — Cormorant Italic 36sp, ink
- Subtitle (`14 of 22 days solved`) — Inter Medium 13sp, inkSoft

### Calendar grid

- 7 columns, cells 40×40.dp, 4.dp gap
- Day letters (`M T W T F S S`) — Inter Medium 11sp WIDER, inkSoft, top row only
- Number cells:
  - Solved: `Sumi.Color.seal` filled 8.dp dot centered below the number; number in Cormorant Italic 15sp ink
  - Today: Cormorant Italic 17sp weight 600 ink + thin 1.5.dp ring around the cell, seal color
  - Future: Inter Regular 12sp, inkSoft at 40%
  - Unsolved past: same as future but at 70% alpha
- Selection: tap a past day → sheet opens with that day's puzzle summary; tap today → opens the game

### Today card

- Full-width, 88.dp tall, radius 8.dp
- Border 1.dp `Sumi.Color.ink` at 8% alpha, no fill
- Content:
  - Top line: `二 · Medium · #1420` — kanji 16sp Shippori + Inter 13sp ink
  - Bottom line: `Unstarted` OR `In progress · 4:32` — Inter 12sp inkSoft, right: `Play →` or `Continue →` Inter Medium 13sp ink

### Previous months

- Each row: 56.dp, shows month + count
- Font: Cormorant Italic 18sp ink + Inter Medium 13sp inkSoft
- Chevron right icon 20.dp
- Tap expands that month's mini calendar inline (260ms `Sumi.Ease.paper`)

## Resources used

| Resource | Where |
|---|---|
| `washi_paper_light.png` | BG-1 |
| `ink_bleed_02.png` | top-right accent |
| Cormorant Italic | month names, numbers |
| Inter Regular/Medium | labels, meta |
| Shippori Mincho | difficulty kanji |

## Acceptance checklist

- [ ] Vertical scroll works; previous months reachable
- [ ] Solved days show a seal-red dot, not a checkmark
- [ ] Today's cell has a seal ring around it
- [ ] Month name is italic (Cormorant), not sans
- [ ] Calendar grid cells are all the same size (40×40.dp)
- [ ] Today card is 88.dp tall, 8% ink border, no fill
- [ ] "Previous months" rows are tappable with 48.dp minimum height
- [ ] Ink bleed is subtle (0.06 alpha) — not drawing attention
