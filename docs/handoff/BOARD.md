# The Board

The 9×9 Sudoku grid. The heart of the app.

## State model (commonMain, pure Kotlin)

```kotlin
data class Cell(
    val value: Int,          // 0 = empty, 1..9 = filled
    val given: Boolean,      // printed clue, can't be erased
    val notes: Set<Int> = emptySet(),  // pencil marks
)

data class BoardState(
    val cells: List<List<Cell>>,       // 9x9
    val selected: Pair<Int, Int>?,     // (row, col) or null
    val conflict: Pair<Int, Int>? = null,
    val notesMode: Boolean = false,
    val hintsRemaining: Int = 3,
    val elapsedMs: Long = 0L,
    val difficulty: Difficulty,
) {
    val counts: IntArray  // remaining count per digit 1..9 (9 - placed)
        get() { ... }
    val isComplete: Boolean
    fun select(r: Int, c: Int): BoardState
    fun enter(digit: Int): BoardState  // respects notesMode + given
    fun toggleNote(digit: Int): BoardState
    fun erase(): BoardState
    fun undo(): BoardState              // keep a move history stack
    fun hint(): BoardState?             // reveals one cell if hints > 0
}

enum class Difficulty { Easy, Medium, Hard, Master, Edo }
```

Keep it a pure state machine in `commonMain`. No Compose imports.

## Puzzle generation

Sumi ships puzzles hand-crafted per day. For scaffolding, use a simple backtracking generator:

```kotlin
object SudokuGenerator {
    fun generate(difficulty: Difficulty, seed: Long = System.currentTimeMillis()): BoardState
}
```

Implementation:
1. Fill a solved 9×9 via backtracking + shuffled candidates.
2. Remove cells per difficulty (Easy ~40 given, Medium ~32, Hard ~28, Master ~24, Edo ~22) while keeping unique solution (verify by counting solutions to ≥2 → abort removal).

For the MVP test case use `SUMI_SAMPLE_PUZZLE` from `reference/sumi/aurora.jsx`.

## Rendering (`SumiBoard`)

Cell visual states (additive, highest priority last):

1. **Base** — transparent / no bg
2. **In-unit** (same row, col, or 3×3 box as selected) — `rgba(ink, 0.03)` light, `rgba(paper, 0.025)` dark
3. **Same digit** (non-zero cell with same value as selected) — `rgba(teal, 0.08)` light, `rgba(teal-light, 0.14)` dark
4. **Selected** — `rgba(red, 0.08)` light, `rgba(paper, 0.08)` dark
5. **Conflict** — `rgba(red, 0.10)` light, `rgba(red, 0.22)` dark

Cell contents:
- **Given clues:** `type.numeral` (Cormorant Garamond), semibold, 22sp, color `ink` (night: `paper`). Slight letter-spacing -0.5.
- **User entries:** `type.hand` (Caveat), medium, 24sp, color `teal` (night: `teal` lighter). This is the single visual signal that a digit is yours.
- **Notes:** 3×3 mini-grid inside the cell, `type.hand`, cellSize × 0.28sp, color `ink` at 45% opacity.
- **Conflict digit** recolors to `red`.

Grid lines (drawn as a single `Canvas` overlay):
- Thin inner lines at cells 1,2,4,5,7,8: `ink` at 25% opacity, 0.5.dp
- Thick box lines at cells 0,3,6,9: `ink` at 85% opacity, 1.5.dp
- Outer border: 1.dp `ink`.

Paper background under the grid: `paperGlow` (slightly lifted).

## Interaction

- Tap cell → select (propagates row/col/box highlight instantly).
- Number pad tap → enter digit or note (respects `notesMode`).
- Long-press number → note toggle regardless of mode.
- Two taps on same cell → clear selection.
- Double-entering a wrong digit shows a brief `conflict` flash (haptic rigid) — does NOT auto-erase; user can undo or erase.

## Completion detection

- **Digit complete** (all 9 of one digit placed): trigger a subtle ink-pulse on every cell of that digit, 800ms `Ease.bleed`, 2-hue range. No sound.
- **House complete** (row, column, or 3×3 box filled and consistent): trigger `AuroraSweep` over that house, 1000ms, 4-hue range. Soft haptic.
- **Board complete & consistent**: fade board to `paperGlow` 40%, fire `AuroraSweep(Win)` across entire grid (3000ms, full spectrum), then transition to Win screen after 400ms hold.

## Do NOT

- Do not show a "wrong!" dialog.
- Do not auto-advance to the next puzzle; the user chooses from Win screen.
- Do not animate cell fill-in with bouncing springs. Digits appear with a soft 120ms ink-settle opacity 0→1.
