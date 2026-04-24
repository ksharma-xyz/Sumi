# Catalogue App — Living Design System Showcase

A secondary app target (or a hidden route inside Sumi, gated behind a debug flag) that renders **every token, component, screen, and state** in the design system. Think Storybook for Compose.

**Why:** without this, the design system lives in PDFs and reviewers eyeball the main app. With this, the LLM that builds Sumi can also render its output live, inspect every state, and the reviewer can walk the catalogue to spot regressions visually.

---

## Audience

1. **Designers** — verify tokens render as intended
2. **Developers** — copy-paste component usage from the examples
3. **Reviewers** — quickly spot drift without digging through code
4. **LLM agents** — screenshot the catalogue and compare to `handoff/reference/Sumi Design System.html`

---

## Distribution strategy

Pick one — probably all three eventually:

### Option A — Separate app module (recommended)

```
sudoku-compose/
├── composeApp/              ← main Sumi app
├── catalogue/               ← DS showcase, separate build target
└── shared/                  ← tokens, components (used by both)
```

Both targets depend on `shared`. Build the catalogue once per PR; deploy to internal TestFlight + APK for the design team.

### Option B — Hidden route inside main app

Debug builds expose `/catalogue` route; release builds strip it out via `BuildConfig.DEBUG`.

### Option C — Web target

Compose Multiplatform can also build to HTML/JS. Ship the catalogue to GitHub Pages so the whole team can review in a browser without installing anything.

---

## Required catalogue sections (12)

The catalogue is a scrollable screen with a left rail and 12 sections.

### 1 · Foundation
Brand story, principles summary, what Sumi is / isn't. Links out to `DESIGN_PRINCIPLES.md`.

### 2 · Color
- Light palette swatches (paper/paperWarm/paperDeep/paperEdge/paperGlow; ink/inkSoft/inkFaint; red; teal; gold)
- Night palette swatches (same names, Night tones)
- Each swatch shows: name, hex, role, contrast ratio against paper + ink
- **Contrast matrix** — every text/bg pair with pass/fail WCAG AA indicator

### 3 · Typography
All 11 type roles rendered with real text, in both light and dark. Include the pangram *"The quieter you become, the more you can hear"* at each role.

### 4 · Spacing & Layout
Visual ruler showing xs/sm/md/lg/xl spacing. Breakpoint demo with resize preview.

### 5 · Motion
Live playable animation thumbnails: `Ease.paper`, `Ease.bleed`, `Ease.brush`, `Ease.snap`. Tap each to replay.

### 6 · Icons
24-icon grid with name + size variants (24/26/28.dp). Tap any icon to copy its name to clipboard.

### 7 · Logos
5 logo marks + wordmark + 3 app icon variants. Each shown at multiple sizes (20/48/120dp).

### 8 · Components
Interactive gallery:
- **Buttons** — every variant × every size × every state (default, pressed, disabled)
- **Chips** — all 5 tones
- **InkBleed / BrushStroke / Seal / QuoteRule / SumiEyebrow** — hero renders
- **SumiBoard** — live 9×9, all 5 cell states visible

### 9 · Screens
Thumbnail of each of the 9 screens at Compact/Medium/Expanded. Tap thumbnail → fullscreen preview with a floating "state" menu (empty / partial / complete / dark mode / large font).

### 10 · A11y
- Contrast matrix (§2 cross-reference)
- Touch target audit — every interactive's measured hit size
- Semantics dump — tree view of a screen's screen-reader narration
- Font scale slider — live 0.85×–2.0× preview of Home + Game + Settings

### 11 · Tokens inspection
Raw table view of every token from `SumiTokens.kt` — color, type, space, radius, duration, easing. Exportable as JSON.

### 12 · Backgrounds
The 5-layer model (BACKGROUNDS.md) rendered live. Toggle each layer on/off to see the composition build up.

---

## Technical structure

```
catalogue/
  src/commonMain/kotlin/com/sumi/catalogue/
    CatalogueApp.kt              # root composable
    CatalogueNav.kt              # route table
    sections/
      FoundationSection.kt
      ColorSection.kt
      TypographySection.kt
      SpacingSection.kt
      MotionSection.kt
      IconsSection.kt
      LogosSection.kt
      ComponentsSection.kt
      ScreensSection.kt
      A11ySection.kt
      TokensSection.kt
      BackgroundsSection.kt
    shared/
      SectionShell.kt            # card, header, description layout
      Swatch.kt                  # color swatch w/ contrast info
      TypeSpecimen.kt            # type role renderer
      ComponentStage.kt          # component preview w/ state chips
      CodeBlock.kt               # copy-paste usage snippet
```

---

## Catalogue-specific UX rules

1. **Neutral chrome.** The catalogue itself uses a minimal neutral UI (Inter 14 + dark-on-light), NOT Sumi's brand — otherwise the DS will fight for attention with itself.
2. **Always show state chips.** Next to every component, a row of chips lets you toggle: light / dark, default / hover / pressed / disabled, small / medium / large font scale, Compact / Medium / Expanded width.
3. **Copy-paste usage.** Every component preview has a code snippet below it with the exact Compose call that would render it.
4. **Deep linkable.** Each section has a URL path (`/catalogue/color`, `/catalogue/components/button`) so reviewers can link directly to a component.
5. **Search.** Top-bar search spans tokens + components by name.

---

## Build checklist

- [ ] Catalogue module created; shared module dependency wired
- [ ] 12 sections scaffolded (even if empty)
- [ ] Color section renders all light + night swatches with contrast ratios computed at runtime
- [ ] Typography section renders all 11 roles in both modes
- [ ] Spacing + breakpoints section with resizable preview
- [ ] Motion section with playable thumbnails
- [ ] Icons + logos gallery
- [ ] Components section with state chips
- [ ] Screens section with device-frame thumbnails + fullscreen preview
- [ ] A11y section with contrast matrix + semantics inspector
- [ ] Tokens section with JSON export
- [ ] Backgrounds section with layer toggles
- [ ] Deploy catalogue to TestFlight / APK / GitHub Pages
- [ ] Add catalogue URL + build link to project README

---

## Maintenance

The catalogue is a first-class target — not a hack. When you add a token or component, you add its entry to the catalogue in the same PR. Without that rule, the catalogue will drift within weeks.
