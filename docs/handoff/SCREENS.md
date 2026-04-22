# Screens

All 9 screens for v1. Spec per screen: purpose, layout, components used, data, copy.
Reference implementations: `handoff/reference/sumi/screens.jsx`.

Phone canvas: 340×640 dp (base); iOS/Android adaptive; safe-area insets respected.

---

## 01 · Splash

**Purpose:** One breath. Brand reveal, then home.
**Duration:** 1.4s total, auto-advances.
**Layout:**
- `WashiBG` base
- `LogoEnso` center, 120.dp, animates its arc drawing in via `Animatable` + `PathMeasure` (0 → 1 over 900ms, `Ease.brush`).
- `LogoWordmark` fades in at 600ms, `Ease.paper`.
- `SumiEyebrow` "A daily sudoku" fades in at 900ms, 10sp, `inkFaint`.

## 02 · Onboarding

**Purpose:** Four slides, introduce voice.
**Slides:**
1. Enso mark + "Sumi 墨" + "The daily practice."
2. Miniature 3×3 grid + "Nine lines. Nine columns. One patience."
3. Difficulty picker preview + "Begin gently. Go as far as you like."
4. Quiet scene illustration + "No ads during play. No noise. No streaks to keep."
**Navigation:** Dot indicators at bottom (tiny ink dots, filled for current), "Skip" top-right, "Continue →" primary button bottom.

## 03 · Home

**Purpose:** The daily ritual hub.
**Structure (top → bottom):**
1. Status bar strip (see Phone shell).
2. Brand chop top-left (`LogoChop` 28.dp) + streak indicator top-right (`IconFlame` + "12 days" in UI meta).
3. **Daily card** — the hero:
   - `SumiEyebrow` "Today · April 22" in red
   - h2 italic display "The Threshold" (puzzle title)
   - body small `inkSoft` — one-line description
   - Chip row: `Hard` (red tone), `Hand-crafted` (ink), `2× streak` (gold if Pro)
   - `SumiButton` primary full-width "Begin Today's Puzzle →"
4. **Continue section** (if in-progress): eyebrow "Continue", card with mini-board preview, time elapsed, progress bar.
5. **Quick start** — 3 buttons ghost variant: "Easy", "Medium", "Hard" (Hard locked with 🔒 chip if free).
6. **Today's quote** — `QuoteRule` divider above, full italic quote + attribution in smaller caps.
7. Bottom nav strip: Home · Daily · Stats · Pro (ink icons, current in red).

## 04 · Game (the solve)

**Purpose:** The practice itself.
**No ads. No banners. No toast popups.**
**Layout:**
1. Top bar (minimal):
   - `IconBack` left, puzzle title + difficulty chip center, `IconPause` right.
   - Timer below, 15sp display italic, monospaced numerics.
2. `SumiBoard` centered, cellSize 38.dp. 1.dp ink border around outer grid.
3. **Digit ledger** — horizontal 9-cell strip below board showing each digit 1–9 with remaining count below in UI meta. Tapping a digit highlights all its placements.
4. **Tools row** — 4 icon buttons: Undo, Erase, Notes (toggle, lantern-lit when on), Hint (`IconSparkle`, red dot if hints remaining).
5. **Number pad** — 9 buttons 1–9, 48×48 dp, ghost variant, serif numerals. Tap to enter; long-press to note.
6. No ads. No "score".

See `BOARD.md` for board interaction details.

## 05 · Win

**Purpose:** Completion ceremony.
**Sequence:**
1. Aurora sweep runs across the full grid, 1600ms, `Ease.bleed`.
2. Board fades to `paperGlow` at 40% opacity; `Seal` with "完" stamps down-rotating into center (-5°), 400ms spring.
3. Three stats row: Time · Mistakes · Difficulty (UI meta labels, large display numerals).
4. Quote of the day (or solve-specific) in italic display h3.
5. `QuoteRule`.
6. Two buttons: primary "Next Puzzle →", ghost "Return Home".
7. Optional Pro-only: "Share as postcard" (generates a themed image — Enso + stats + date in wordmark style).

## 06 · Pause

**Purpose:** Step away without losing place.
**Layout:** Blurred snapshot of current game behind (gaussian 16.dp), paper overlay at 88% opacity.
- Center: kanji **休** in Shippori Mincho, 120.sp, `red`.
- Below: "Rest" in UI meta, tracking 0.3em, `inkFaint`.
- Time-so-far below that, display italic.
- Two buttons: primary "Resume", ghost "End session".

## 07 · Daily

**Purpose:** The monthly log.
**Layout:**
1. `SumiEyebrow` "April · Practice log"
2. h1 italic: "Fourteen days, quietly."
3. **Heatmap** — 30-day grid (5×6), each cell a 40×40 dp square:
   - Solved: paperWarm with ink dot
   - Solved fast: paperDeep with red dot (today's color flag if applicable)
   - Unsolved: paperEdge outline, empty
   - Today: red outline, whatever state inside
4. Below heatmap: vertical list of recent entries — date | difficulty chip | time | completion mark.

## 08 · Stats

**Purpose:** Pro-only. Practice log + improvement.
**Locked state (Free):**
- Blurred preview of stats behind, centered lock: `IconLock` + "Practice log · Sumi Pro" + ghost "See Pro →" button.

**Unlocked:**
1. Eyebrow "Practice log", h1 "486" display-italic hero, body "puzzles solved, all time".
2. Line chart — 30-day average solve time, hand-drawn feel (paths with slight jitter).
3. PBs by difficulty: 5 rows (Easy, Medium, Hard, Master, Edo), each with digit + time + date.
4. QuoteRule + "The longer the practice, the quieter the mind."

## 09 · Paywall

**Purpose:** Pro upgrade. Night-mode by default (it's aspirational).
**Layout:**
1. Dark `WashiBG` (night).
2. Centered: Gold Enso mark, wordmark with gold 墨 (not red).
3. h1 italic: "Sumi Pro"
4. Subhead: "An uninterrupted practice."
5. Feature list with `IconCheck` in gold:
   - Remove all ads · Forever quiet
   - Unlimited hints · Use what you need
   - The full quote library · 600 passages
   - Hard, Master, Edo difficulties
   - The Salon · weekly global register
   - Practice log · stats + streaks
   - Gold, Indigo, Edo themes
   - iCloud / Drive sync · across devices
   - Export PDF puzzle books
6. Price card: "$3.99/month" or "$29/year (save 38%)" — two ghost-gold buttons.
7. Bottom meta: "Restore purchase · Terms · Privacy".

**Never shown during a solve.**

---

## Navigation flow

```
Splash → Onboarding (first run only) → Home
                                         ├→ Game → Win → Home or next Game
                                         │      └ Pause (modal) ↩ resumes Game
                                         ├→ Daily
                                         ├→ Stats (Pro-gated)
                                         └→ Paywall (from any "upgrade" affordance)
```

Use whatever navigation stack your base arch ships (Decompose, Voyager, or CMP's `Navigation`). The routes are flat; no deep hierarchy.
