# Screens — start here

Each screen has its own dedicated spec in `screens/` with:
- Exact background recipe (which BG-N from BACKGROUNDS.md)
- Scroll behavior (always scrollable unless noted)
- Section-by-section layout with dp values
- Which resources go where
- State variations
- Acceptance checklist

**Read BACKGROUNDS.md before any screen doc. It is the source of truth for the 5 backgrounds.**

## Index

| # | Screen | File | BG | Scrolls? |
|---|---|---|---|---|
| 01 | Splash | `screens/01_SPLASH.md` | BG-1 | no (one-shot, <2s) |
| 02 | Onboarding (3 slides) | `screens/02_ONBOARDING.md` | BG-1 | per-slide: yes |
| 03 | Home | `screens/03_HOME.md` | BG-1 | **yes** |
| 04 | Game (solve) | `screens/04_GAME.md` | BG-3 | **no** (fits viewport) |
| 05 | Win | `screens/05_WIN.md` | BG-1 + bleeds | **yes** |
| 06 | Pause overlay | `screens/06_PAUSE.md` | BG-5 | no (modal) |
| 07 | Daily | `screens/07_DAILY.md` | BG-1 | **yes** |
| 08 | Stats | `screens/08_STATS.md` | BG-1 / BG-4 if locked | **yes** |
| 09 | Paywall | `screens/09_PAYWALL.md` | BG-4 | **yes** |

## Universal rules for every screen

### Scroll behavior (global rule)

**Every screen that could exceed the viewport must be vertically scrollable.** No exceptions except:
- Game (board must be fully visible)
- Pause (modal, fits by construction)
- Splash (one-shot, <2s)

Implementation:

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp)
        .padding(top = 16.dp, bottom = 40.dp),
) { ... }
```

If you used `LazyColumn`, the same applies — ensure the column is the root and top-level content fits inside. Never nest `verticalScroll` inside another `verticalScroll` — it throws.

### Status bar + safe area

- Top padding: `WindowInsets.statusBars` + 12.dp
- Bottom padding: `WindowInsets.navigationBars` + 24.dp (or 40.dp if the screen has a fixed bottom CTA)
- Never let content sit under the status bar

```kotlin
Modifier
  .windowInsetsPadding(WindowInsets.safeDrawing)
  .padding(horizontal = 24.dp)
```

### Screen padding

Unless a screen doc says otherwise:
- Horizontal: `24.dp`
- Vertical top: `24.dp` (after status bar)
- Vertical bottom: `40.dp`
- Between major blocks: `32.dp`
- Between sibling items in a block: `16.dp`
- Between a label and its value: `4.dp`

### Fonts and sizes

Pull sizes from `SumiTokens.Size.*`. Do not invent sizes. Minimum body 16sp, minimum label 12sp, minimum touch target 48.dp.

### Animations during navigation

Use `SumiTransitions.paperSlide` (see ANIMATIONS.md). Do not default to Material's fade.

## How to verify a screen is correct

For each screen, after you build it, screenshot on a 390×844 device (iPhone 14) AND on a 340×640 compact phone AND on a 600×1024 small tablet. Run the acceptance checklist at the bottom of that screen's doc. If any item fails, fix before moving on.
