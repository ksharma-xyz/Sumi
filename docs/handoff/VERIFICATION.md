# Verification Framework

## Purpose

When an LLM (Claude Code, Cursor, Copilot) implements Sumi from this handoff, how do you — the human
reviewer — know whether what was built actually matches what was designed?

Manually eyeballing every screen is tedious and unreliable. This doc gives you a **structured
framework** the LLM can run against a codebase to produce a grading report: *"here's what was
correct, here's what wasn't, here's why, and here's what to fix."*

---

## How to use it

### One-time setup

Tell the LLM:
> Read `handoff/VERIFICATION.md` end-to-end. You will use it as a grading rubric against the current
> codebase. For every section, you will produce a PASS, PARTIAL, or FAIL verdict with evidence (file
> path + line number or screenshot observation) and a short reason.

### Running a review

Prompt the LLM:
> Run the verification framework in `handoff/VERIFICATION.md` against the current codebase. For each
> section 1–12, output:
> - **Verdict:** PASS / PARTIAL / FAIL
> - **Evidence:** file path and line number, or visual observation
> - **Reason:** why it passes / fails the criterion
> - **Fix:** concrete steps to correct
>
> Output as a Markdown table per section. Be strict. PARTIAL means "implemented but not to spec";
> FAIL means "missing or wrong".

The LLM then walks every section below and produces a report. You review the report; the LLM
implements the fixes.

### Re-running

Each time you iterate, run the framework again. Track % PASS over time — it should climb toward
100%.

---

## Rules of engagement for the LLM grader

1. **Be strict.** "Close enough" is FAIL unless spec explicitly allows variance.
2. **Cite evidence.** Every verdict must point to a file/line or a screenshot observation. No
   hand-waving.
3. **Never mark PASS by assumption.** If you didn't actually open the file and check the value, mark
   PARTIAL with reason "not verified".
4. **Surface surprises.** If the code does something clever that the spec doesn't cover, note it
   under "unspecified deviations" — don't fail it automatically, but flag it for human review.
5. **Don't auto-fix.** Produce the report first. Fixes come after human sign-off.

---

## Sections

Each section below is a grading rubric with explicit criteria.

---

### § 1 · Design tokens

**Reference:** `handoff/kotlin/SumiTokens.kt`, `handoff/ADAPTIVE.md §3`

| #   | Criterion                                                | How to verify                                                    | Pass threshold                                               |
|-----|----------------------------------------------------------|------------------------------------------------------------------|--------------------------------------------------------------|
| 1.1 | Color palette matches tokens exactly                     | Grep all hex values in code; cross-check against `SumiTokens.kt` | 100% match — zero ad-hoc hexes outside tokens                |
| 1.2 | Night palette present and wired                          | Search for `Sumi.Color.Night.*` usage                            | Every screen that supports dark mode references Night tokens |
| 1.3 | Type scale matches ADAPTIVE.md §3                        | Open type styles; verify family + weight + size per role         | All 11 roles present with correct values                     |
| 1.4 | No Material default `Typography` used                    | Grep for `MaterialTheme.typography` without override             | 0 occurrences                                                |
| 1.5 | Spacing tokens (xs/sm/md/lg/xl) used consistently        | Grep numeric `.dp` values in layout modifiers                    | ≥90% use tokens; stragglers flagged                          |
| 1.6 | Radius tokens used; no rounded pills except where speced | Grep `RoundedCornerShape` usages                                 | All ≤12.dp except documented exceptions                      |

---

### § 2 · Color system + contrast

**Reference:** `handoff/ACCESSIBILITY.md §1`

| #   | Criterion                                    | How to verify                                         | Pass threshold                        |
|-----|----------------------------------------------|-------------------------------------------------------|---------------------------------------|
| 2.1 | All text pairs meet WCAG AA                  | Enumerate text/background pairs; compute contrast     | 100% ≥ 4.5:1 (body) or 3:1 (display)  |
| 2.2 | Red used ONLY for chop / today / completion  | Grep `Sumi.Color.red`; inspect each call site         | No red on generic CTAs, borders, tags |
| 2.3 | Gold used ONLY on Pro surfaces               | Grep `Sumi.Color.gold`; all uses on Pro screens       | Zero gold on free-tier screens        |
| 2.4 | Teal used ONLY for user-entered board digits | Grep `Sumi.Color.teal`                                | Single code path: board cell renderer |
| 2.5 | No pure white / pure black                   | Grep `0xFFFFFFFF` and `0xFF000000`                    | Zero occurrences                      |
| 2.6 | Banned combinations absent                   | Search for `inkFaint` used as text color on body text | Zero occurrences                      |

---

### § 3 · Typography

**Reference:** `handoff/FONTS.md`, `handoff/DESIGN_PRINCIPLES.md §3`, `handoff/ADAPTIVE.md §3`

| #   | Criterion                                   | How to verify                                    | Pass threshold                                                        |
|-----|---------------------------------------------|--------------------------------------------------|-----------------------------------------------------------------------|
| 3.1 | All 5 families present in resources         | List `composeResources/font/`                    | Cormorant, Source Serif 4, Inter, Caveat, Shippori Mincho all present |
| 3.2 | Required weights loaded                     | Grep `Font(` calls; enumerate weights per family | Matches FONTS.md table                                                |
| 3.3 | Italic display uses weight 600 (not 500)    | Grep Cormorant usage                             | All italic display at weight 600                                      |
| 3.4 | Body minimum 16sp                           | Grep body text sizes                             | No body text below 16sp                                               |
| 3.5 | UI labels minimum 12sp                      | Grep label text sizes                            | No label below 12sp                                                   |
| 3.6 | Button weight 700 (not 600)                 | Grep button text styles                          | Weight 700 everywhere                                                 |
| 3.7 | Caveat used ONLY for board entries          | Grep Caveat family                               | Single code path: board cell renderer for entered digits              |
| 3.8 | Shippori used ONLY for kanji chops          | Grep Shippori                                    | Only 墨 / 休 / 完 characters                                             |
| 3.9 | Inter used ONLY for buttons + labels + meta | Grep Inter                                       | No Inter in body or display                                           |

---

### § 4 · Adaptive layout

**Reference:** `handoff/ADAPTIVE.md`

| #    | Criterion                                                   | How to verify                                    | Pass threshold                                           |
|------|-------------------------------------------------------------|--------------------------------------------------|----------------------------------------------------------|
| 4.1  | `WindowSize` enum + `LocalWindowSize` present               | Find declaration                                 | File exists; provided at app root                        |
| 4.2  | Every screen branches on window size                        | Grep `LocalWindowSize.current`                   | All 9 screens have Compact/Medium/Expanded paths         |
| 4.3  | Board cell size is computed, not hardcoded                  | Inspect `SumiBoard` caller                       | Uses `boardCellSize()` or equivalent; no literal `38.dp` |
| 4.4  | Layout holds at 2.0× font scale                             | Manual run with Dynamic Type XXXL / Android 200% | No clipping, no missing text, all buttons tappable       |
| 4.5  | Chips use `FlowRow` at large font scale                     | Inspect chip rows                                | `FlowRow` when `fontScale > 1.3`                         |
| 4.6  | Number pad grows to 3×3 on larger screens                   | Inspect number pad layout                        | 3×3 grid on Medium/Expanded                              |
| 4.7  | On Expanded, two-column+ layouts on Home/Game/Stats/Paywall | Inspect layouts                                  | All four screens have multi-column Expanded layouts      |
| 4.8  | Board capped at 576.dp on tablets                           | Inspect board max size                           | Never exceeds 9 × 64.dp                                  |
| 4.9  | Icon sizes scale with window size                           | Inspect icon size tokens                         | Matches ADAPTIVE.md §5 table                             |
| 4.10 | Minimum touch target 48.dp at 2.0× scale                    | Audit interactive elements                       | All ≥ 48.dp                                              |

---

### § 5 · Accessibility

**Reference:** `handoff/ACCESSIBILITY.md`

| #    | Criterion                                                | How to verify                             | Pass threshold                                                   |
|------|----------------------------------------------------------|-------------------------------------------|------------------------------------------------------------------|
| 5.1  | Every `IconButton` has non-empty `contentDescription`    | Grep `IconButton(`                        | 100%                                                             |
| 5.2  | Board cells have row/col/state semantics                 | Inspect cell renderer                     | `stateDescription` + `contentDescription` present                |
| 5.3  | Decorative elements marked invisible to screen readers   | Grep `WashiBG`, `InkBleed`, `BrushStroke` | All have `invisibleToUser()` or equivalent                       |
| 5.4  | Kanji have English `contentDescription`                  | Grep 墨, 休, 完                              | Each usage has description                                       |
| 5.5  | Focus indicator visible on keyboard nav                  | Manual tab-through                        | 2.dp outline in red visible at each stop                         |
| 5.6  | Reduced motion respected                                 | Check motion code paths                   | PetalFall, PaperBreath, AuroraSweep all check the system setting |
| 5.7  | Color not the only signal                                | Audit per-screen states                   | Every colored state also has shape/font/label difference         |
| 5.8  | Font scale 0.85–2.0 supported                            | Manual run                                | No clipping                                                      |
| 5.9  | Sound/haptics respect system settings                    | Inspect audio + haptics code              | Check system toggles before firing                               |
| 5.10 | TalkBack/VoiceOver dry run reads top-to-bottom logically | Manual test per screen                    | Coherent narration, no gibberish                                 |

---

### § 6 · Components

**Reference:** `handoff/COMPONENTS.md`

| #   | Criterion                          | How to verify                                            | Pass threshold                                                                                                                 |
|-----|------------------------------------|----------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| 6.1 | Signatures match spec              | Compare each composable signature                        | All 10 components match (WashiBG, SumiButton, SumiChip, InkBleed, BrushStroke, Seal, QuoteRule, SumiEyebrow, SumiBoard, logos) |
| 6.2 | No Material ripples                | Grep `rememberRipple()` or `indication = rememberRipple` | Zero occurrences                                                                                                               |
| 6.3 | Press states are ink-darken        | Inspect click feedback                                   | Custom 60/140ms darken, not ripple                                                                                             |
| 6.4 | No elevation shadows except `seal` | Grep `elevation =` or `Modifier.shadow`                  | Only on Seal composable                                                                                                        |
| 6.5 | Corners ≤12.dp everywhere          | Grep `RoundedCornerShape`                                | ≤12.dp                                                                                                                         |

---

### § 7 · Board

**Reference:** `handoff/BOARD.md`

| #   | Criterion                                                      | How to verify                | Pass threshold                        |
|-----|----------------------------------------------------------------|------------------------------|---------------------------------------|
| 7.1 | State model pure Kotlin (commonMain)                           | Inspect `BoardState`         | No Compose imports                    |
| 7.2 | Given clues vs user entries use different fonts                | Inspect cell renderer        | Cormorant vs Caveat                   |
| 7.3 | Given clues vs user entries use different colors               | Inspect cell renderer        | ink vs teal                           |
| 7.4 | In-unit, same-digit, selected, conflict states all implemented | Inspect cell highlight logic | All 5 states present                  |
| 7.5 | Aurora sweep on house complete                                 | Inspect completion handler   | Fires on row/col/box completion       |
| 7.6 | Aurora sweep on full win                                       | Inspect win handler          | Fires 3s before Win screen transition |
| 7.7 | Mistakes do not auto-erase                                     | Inspect mistake handler      | User keeps the wrong digit; can undo  |
| 7.8 | Long-press digit toggles notes                                 | Inspect gesture detection    | Long-press wired                      |

---

### § 8 · Screens

**Reference:** `handoff/SCREENS.md`

For each of the 9 screens (Splash, Onboarding, Home, Game, Win, Pause, Daily, Stats, Paywall):

| #     | Criterion                                           | How to verify                 | Pass threshold                                         |
|-------|-----------------------------------------------------|-------------------------------|--------------------------------------------------------|
| 8.n.1 | Layout matches SCREENS.md structure                 | Screenshot + compare to spec  | Sections present in order                              |
| 8.n.2 | Copy matches spec (no "Great job!" etc.)            | Grep user-facing strings      | No banned phrases                                      |
| 8.n.3 | No ads on Game, Daily, Pause, Win                   | Inspect render tree           | Zero ad components                                     |
| 8.n.4 | Pro-gated features show locked state for free users | Inspect conditional rendering | Lock overlay present on Stats, Hard+ difficulty, Salon |

---

### § 9 · Motion

**Reference:** `handoff/ANIMATIONS.md`

| #   | Criterion                                                    | How to verify               | Pass threshold                  |
|-----|--------------------------------------------------------------|-----------------------------|---------------------------------|
| 9.1 | Durations match spec table                                   | Inspect animation specs     | Within ±50ms of spec            |
| 9.2 | Eases match spec (paper/bleed/brush/snap)                    | Inspect easing curves       | All 4 curves used appropriately |
| 9.3 | No bouncy springs except Seal stamp                          | Grep `spring(`              | Only on Seal                    |
| 9.4 | Aurora hues match `AURORA_HUES_PAPER` and `AURORA_HUES_DARK` | Inspect hue arrays          | Exact match                     |
| 9.5 | PetalFall ≤12 petals on screen                               | Inspect petal state         | Cap enforced                    |
| 9.6 | PaperBreath off during active solve                          | Inspect animation lifecycle | Paused while game is running    |

---

### § 10 · Backgrounds

**Reference:** `handoff/BACKGROUNDS.md`

| #    | Criterion                                       | How to verify                      | Pass threshold                  |
|------|-------------------------------------------------|------------------------------------|---------------------------------|
| 10.1 | Washi PNGs present (light + dark)               | Check `composeResources/drawable/` | Both present at ≥1024²          |
| 10.2 | WashiBG layers in correct order                 | Inspect render order               | Base → noise → vignette         |
| 10.3 | Vignette warm-brown (light) / warm-black (dark) | Inspect vignette color             | Matches BACKGROUNDS.md §Layer 2 |
| 10.4 | Ink bleeds on Win/Paywall only                  | Grep `InkBleed` usage              | Matches per-screen recipes      |
| 10.5 | Dark mode uses `washi_paper_dark.png`           | Inspect conditional resource load  | Correct asset per mode          |

---

### § 11 · Sound & Haptics

**Reference:** `handoff/SOUND_HAPTICS.md`

| #    | Criterion                  | How to verify                          | Pass threshold                                           |
|------|----------------------------|----------------------------------------|----------------------------------------------------------|
| 11.1 | All 12 sound files present | Check `composeResources/files/sounds/` | All present                                              |
| 11.2 | Sounds normalized to -6dB  | Inspect audio metadata                 | Within ±1dB                                              |
| 11.3 | Haptic enum has 6 kinds    | Inspect `SumiHaptic`                   | All 6: Soft, Light, Rigid, Heavy, Success, SuccessDouble |
| 11.4 | Sound off by default       | Inspect default settings               | Off on first launch                                      |

---

### § 12 · Product tier gating

**Reference:** `handoff/PRODUCT.md`

| #    | Criterion                    | How to verify                           | Pass threshold                        |
|------|------------------------------|-----------------------------------------|---------------------------------------|
| 12.1 | Free: Easy + Medium only     | Inspect difficulty picker               | Hard/Master/Edo locked with Pro chip  |
| 12.2 | Free: 3 hints per puzzle     | Inspect hint counter                    | Starts at 3 on free                   |
| 12.3 | Free: no gold anywhere       | Grep gold usage in free-tier code paths | Zero                                  |
| 12.4 | Pro: Salon screen present    | Check route list                        | Screen exists and renders             |
| 12.5 | No ads during solve          | Inspect Game screen                     | Zero ad components on Game route      |
| 12.6 | Max 1 ad per 3 completions   | Inspect ad frequency                    | Rate limit enforced                   |
| 12.7 | Notifications off by default | Inspect default settings                | No permission request on first launch |

---

## Output format

The LLM grader should produce a report like:

```markdown
# Sumi Verification Report — <date>

## Summary

- Total criteria: 98
- PASS: 72 (73%)
- PARTIAL: 14 (14%)
- FAIL: 12 (12%)

## § 1 · Design tokens

| # | Criterion | Verdict | Evidence | Reason | Fix |
|---|---|---|---|---|---|
| 1.1 | Color palette matches | PARTIAL | `HomeScreen.kt:42` uses `Color(0xFFA02A20)` | Close to red but not exact | Replace with `Sumi.Color.red` |
| 1.2 | Night palette wired | FAIL | No Night refs found | Dark mode not implemented | Follow SumiTheme.kt from handoff |

...
```

Save each report as `verification/<yyyy-mm-dd>-report.md` so you can diff progress over time.

---

## Maintenance

When you change a spec (e.g. bump body size from 16 to 17), update the criterion in this file in the
same commit. The framework is only useful if it stays in sync.
