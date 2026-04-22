# START HERE — First prompt for Claude Code

Open your blank Compose Multiplatform project in a terminal, then run `claude` and paste the prompt below. Claude Code will read the handoff, port the tokens, and scaffold the foundations.

---

## Copy-paste this prompt

````
I'm building Sumi 墨, a zen Sudoku app, on Compose Multiplatform (Android + iOS + Desktop).

The complete design system is in the `handoff/` folder at the project root. Before you write any code, please:

1. Read `handoff/README.md` end-to-end.
2. Read `handoff/DESIGN_PRINCIPLES.md` — these are non-negotiable.
3. Read `handoff/kotlin/SumiTokens.kt` — this is the single source of truth for colors, type, spacing, motion.
4. Skim `handoff/COMPONENTS.md` and `handoff/SCREENS.md` so you understand the surface area.
5. Open `handoff/reference/Sumi Design System.html` in a browser if you need to see the live spec.

Then execute Stage 1 from the README:
- Copy `handoff/kotlin/SumiTokens.kt` and `handoff/kotlin/SumiTheme.kt` into `composeApp/src/commonMain/kotlin/com/sumi/theme/`
- Set up font resources: download Cormorant Garamond (display + italic), Source Serif 4 (body), Inter (UI), Caveat (hand), Shippori Mincho (CJK) from Google Fonts and wire them up via `compose-resources`. See `handoff/FONTS.md`.
- Build the `WashiBG` composable — paper-texture background using Compose `Canvas` + noise. Spec in `handoff/COMPONENTS.md` under "WashiBG".
- Build `SumiButton` (4 variants: primary, ghost, subtle, red) and `SumiChip` (5 tones).
- Wire a tiny demo screen showing the button variants, chip tones, and a text sample for each type role (display italic, body, UI caps).

Do NOT build screens or the board yet. I want to validate that tokens render correctly on all three platforms before we go further. Stop after Stage 1 and show me a screenshot per platform.

Rules:
- Zero gradients except where called out (aurora sweep, seasonal border).
- Zero rounded corners except buttons (2.dp) and sheets (12.dp).
- Zero emoji.
- Use `Modifier.clickable(indication = null)` and draw custom press states.
- Material3 is fine as a base for `Surface` / `Text`, but override the ColorScheme and Typography completely from `SumiTokens`. Don't leak Material's defaults anywhere user-visible.

Go.
````

---

## After Stage 1 lands

Once the foundations are solid on all three platforms, queue the next prompt:

````
Stage 2: build the Sudoku board. Full spec is in `handoff/BOARD.md`.

Requirements:
- `SumiBoard` composable that renders a 9×9 grid with the exact cell-state rules in BOARD.md (selection, same-digit highlight, same-unit highlight, conflict, notes).
- Two number fonts: `SUMI.type.numeral` (Cormorant Garamond, for given clues) and `SUMI.type.hand` (Caveat, for user entries).
- Pure-Kotlin `BoardState` in commonMain — generates puzzles by difficulty, validates moves, tracks conflicts, tracks notes per cell.
- A minimal `BoardPreviewScreen` that wires it up with tap-to-select and a number pad.

Use the sample puzzle from `handoff/reference/sumi/aurora.jsx` (search for `SUMI_SAMPLE_PUZZLE`) as a fixed test case until the generator works.

Stop after the board solves cleanly from the sample state. Show me a screenshot.
````

---

## Subsequent stages

Point Claude Code at `handoff/SCREENS.md` one screen at a time. Don't say "build all screens" — you'll get shallow work. Say "build `HomeScreen` per `handoff/SCREENS.md` §Home, then stop."
