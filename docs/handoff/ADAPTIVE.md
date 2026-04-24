# Adaptive Design — Density, Tablets, Large Font

Sumi must feel right on: a small iPhone SE, a large Android phone, a foldable, a 10" tablet, and with system font scaling from 0.85× up to 2.0× (Dynamic Type XXXL / Android 200%).

The previous spec assumed a 375.dp phone. That was a mistake. This doc replaces those assumptions with a responsive system.

**Action for the LLM implementing this:** read §Summary of changes at the bottom before touching code, then re-read whichever section matches the component you're working on.

---

## 1. Breakpoints

Three canonical widths. Every screen tests at all three.

| Breakpoint | Width range | Target |
|---|---|---|
| `Compact` | 0 – 599.dp | Phones portrait, small foldables closed |
| `Medium` | 600 – 839.dp | Phones landscape, foldables open, small tablets |
| `Expanded` | 840.dp + | Tablets, desktop, unfolded large foldables |

In Compose:

```kotlin
enum class WindowSize { Compact, Medium, Expanded }

@Composable
fun windowSize(): WindowSize {
    val w = LocalConfiguration.current.screenWidthDp
    return when {
        w < 600 -> WindowSize.Compact
        w < 840 -> WindowSize.Medium
        else    -> WindowSize.Expanded
    }
}
```

Expose via `CompositionLocalProvider(LocalWindowSize provides windowSize())` at app root.

## 2. Font scale (user accessibility setting)

Three zones — the app must not break at any of them.

| Zone | `fontScale` | Treatment |
|---|---|---|
| Normal | 0.85 – 1.15 | Default layout |
| Large | 1.15 – 1.50 | Reflow: chips stack, digit ledger wraps, buttons grow |
| XL | 1.50 – 2.00 | Single-column everything; hide non-essential chrome; board cell grows proportionally |

Read via `LocalDensity.current.fontScale`.

**Rule:** no text is clipped, no button is under 48.dp tall, at 2.0× font scale. Verify with a manual UI test per screen.

## 3. Type scale — new, legibility-first

The previous scale was too light for real use. Replace with:

| Role | Family | Weight | Size (sp) | Line height | Notes |
|---|---|---|---|---|---|
| Display XL | Cormorant | **600 italic** | 48 | 1.05 | Hero h1. Was 500 → now 600 for legibility. |
| Display L | Cormorant | 600 italic | 36 | 1.08 | Screen titles. |
| Display M | Cormorant | 600 italic | 28 | 1.15 | Card titles. |
| Subhead | Source Serif | **500** | 17 | 1.4 | Card descriptions. Was 15 → now 17. |
| Body | Source Serif | 400 | 16 | 1.55 | Long-form. Was 14 → now 16. |
| Body S | Source Serif | 400 | 14 | 1.5 | Captions. Was 12 → now 14. |
| UI Button | Inter | **700** | 13 | 1 | Uppercase, tracking 0.22em. Was 600 → now 700. |
| UI Label | Inter | 600 | 12 | 1.2 | Was 10 → now 12. |
| UI Meta | Inter | 500 | 12 | 1.3 | Was 11 → now 12. |
| Numeral (board) | Cormorant | 600 | 26 at cell 44 | 1 | Scales with cell size. Was 22. |
| Hand (entries) | Caveat | **600** | 28 at cell 44 | 1 | Scales with cell size. Was 500. |
| CJK chop | Shippori | 600 | role-specific | 1 | — |

**Why heavier weights:** Cormorant italic 500 is beautiful but disappears on small screens and low-brightness displays. 600 italic is still elegant and holds contrast at normal viewing distance. Source Serif 400 reads clearly now that base is 16 (was 14).

**Minimum sizes (never go below):**
- Primary body: 16sp
- Captions & meta: 12sp
- Button text: 13sp
- UI labels: 12sp
- Interactive targets: 48.dp tall (Material / WCAG target)

Update `SumiTokens.kt` type block to match. See §8 for the diff.

## 4. Density scale (tablets)

Compact phones render at 1× density. Tablets scale up **spacing**, not just fonts.

```kotlin
val density = when (windowSize()) {
    WindowSize.Compact  -> 1.0f
    WindowSize.Medium   -> 1.15f
    WindowSize.Expanded -> 1.35f
}
```

Apply `density` as a multiplier to `Space.*` tokens **for layout spacing only** — padding between sections, card margins, gutter widths. Do NOT scale:
- Font sizes (system font scale handles that)
- Line widths / stroke weights
- Icon sizes (see §5)

## 5. Icon & touch-target sizing

| Context | Compact | Medium | Expanded |
|---|---|---|---|
| UI icon default | 24.dp | 26.dp | 28.dp |
| Top-bar icons | 22.dp | 24.dp | 26.dp |
| Tool row icons | 26.dp | 28.dp | 30.dp |
| Minimum touch | 48.dp | 48.dp | 48.dp |

Icon visual size grows subtly on larger screens; touch target stays ≥48.dp.

## 6. Board sizing (the critical one)

The 9×9 board must scale cleanly. **Rule: cell size is computed, never hard-coded.**

```kotlin
@Composable
fun boardCellSize(): Dp {
    val w = LocalConfiguration.current.screenWidthDp.dp
    val h = LocalConfiguration.current.screenHeightDp.dp
    val maxBoard = minOf(
        w - 32.dp,              // leave 16.dp gutter each side
        (h * 0.55f).coerceAtMost(560.dp),  // cap height share
    )
    return (maxBoard / 9f).coerceIn(34.dp, 64.dp)
}
```

| Screen | Cell size (approx) |
|---|---|
| iPhone SE (320dp) | ~36.dp |
| iPhone 15 (390dp) | ~40.dp |
| Phone landscape (800dp) | ~58.dp |
| iPad mini (744dp) | ~56.dp |
| iPad Pro 11" (834dp) | ~64.dp (capped) |
| iPad Pro 13" portrait (1024dp) | ~64.dp (capped) |

Cap at 64.dp — above that the board looks sparse and the eye has to travel too far. Extra tablet space goes to margins, digit ledger, and side panels instead.

**Numeral sizing inside a cell** = `cellSize × 0.55` (given clues) and `cellSize × 0.62` (hand entries — Caveat sits smaller visually so needs more).

## 7. Responsive layout per screen

### Compact (phones)
Single column. As currently specced in `SCREENS.md`.

### Medium (landscape / foldables)
Most screens: two-column.
- **Home:** Today's card (60% width) | Stats strip + Quote of the day (40%)
- **Game:** Board (left) | Digit ledger + tools + number pad (right panel)
- **Daily:** Heatmap (left) | Log list (right)
- **Stats:** Hero + chart (left) | PBs + quote (right)

### Expanded (tablets)
- **Home:** three-row editorial layout:
  1. Header band — chop, streak, quote (full width, spans ~100.dp tall)
  2. Today's card hero (left 2/3) | Continue + Quick start (right 1/3)
  3. Practice log strip (full width)
- **Game:** Board centered, digit ledger + tools as a vertical rail on the right, number pad below ledger. Left gutter holds timer + difficulty chip. Never edge-to-edge — always centered with breathing room.
- **Paywall:** two-column — mark + pitch (left) | feature list + price cards (right)
- **Stats:** Three-column — hero number | chart | PBs list

**Rule of thumb for Expanded:** the board is still the hero. Surrounding chrome expands; the board never becomes wider than 576.dp (9 × 64.dp).

### Foldables
Detect fold posture via `WindowInfoTracker` (Android) / `UIScreen.isGeminiMode` (iOS 17+). On a dual-screen fold:
- **Tabletop posture** (half-open horizontal): puzzle on top half, controls on bottom half
- **Book posture** (half-open vertical): puzzle on one page, daily log on the other

Not v1. Spec only.

## 8. Rows "fill the width" at large font scale

Specifically for Home and Daily lists at `fontScale > 1.3`:
- Chip rows wrap instead of horizontal-scroll (`FlowRow`)
- Digit ledger becomes two rows of 5+4 instead of one row of 9
- Number pad becomes 3×3 grid at full available width (each button is `(width - gutters) / 3`)
- Stats rows stack vertically with full-width horizontal rules between

Implementation tip: use `BoxWithConstraints` + `LocalDensity.fontScale` to branch layout.

## 9. Grid cells on tablets (the "use the space" idea)

On Expanded, certain lists become grids:
- **Daily log entries:** grid of 2 or 3 columns of date-cards (each card = date + difficulty + time + mark)
- **Stats PBs:** 3×2 grid of difficulty cards instead of a vertical list
- **Paywall features:** 2×5 grid instead of one tall list
- **The Salon (Pro leaderboard):** single wide table with rank · name · city · time · difficulty — no stacking

## 10. Summary of changes (apply in order)

1. **Bump type scale** (§3). Primary body 14→16, UI label 10→12, button 13→13 but weight 600→700, display italic 500→600. Update `SumiTokens.kt`.
2. **Add `WindowSize` + `LocalWindowSize`** (§1). One file, wired at app root.
3. **Add `boardCellSize()`** (§6). Replace hardcoded 38.dp in `SumiBoard` calls.
4. **Add density multiplier** (§4) applied to spacing tokens only.
5. **Add per-screen Medium/Expanded layouts** (§7). Each screen gets a `when (windowSize)` branch.
6. **Reflow at `fontScale > 1.3`** (§8). Chips, ledger, number pad, stats rows.
7. **Verify min touch 48.dp** everywhere with a one-time audit.
