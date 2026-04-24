# 09 · Paywall

**Sumi Pro pitch. Dark by design — this is the "night mode" screen of the app, distinct from the paper screens. Scrollable.**

## Background

`BG-4` (ink night) — see BACKGROUNDS.md.

**Gold-tinted ink bleed:** `ink_bleed_01.png`, 400.dp, alpha 0.14, centered behind the Pro wordmark. Tint via `ColorFilter.tint(Sumi.Color.goldIvory, BlendMode.SrcIn)` applied at render.

This is the ONE screen where an ink bleed is tinted — because gold over ink is the Pro signature.

## Scroll behavior

**Scrollable.** Pricing tiers + features + footer easily exceed viewport.

## Layout

```
┌────────────────────────────┐ BG-4 ink night
│  ←                         │  close button top-left
│                            │
│                            │
│       [gold ink bleed]     │
│                            │
│         Sumi Pro           │  Cormorant Italic 46sp
│                            │  gold-ivory color
│                            │  gap 8.dp
│     An uninterrupted       │  Cormorant Italic 22sp
│       practice.            │  off-white
│                            │  gap 40.dp
│                            │
│   ━━━━━━━━━━━━━━           │  hairline divider
│                            │  gap 24.dp
│   No ads, ever             │  feature row 56.dp
│   Unlimited history        │
│   Custom themes            │
│   Export your practice     │
│   ━━━━━━━━━━━━━━           │
│                            │  gap 40.dp
│                            │
│   ┌───────┬───────┐        │  pricing tabs
│   │Monthly│Yearly │        │
│   │ $3.99 │$29.99 │        │
│   │       │ -38%  │        │
│   └───────┴───────┘        │
│                            │  gap 24.dp
│                            │
│  [ Start 7-day free trial ]│  primary gold button
│                            │  gap 12.dp
│    Already Pro · Restore   │  ghost link, center
│                            │
│                            │  gap 32.dp
│   Terms · Privacy          │  footer 11sp, 40% alpha
│                            │
└────────────────────────────┘
```

## Sections

### Close

- Top-left, 48.dp tap, 24.dp X icon in `Sumi.Color.goldIvory` at 70% alpha
- Tap → dismiss paywall (back to source screen)

### Hero

- Wordmark `Sumi Pro` — Cormorant Garamond Italic 46sp, color `Sumi.Color.goldIvory` (#E8D7A6)
- Subhead `An uninterrupted practice.` — Cormorant Italic 22sp, off-white (`Sumi.Color.paper` at 80% alpha)
- Centered, gold ink bleed behind

### Features list

4 rows, each 56.dp tall, left-aligned:
- Icon (20.dp, goldIvory at 60% alpha): `check`, `infinity`, `palette`, `download`
- Label Inter Medium 15sp, goldIvory at 90%
- 12.dp between icon and label

Wrapped above and below with 1.dp hairline dividers in goldIvory at 20% alpha.

### Pricing tabs

- Two cells, equal width, 96.dp tall, 12.dp gap
- Selected tab: 1.5.dp goldIvory border, 6% goldIvory fill
- Unselected: 1.dp goldIvory border at 30% alpha, no fill
- Content per cell:
  - Top: tier name Inter Medium 13sp UPPER WIDER, goldIvory at 70%
  - Middle: price Cormorant Italic 28sp, goldIvory
  - Bottom (yearly only): savings tag `-38%` Inter 11sp in seal red at 80%

### Primary CTA

- Full-width, 56.dp tall, radius md
- Background: `Sumi.Color.goldIvory`
- Label: `Start 7-day free trial` — Inter Bold 14sp, color `Sumi.Color.ink` (dark text on gold)
- On press: goldIvory at 85% alpha for 120ms

### Restore link

- `Already Pro · Restore` — Inter Medium 13sp, goldIvory at 80%, center, tappable

### Footer

- `Terms · Privacy` — Inter 11sp, goldIvory at 40%, center
- 24.dp above bottom safe area

## Resources used

| Resource | Where |
|---|---|
| `washi_ink_dark.png` | BG-4 |
| `ink_bleed_01.png` | gold-tinted behind hero |
| Cormorant Italic | wordmark, subhead, prices |
| Inter Medium/Bold | buttons, labels |
| Icons: check, infinity, palette, download | `drawable/` |

## Acceptance checklist

- [ ] Background is warm ink-dark (#1A1410 base), NOT blue-black
- [ ] The ONE ink bleed is gold-tinted, centered behind the Sumi Pro wordmark
- [ ] Wordmark is Cormorant Italic in goldIvory, not ink
- [ ] Primary button is goldIvory fill with DARK text — high contrast
- [ ] Pricing tabs clearly show selected state (border + fill)
- [ ] Yearly tab shows savings badge (seal red)
- [ ] Restore link is reachable and tappable
- [ ] Feature list uses simple icon + label rows, no cards
- [ ] Screen scrolls; footer reachable on 340×600
- [ ] Close (X) lives top-left, 48.dp tap
