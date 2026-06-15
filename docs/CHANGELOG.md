# Sumi changelog

One section per version. The newest unreleased version is at the top; move it
to a dated heading when it ships. The "What's new" block is store / in-app copy;
the table below it is the technical record.

Copy rule: no middle dot, no em dash anywhere in user-facing text.

## 1.2 (next version, unreleased)

### What's new (user-facing)

Smarter, calmer, and more legible Sudoku.

- Your pencil marks now stick around. Notes are saved with the puzzle, so they
  survive closing and reopening the app, and a note clears itself from the rest
  of a row, column, or box the moment you place that number.
- A clearer board. Bigger, fixed-size numbers that no longer shrink or grow with
  your phone's text-size setting, tighter margins so the grid is larger, and
  bolder lines around the nine boxes so the grid reads at a glance.
- Smarter hints. A hint now points you at a square you could actually work out
  next, instead of revealing a random one.
- Auto-fill notes. Long-press the Notes button to fill every empty square with
  its possible numbers.
- Redo. Step forward again after an undo.
- New ways to play, in Settings:
  - High-legibility numerals - a clearer typeface for the board and number pad.
  - Conflicts only - highlight just the squares that break a row, column, or box
    rule, instead of flagging anything that differs from the solution.
  - Digit-first input - tap a number, then tap squares to place it.
  - Lives mode - the puzzle ends after a few mistakes, for a tougher challenge.
- The timer now pauses when you leave the app and freezes while paused, so time
  away never counts against your solve. The board also reopens on the square you
  last selected.

### Changes (technical record)

Commits on the working branch, oldest first.

| Area | Commit | Summary |
|------|--------|---------|
| Board | `4f87528` | Persist pencil notes in GameSave; fixed-size dp->sp digits/notes (ignore OS font scale); tighter side padding; stronger 3x3 box lines; SumiBoardPreview |
| Notes | `c2cc3c3` | Auto-strip a placed digit from peer cells' notes (+ tests) |
| Undo/redo | `43a2bc8` | Redo stack in BoardState (canUndo/canRedo), Redo tool, dim when empty (+ tests) |
| Hints | `59cfcf2` | SudokuLogic candidate engine + naked/hidden-single detection; hint() targets a logically-solvable cell; BoardState.fillNotes() (+ tests) |
| Notes | `989ea2d` | One-shot fill-notes via long-press on the Note tool |
| Setting | `80a45da` | High-legibility numerals toggle (board) |
| Setting | `60eab2b` | Conflicts-only highlight toggle; BoardState.conflictCells (+ tests) |
| Setting | `5bcc1d9` | Digit-first input toggle; BoardState.placeAt; armed-digit state (+ tests) |
| Timer/state | `966f737` | Auto-pause on background; freeze timer while paused; persist selected cell |
| Mode | `e0f96c8` | Lives mode (MAX_LIVES = 3) + GameOverRoute / GameOverScreen / GameOverEntry |
| Fix | `458ec94` | Reset paused in init() - fixes frozen timer on a new game (reused ViewModel) |
| Polish | `fedde65` | No pause-overlay flash before game-over; high-legibility also applies to the number pad |

### Already present before this work (verified, not changed)
- Number pad shows remaining count per digit and disables a fully-placed digit.
- Haptics (tick / confirm / error / win) with a Settings toggle.

### New settings (all persisted via DataStore, default off)
- High-legibility numerals
- Conflicts only
- Digit-first input
- Lives mode

### Notes for QA / release
- Lives limit is `MAX_LIVES = 3` (`xyz.ksharma.sumi.screens.game.MAX_LIVES`).
- Auto-fill notes is discoverable only via long-press today; consider a visible
  affordance later.
- Release is iOS-only and shipped manually from Xcode.
