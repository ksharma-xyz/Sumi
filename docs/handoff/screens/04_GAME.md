# 04 · Game (solve)

**The board, tools, and number pad during an active solve. Zero chrome noise. No background animations. No ink bleeds.**

## Background

`BG-3` (paper quiet) — BG-1 with `WashiVariant.Quiet` (fibre alpha 0.7).

**Absolutely no ink bleeds.** **No PaperBreath animation during solve.** Fibre alone. The board is the hero.

## Scroll behavior

**NOT scrollable.** The entire screen must fit any viewport ≥ 340 × 600. Use `Box(Modifier.fillMaxSize())` with a `Column` and flex spacing, not `verticalScroll`.

If the viewport is too small, the board shrinks proportionally (see §Responsive) — never let the screen overflow into scroll.

## Layout (390 × 844 reference)

```
┌────────────────────────────┐ <- safe top
│ ← 二·Medium·#1420   ⏸ 4:32 │  top bar, 48.dp
│                            │  gap 8.dp
│ Marks 0/3      Hints 2/3   │  meta row, 24.dp
│                            │  gap 16.dp
│ ┌────────────────────────┐ │
│ │                        │ │
│ │                        │ │
│ │      9×9 board         │ │  board: fills width minus 24.dp
│ │                        │ │  aspect 1:1
│ │                        │ │
│ │                        │ │
│ └────────────────────────┘ │
│                            │  gap 20.dp
│  ✎  ←  ⚑  ☆               │  tool row, 56.dp
│                            │  gap 20.dp
│                            │
│   1  2  3                  │  number pad
│   4  5  6                  │  3×3 grid, cells 56.dp
│   7  8  9                  │  gap 12.dp
│                            │  (see ADAPTIVE.md §Game)
└────────────────────────────┘ <- safe bottom 16.dp
```

## Sections

### Top bar (48.dp tall)

- Left: back arrow (24.dp ink icon), 48.dp tap target, title `二 · Medium · #1420` on its right
  - Kanji (二) 16sp Shippori, rest Inter Medium 13sp, `Sumi.Color.ink`
- Right: pause icon (24.dp) + timer `4:32` in Cormorant italic 18sp, ink
  - Tap icon or timer → Pause overlay (screen 06)

### Meta row

- `Marks 0 / 3   Hints 2 / 3`
- Inter Medium 12sp, WIDER track, `Sumi.Color.inkSoft`
- Split left/right via `Row { Spacer(Modifier.weight(1f)) }`

### Board (9×9)

See `BOARD.md` for the full spec. Quick summary:
- Fills width minus 24.dp horizontal padding
- 1:1 aspect; cell size = `(width - 24.dp) / 9`
- 3×3 house dividers 2.dp ink; minor cell dividers 0.5.dp at 30% alpha ink
- Fixed digits: Cormorant 600 at ~60% of cell size, `Sumi.Color.ink`
- User digits: Caveat 500, ~72% of cell size, `Sumi.Color.ink`
- Pencil marks: Inter 10sp grid inside cell, `Sumi.Color.inkSoft`
- Selection: cell, row, column, and house tint with `Sumi.Color.ink` at 6% alpha
- Conflict: 2px `Sumi.Color.seal` outline inside the cell

### Tool row

Four equal cells, 56.dp tall, 12.dp gap between:

| Icon | Label hint | Action |
|---|---|---|
| ✎ pencil | `Pencil` | toggle pencil mode |
| ← undo | `Undo` | pop history stack |
| ⚑ flag | `Mark` | leave a bookmark on the selected cell |
| ☆ hint | `Hint` | consume a hint (disabled at 0) |

- Each cell: `Sumi.Color.ink` icon at 24.dp
- Active/toggled: background tint `Sumi.Color.ink` at 8% alpha, radius `Sumi.Radius.sm`
- Disabled: icon at 30% alpha

### Number pad

- 3×3 grid, cells 56 × 56.dp, 12.dp gap
- Each cell: digit in Cormorant 600 at 28sp, `Sumi.Color.ink`
- Press: cell background tints `Sumi.Color.ink` at 10% alpha for 120ms
- Long-press: enters that digit as a pencil mark (if pencil mode off) / clears that pencil mark (if pencil mode on)
- Completed digits (all 9 placed): digit dims to 30% alpha but remains tappable (for pencil work)

## Responsive (critical — see ADAPTIVE.md)

For viewports < 360.dp wide:
- Board uses full width minus 16.dp padding instead of 24.dp
- Tool row cells shrink to 48.dp
- Number pad cells shrink to 48.dp, gap 8.dp

For viewports ≥ 600.dp wide (small tablets):
- Board max 480.dp, centered horizontally
- Number pad to the right of the board in landscape, below in portrait

## Animations

- Placing a digit: short 200ms opacity ramp from 0 → 1 + 1dp settle, `Sumi.Ease.bleed`
- Conflict: seal outline fades in 240ms, does not shake
- Hint consumed: hint counter `3` → `2` cross-fades 300ms
- Row/col/house complete: momentary 260ms halo pulse at 12% ink alpha across the line — no particles, no ink splash (see ANIMATIONS.md §WinLine)
- **No PaperBreath** here. The paper must be still.

## Resources used

| Resource | Where |
|---|---|
| `washi_paper_light.png` | BG-3 base (drawn at quiet alpha) |
| Cormorant 600 | fixed digits, timer |
| Caveat 500 | user digits |
| Inter Medium | chrome labels |
| Shippori Mincho | difficulty kanji in header |
| `ic_back.xml`, `ic_pause.xml`, `ic_pencil.xml`, `ic_undo.xml`, `ic_flag.xml`, `ic_hint.xml` | icons |

## State variations

- **Pencil mode active:** tool icon shows tinted background; entered digits go to the pencil grid, not the main cell
- **All hints used:** hint cell disabled (30% alpha, no press)
- **Paused:** overlay BG-5 mounts on top; timer stops; see screen 06
- **Dark mode:** BG-3 dark, all inks flip per token

## Acceptance checklist

- [ ] Board is perfectly square and fills available width minus padding
- [ ] Paper fibre is visible but at reduced intensity (Quiet variant)
- [ ] NO ink bleed on the background. NO animated paper breath.
- [ ] Fixed vs user digits are clearly different fonts (Cormorant vs Caveat)
- [ ] Pencil marks fit 3×3 inside each cell without overflow
- [ ] Top bar timer ticks in real time; pause icon stops it
- [ ] Number-pad digits are centered in their cells, not baseline-offset
- [ ] Screen never scrolls; content fits viewport
- [ ] On a 340-wide device, nothing clips — responsive shrink kicked in
- [ ] Selection tint is visible but not overwhelming (6% alpha)
