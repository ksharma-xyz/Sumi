# 03 · Home

**The daily entry point. Shows today's date, a quote, the primary "Play" action, and quick links to Daily / Stats / Settings. Always scrollable — on short phones the quote and footer push below the fold.**

## Background

`BG-1` (paper, light) — full washi paper.

**No ink bleeds on Home.** Keep it the calmest surface in the app. Every other paper screen has at least one bleed; Home intentionally has none.

## Scroll behavior

**Vertically scrollable.** Required — the footer section (version, credits, log link) must always be reachable.

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(horizontal = 24.dp)
        .padding(top = 12.dp, bottom = 48.dp),
    verticalArrangement = Arrangement.spacedBy(0.dp),
) { ... }
```

## Layout (390 × 844 reference)

```
┌────────────────────────────┐ <- safe top
│ Sumi 墨           ⚙        │  header bar, 48.dp
│                            │  gap 24.dp
│                            │
│ April 22 · Today           │  label 12sp WIDER track, inkSoft
│                            │  gap 8.dp
│ Nothing in excess.         │  quote — Cormorant Italic 28sp
│                            │  attribution — Inter 12sp UPPER
│ — DELPHI                   │  gap 4.dp
│                            │
│                            │  gap 48.dp
│                            │
│   [ Continue practice ]    │  primary button, full-width
│                            │  52.dp tall
│                            │  label: "Continue" or "Begin"
│                            │  gap 16.dp
│ Yesterday · 8:42 · Medium  │  meta line, inkSoft 13sp
│                            │
│                            │  gap 40.dp
│                            │
│  Daily    Stats    Log     │  quick-link row, 3 equal cells
│                            │  each: kanji + label
│                            │
│                            │  gap 48.dp
│                            │
│ v1.0 · Practice Log · ToS  │  footer faint inkSoft 11sp
│                            │
└────────────────────────────┘ <- safe bottom 40.dp
```

## Section-by-section

### Header bar (48.dp tall)

- Left: wordmark "Sumi" (Cormorant Italic 20sp) + kanji 墨 (Shippori 20sp) in the same line, ink color
- Right: settings gear icon (24.dp), `Sumi.Color.inkSoft`, tap → Settings sheet
- No background fill; sits on paper
- 12.dp horizontal padding inside

### Today block

- Label: `April 22 · Today`
  - Font: Inter Medium 12sp, letter-spacing WIDER (+0.2em), uppercase NOT required (title-case)
  - Color: `Sumi.Color.inkSoft`
- Quote body:
  - Font: Cormorant Italic 28sp, line-height 36sp, `Sumi.Color.ink`
  - `text-wrap: pretty` (in Compose: `TextLayoutResult` auto-balance, or manual line break)
  - Quotes rotate daily — ship 90 built-in, see PRODUCT.md §Quote Bank
- Attribution:
  - Font: Inter Medium 11sp, letter-spacing WIDEST (+0.28em), UPPERCASE
  - Prefix: em-dash + space
  - Color: `Sumi.Color.inkSoft`

### Primary CTA

- Button: full-width (`fillMaxWidth()`), height 52.dp
- Label text depends on state:
  - No puzzle in progress → "Begin practice"
  - Puzzle in progress → "Continue practice"
- Variant: `SumiButton.Primary`
  - Background `Sumi.Color.ink`, text `Sumi.Color.paper`
  - Corner radius `Sumi.Radius.md` (8.dp)
  - Label: Inter Bold 13sp, letter-spacing WIDER, case: **Title Case** (not UPPER)
- Below button, meta line:
  - `Yesterday · 8:42 · Medium` OR `Puzzle #1420 · Medium · 4:32 in`
  - Inter Regular 13sp, `Sumi.Color.inkSoft`
  - Hidden entirely if user is brand-new (no prior play)

### Quick-link row

Three equal-width cells, arranged with `Row { Modifier.weight(1f) }`:

```
┌─────────┬─────────┬─────────┐
│    日   │    弓   │    書   │   kanji 28sp, Shippori
│  Daily  │  Stats  │   Log   │   Inter 13sp
│         │         │         │
└─────────┴─────────┴─────────┘
```

- Kanji row: 28sp Shippori Mincho, `Sumi.Color.ink`
- Label row: Inter Medium 13sp, `Sumi.Color.inkSoft`, title case
- Cell height: 96.dp
- Cell background: none (paper shows through). On press: ripple tinted `Sumi.Color.ink` at 6% alpha
- Divider: vertical hairlines between cells, `Sumi.Color.ink` at 10% alpha, 1.dp wide, 60% height centered

Optional: add a subtle rule above and below the quick-link row (full-width 1.dp hairline at 8% alpha ink) to set the block apart.

### Footer

- Line: `v1.0 · Practice Log · Terms` where "Practice Log" and "Terms" are tap targets
- Font: Inter Regular 11sp, `Sumi.Color.inkSoft` at 70%
- Alignment: center
- 24.dp above bottom safe-area

## Resources used

| Resource | Where |
|---|---|
| `washi_paper_light.png` | BG-1 |
| Cormorant Italic | quote body, wordmark |
| Shippori Mincho | kanji (墨, 日, 弓, 書) |
| Inter Regular/Medium/Bold | labels, button, meta |
| Settings icon | `ic_settings.xml` in drawable |

## State variations

- **First-time user** (no session yet):
  - Quote block shows the same quote
  - CTA: "Begin practice"
  - Meta line hidden
  - Quick-link row may show Daily / Tutorial / Log instead of Daily / Stats / Log
- **Puzzle in progress:** "Continue practice"; meta line shows time elapsed
- **Streak active** (consecutive days solved): small dot (4.dp, `Sumi.Color.seal`) after "Today" label
- **Dark mode:** BG-2, all inks invert per tokens

## Animations

- On first enter: quote text fades in 400ms (`Sumi.Ease.paper`), wordmark 200ms earlier
- Buttons use `SumiButton` press ripple only — no scale bounce
- Reduced motion: no fade on text, just appear

## Acceptance checklist

- [ ] Home scrolls vertically if content is taller than viewport
- [ ] Quote uses Cormorant Italic, not upright serif
- [ ] Attribution is UPPER + WIDEST tracked, with em-dash prefix
- [ ] Primary button is 52.dp tall, ink on paper, title-case label
- [ ] Quick-link kanji are Shippori, not an emoji or a generic CJK fallback
- [ ] No ink bleed behind anything on Home — Home is the quiet surface
- [ ] Paper has visible fibre texture (not a flat color)
- [ ] Footer is reachable by scrolling on a 340×640 device
- [ ] Meta line under CTA hides when no prior session exists
