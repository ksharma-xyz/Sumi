# Sumi changelog

One section per version. The newest unreleased version is at the top; move it
to a dated heading when it ships. The "What's new" block is store / in-app copy;
the table below it is the technical record.

Copy rule: no middle dot, no em dash anywhere in user-facing text.

Every shipped version is tagged `v{version}` on `main` and has a matching
GitHub Release. Store copy for each version lives in `docs/release-notes/`.

## 1.5.0 (next version, unreleased, on `main`)

### What's new (user-facing)

Easier on the eyes.

- Pencil marks are bigger. The candidate numbers inside a square grew from 30%
  to 36% of the cell, so a square full of notes reads at a glance.

### Changes (technical record)

| Area | Commit | Summary |
|------|--------|---------|
| Board | `1adb0b2` | `NOTE_SIZE_RATIO` 0.30 -> 0.36 in `SumiBoard.kt` |

## 1.4.0 (cut as `prod/1.4.0`, RC tag `v1.4.0-RC1`, not yet in production)

Maintenance release. No user-facing changes.

### Changes (technical record)

| Area | Commit | Summary |
|------|--------|---------|
| Billing | `30c8ae0` | Play Billing Library 7.1.1 -> 9.0.0 plus `enableAutoServiceReconnection()`. Required: Google rejects updates on Billing below 8.0.0 from 31 Aug 2026 |
| Generator | `2d427bf` | Fold `continue` into the `if` in the MRV scan (no behaviour change) |
| CI | `91f9cab` | Guard clause around the `release-1-cut` MARKETING_VERSION bump |

### Notes for QA / release
- Android only in substance. The iOS build carries nothing new.
- Verify the Pro one-time purchase and restore flow on Billing 9.0.0 before
  promoting past the internal track.

## 1.3.0 (released 2026-07-07, tag `v1.3.0`, branch `prod/1.3`)

### What's new (user-facing)

Never lose your place.

- Your in-progress game is kept, even days later, instead of being discarded
  after a day.
- The board blooms onto the screen like ink spreading on paper.
- Puzzles are cached, so opening a game resumes instantly and new puzzles
  generate faster.
- No more pause-overlay flash when returning from the background.
- Bigger, better-contained pencil marks.

### Changes (technical record)

| Area | Commit | Summary |
|------|--------|---------|
| Board / ads | `0c1300b` | Bigger, contained pencil notes; difficulty-aware idle ad gap |
| Fix / perf | `813fef4` | No pause-overlay flash on background; puzzle cache for instant resume |
| Motion / perf | `91e9a3f` | Ink-bloom board entrance; faster puzzle generation |
| Fix | `1a9be70` | Resume in-progress game instead of discarding it after a day |
| Release | `838d5a3` | Bump to 1.3; fix `release-1-cut` iOS version bump no-op |

## 1.2.0 (released 2026-06-15, tag `v1.2.0`)

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

## 1.1.0 (released 2026-06-14, tag `v1.1.0`)

No changelog section was written at the time. See the `v1.1.0` GitHub Release
for the commit list.
