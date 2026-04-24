# Sumi 墨 — Compose Multiplatform Handoff

A zen Sudoku app in the spirit of ink, paper, and patience.
This package contains everything you need to build Sumi on a **Compose Multiplatform (Android + iOS + Desktop)** blank project.

---

## What's in this folder

```
handoff/
├── README.md                    ← you are here
├── VERIFICATION.md              ← grading rubric — run this to audit any build
├── ADAPTIVE.md                  ← breakpoints, tablet layouts, font-scale reflow
├── ACCESSIBILITY.md             ← WCAG, semantics, reduced-motion contract
├── CATALOGUE_APP.md             ← living design-system showcase app
├── START_HERE.md                ← first prompt to give Claude Code
├── DESIGN_PRINCIPLES.md         ← the voice, the rhythm, the non-negotiables
├── BRAND.md                     ← name, kanji, wordmark usage
├── COMPONENTS.md                ← Compose component API contracts
├── SCREENS.md                   ← index of 9 screens (see screens/ subfolder)
├── screens/                     ← one prescriptive spec per screen
│   ├── 01_SPLASH.md
│   ├── 02_ONBOARDING.md
│   ├── 03_HOME.md
│   ├── 04_GAME.md
│   ├── 05_WIN.md
│   ├── 06_PAUSE.md
│   ├── 07_DAILY.md
│   ├── 08_STATS.md
│   └── 09_PAYWALL.md
├── BOARD.md                     ← Sudoku board rendering + state
├── ANIMATIONS.md                ← aurora sweep, petal fall, paper breath
├── BACKGROUNDS.md               ← paper noise, vignettes, ink bleeds, foxing (per-screen recipes + image-gen prompts)
├── SOUND_HAPTICS.md             ← sound + haptic spec
├── FONTS.md                     ← typography — what to license / download
├── PRODUCT.md                   ← free vs Pro, ads, The Salon
├── kotlin/
│   ├── SumiTokens.kt            ← colors, type, spacing, motion — single source
│   ├── SumiTheme.kt             ← MaterialTheme wrapper for CMP
│   └── Quotes.kt                ← the quote library
├── svg/
│   ├── logo-enso.svg
│   ├── logo-grid.svg
│   ├── logo-chop.svg
│   ├── logo-nine.svg
│   ├── logo-strokes.svg
│   ├── wordmark.svg
│   ├── app-icon-paper.svg
│   ├── app-icon-ink.svg
│   ├── app-icon-red.svg
│   └── icons/                   ← 24 UI icons, 24×24, stroke 1.8
└── reference/                   ← the original HTML/JSX design system
    ├── Sumi Design System.html
    └── sumi/                    ← all source components
```

---

## Recommended project scaffold

You said you'll start from a **Compose Multiplatform blank project with a base architecture starter**. Good. Target structure once you scaffold:

```
sumi/
├── composeApp/
│   ├── commonMain/kotlin/com/sumi/
│   │   ├── theme/
│   │   │   ├── SumiTokens.kt        ← from handoff/kotlin/
│   │   │   ├── SumiTheme.kt
│   │   │   └── SumiTypography.kt
│   │   ├── design/
│   │   │   ├── components/          ← Button, Chip, Seal, InkBleed, WashiBG…
│   │   │   ├── icons/               ← 24 icons as ImageVector
│   │   │   ├── logos/               ← Enso, Grid, Chop, Wordmark
│   │   │   └── motion/              ← AuroraSweep, PetalFall, PaperBreath
│   │   ├── game/
│   │   │   ├── Board.kt             ← rendering
│   │   │   ├── BoardState.kt        ← solver, conflicts, notes
│   │   │   └── Puzzle.kt            ← data model + generator bridge
│   │   ├── screens/
│   │   │   ├── SplashScreen.kt
│   │   │   ├── OnboardingScreen.kt
│   │   │   ├── HomeScreen.kt
│   │   │   ├── GameScreen.kt
│   │   │   ├── WinScreen.kt
│   │   │   ├── PauseSheet.kt
│   │   │   ├── DailyScreen.kt
│   │   │   ├── StatsScreen.kt
│   │   │   └── PaywallScreen.kt
│   │   └── data/
│   │       ├── Quotes.kt            ← from handoff/kotlin/
│   │       └── PuzzleRepo.kt
│   ├── androidMain/                 ← haptics (Vibrator), sounds (SoundPool)
│   ├── iosMain/                     ← haptics (UIImpactFeedbackGenerator), AVFoundation
│   └── desktopMain/
└── composeResources/
    ├── font/                        ← Cormorant, Source Serif 4, Inter, Caveat, Shippori Mincho
    ├── drawable/                    ← copy from handoff/svg/
    └── raw/                         ← sound files
```

---

## Build order (recommended)

Don't try to build everything at once. Follow this order — each stage should feel complete before moving on.

### Stage 1 — Foundation (1–2 days)
1. Drop `SumiTokens.kt` + `SumiTheme.kt` into `commonMain/theme/`
2. Register fonts in `composeResources/font/`
3. Build `WashiBG` — the paper-texture background. Everything else rides on top of this.
4. Build `SumiButton` and `SumiChip` — validate tokens round-trip correctly.

### Stage 2 — The board (2–3 days)
5. Build `Board.kt` renderer (`SumiBoard` composable, 9×9, draw grid, cells, numerals). See `BOARD.md`.
6. Build `BoardState.kt` — pure Kotlin state (MVVM or Decompose, whatever your base arch uses). Generate/solve/validate Sudoku.
7. Hook selection, notes, and conflicts into the renderer.

### Stage 3 — Screens (3–5 days)
8. Home → Game → Win. The critical path.
9. Pause, Daily, Stats, Paywall, Onboarding, Splash.

### Stage 4 — Motion & polish (2 days)
10. AuroraSweep (row / column / box completion).
11. PetalFall (splash / home idle).
12. PaperBreath (ambient background animation).
13. Haptics + sound hooks.

### Stage 5 — Pro features
14. The Salon (weekly leaderboard). Stub the backend, build the UI.
15. Daily streak + iCloud/Drive sync via your base-arch's sync layer.
16. Ad interstitials (between puzzles only, never during).

---

## Non-negotiables (read before you start)

0. **Use shipped asset files for logos and icons.** Never hand-draw the Ensō with `Canvas` / `drawArc`, never recreate an icon as a Compose `Path`. Import the SVGs from `handoff/svg/` as Android Vector Drawables or `ImageVector`, then `Image(painter = painterResource(...))`. The brush-ink texture only survives through the asset import path.
1. **Paper, not Material.** No ripples, no elevation shadows except the ones in `SumiTokens.elevation`. Use `Modifier.clickable(indication = null)` and draw your own press states.
2. **No gradients** except the aurora sweep and the seasonal border bloom. The rest is flat paper and flat ink.
3. **No rounded corners** except on buttons (`radius.xs = 2.dp`) and sheets (`radius.lg = 12.dp`). Cells, chips, and panels are sharp-edged.
4. **No emoji.** Use the kanji chops (墨 休 完) and the custom icon set.
5. **Typography does the heavy lifting.** Italic serif for emotion, UI sans for function, hand script for the user's own digits on the board.
6. **The board is sacred.** No ads, no interstitials, no modals over the grid during a solve.

See `DESIGN_PRINCIPLES.md` for the full philosophy.

---

## First prompt to Claude Code

See `START_HERE.md` — it's a copy-pastable prompt that bootstraps the whole thing.
