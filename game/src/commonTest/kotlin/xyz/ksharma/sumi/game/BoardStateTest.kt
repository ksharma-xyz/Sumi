@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game

import xyz.ksharma.sumi.game.generator.SudokuGenerator
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Cell
import xyz.ksharma.sumi.game.model.Difficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BoardStateTest {

    private val board get() = SudokuGenerator.generate(Difficulty.Easy, seed = 1000L)

    // ── Selection ─────────────────────────────────────────────────────────────

    @Test
    fun selectAndDeselectCell() {
        val b = board.select(0, 0)
        assertEquals(Pair(0, 0), b.selected)
        assertNull(b.select(0, 0).selected, "Selecting same cell twice should deselect")
    }

    // ── Enter — correct digit ─────────────────────────────────────────────────

    @Test
    fun enterCorrectDigitPlacesValue() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val correct = b.solution[r][c]
        val b2 = b.select(r, c).enter(correct)
        assertEquals(correct, b2.cells[r][c].value)
    }

    @Test
    fun enterCorrectDigitDoesNotIncrementMistakes() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val b2 = b.select(r, c).enter(b.solution[r][c])
        assertEquals(0, b2.mistakeCount)
    }

    @Test
    fun enterCorrectDigitProducesNoErrorCells() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val b2 = b.select(r, c).enter(b.solution[r][c])
        assertFalse((r to c) in b2.errorCells)
    }

    // ── Enter — wrong digit ───────────────────────────────────────────────────

    @Test
    fun enterWrongDigitIncrementsMistakeCount() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val wrong = wrongDigitFor(b, r, c)
        val b2 = b.select(r, c).enter(wrong)
        assertEquals(1, b2.mistakeCount)
    }

    @Test
    fun enterWrongDigitAppearsInErrorCells() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val wrong = wrongDigitFor(b, r, c)
        val b2 = b.select(r, c).enter(wrong)
        assertTrue((r to c) in b2.errorCells, "Wrong digit should appear in errorCells")
    }

    @Test
    fun enteringDifferentWrongDigitInSameCellDoesNotDoubleMistake() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val wrong1 = wrongDigitFor(b, r, c)
        val wrong2 = alternateWrongDigit(b, r, c, wrong1)
        val b2 = b.select(r, c).enter(wrong1).enter(wrong2)
        assertEquals(1, b2.mistakeCount, "Re-entering a wrong digit on a wrong cell should not add another mistake")
    }

    @Test
    fun correcting_wrongCellRemovesFromErrorCells() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val wrong = wrongDigitFor(b, r, c)
        val b2 = b.select(r, c).enter(wrong)
        assertTrue((r to c) in b2.errorCells)
        val b3 = b2.enter(b.solution[r][c])
        assertFalse((r to c) in b3.errorCells, "Correcting a wrong cell should clear the error")
    }

    @Test
    fun mistakesAccumulateWithoutLockingBoard() {
        // Game does not end on mistakes — count keeps climbing and the board stays editable.
        var b = board
        var mistakes = 0
        outer@ for (r in 0..8) {
            for (c in 0..8) {
                if (b.cells[r][c].given) continue
                b = b.select(r, c).enter(wrongDigitFor(b, r, c))
                mistakes++
                if (mistakes == 5) break@outer
            }
        }
        assertEquals(5, b.mistakeCount)
        // Board must still accept new entries — pick another empty cell and enter the correct value.
        val (r, c) = findEmptyCell(b)
        val correct = b.solution[r][c]
        val b2 = b.select(r, c).enter(correct)
        assertEquals(correct, b2.cells[r][c].value, "Board must remain editable regardless of mistake count")
    }

    // ── Erase ─────────────────────────────────────────────────────────────────

    @Test
    fun eraseRemovesUserEntry() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val b2 = b.select(r, c).enter(b.solution[r][c]).erase()
        assertEquals(0, b2.cells[r][c].value)
    }

    @Test
    fun eraseDoesNotAffectGiven() {
        val b = board
        val (r, c) = findGivenCell(b)
        val original = b.cells[r][c].value
        assertEquals(original, b.select(r, c).erase().cells[r][c].value)
    }

    @Test
    fun eraseOnWrongCellRemovesError() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val b2 = b.select(r, c).enter(wrongDigitFor(b, r, c))
        assertTrue((r to c) in b2.errorCells)
        val b3 = b2.erase()
        assertFalse((r to c) in b3.errorCells, "Erasing a wrong cell should clear the error")
    }

    // ── Undo ──────────────────────────────────────────────────────────────────

    @Test
    fun undoRestoresPreviousState() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val b2 = b.select(r, c).enter(7)
        assertEquals(7, b2.cells[r][c].value)
        assertEquals(0, b2.undo().cells[r][c].value)
    }

    @Test
    fun undoClearsErrorCell() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val b2 = b.select(r, c).enter(wrongDigitFor(b, r, c))
        assertTrue((r to c) in b2.errorCells)
        assertFalse((r to c) in b2.undo().errorCells, "Undo should clear error for that cell")
    }

    @Test
    fun undoOnEmptyHistoryIsNoOp() {
        val b = board
        assertEquals(b.cells, b.undo().cells)
    }

    // ── Notes ─────────────────────────────────────────────────────────────────

    @Test
    fun notesModeTogglesNotes() {
        val b = board.toggleNotes()
        assertTrue(b.notesMode)
        val (r, c) = findEmptyCell(b)
        val b2 = b.select(r, c).enter(4)
        assertTrue(4 in b2.cells[r][c].notes)
        assertFalse(4 in b2.enter(4).cells[r][c].notes, "Entering same note twice should remove it")
    }

    @Test
    fun notesDoNotCountAsMistakes() {
        val b = board.toggleNotes()
        val (r, c) = findEmptyCell(b)
        val wrong = wrongDigitFor(b, r, c)
        val b2 = b.select(r, c).enter(wrong)
        assertEquals(0, b2.mistakeCount, "Notes must not count as mistakes")
        assertFalse((r to c) in b2.errorCells, "Notes must not produce error cells")
    }

    // ── Hint ──────────────────────────────────────────────────────────────────

    @Test
    fun hintRevealsSolutionValue() {
        val b = board
        val b2 = b.hint()
        assertTrue(b2.hintsRemaining < b.hintsRemaining, "Hint should decrement hintsRemaining")
        for (r in 0..8) for (c in 0..8) {
            if (!b.cells[r][c].given && b2.cells[r][c].given) {
                assertEquals(b2.solution[r][c], b2.cells[r][c].value, "Revealed cell must match solution")
                return
            }
        }
    }

    @Test
    fun hintExhaustsAtZero() {
        var b = board
        repeat(3) { b = b.hint() }
        assertEquals(0, b.hintsRemaining)
        val b2 = b.hint()
        assertEquals(0, b2.hintsRemaining, "Hints must not go below zero")
    }

    @Test
    fun hintClearsErrorInTargetCell() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val b2 = b.select(r, c).enter(wrongDigitFor(b, r, c))
        assertTrue((r to c) in b2.errorCells)
        val b3 = b2.hint()
        val revealedErrorCells = b3.errorCells
        // At least the hint cell should now be correct (given = true, value = solution)
        assertTrue(b3.hintsRemaining < b2.hintsRemaining)
        // All given=true cells must have correct values
        for (rr in 0..8) for (cc in 0..8) {
            if (b3.cells[rr][cc].given) {
                assertEquals(b3.solution[rr][cc], b3.cells[rr][cc].value, "Hint cell must match solution")
                assertFalse((rr to cc) in revealedErrorCells)
            }
        }
    }

    // ── isComplete ────────────────────────────────────────────────────────────

    @Test
    fun isNotCompleteWithEmptyCells() {
        assertFalse(board.isComplete)
    }

    @Test
    fun isCompleteWhenAllCellsMatchSolution() {
        val b = fillBoardCorrectly(board)
        assertTrue(b.isComplete)
    }

    @Test
    fun isNotCompleteWithWrongNonDuplicateCell() {
        // A wrong digit that doesn't duplicate in its row/col/box would have
        // triggered hasConflicts=false under the old logic — this must still be incomplete.
        val b = board
        val (r, c) = findEmptyCell(b)
        val wrong = wrongDigitFor(b, r, c)
        val b2 = b.select(r, c).enter(wrong)
        assertFalse(b2.isComplete)
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    @Test
    fun timerAdvancesNormally() {
        val b = board.tick(1000L)
        assertEquals(1000L, b.elapsedMs)
    }

    @Test
    fun timerKeepsAdvancingDespiteMistakes() {
        // Mistakes no longer end the game; timer only stops when the puzzle is solved.
        var b = board
        var mistakes = 0
        outer@ for (r in 0..8) {
            for (c in 0..8) {
                if (b.cells[r][c].given) continue
                b = b.select(r, c).enter(wrongDigitFor(b, r, c))
                mistakes++
                if (mistakes == 5) break@outer
            }
        }
        val elapsed = b.elapsedMs
        assertEquals(elapsed + 1000L, b.tick(1000L).elapsedMs, "Timer must keep advancing while the puzzle is unsolved")
    }

    @Test
    fun timerStopsWhenComplete() {
        val b = fillBoardCorrectly(board)
        assertTrue(b.isComplete)
        val elapsed = b.elapsedMs
        assertEquals(elapsed, b.tick(1000L).elapsedMs, "Timer must not advance after completion")
    }

    // ── Error cells — row / column / box ─────────────────────────────────────

    @Test
    fun errorCellsEmptyOnFreshBoard() {
        assertTrue(board.errorCells.isEmpty())
    }

    @Test
    fun wrongDigitInColumnIsError() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val b2 = b.select(r, c).enter(wrongDigitFor(b, r, c))
        assertTrue((r to c) in b2.errorCells)
    }

    @Test
    fun multipleWrongCellsAllAppearInErrorSet() {
        val b = board
        val empties = allEmptyCells(b).take(2)
        var b2 = b
        for ((r, c) in empties) {
            b2 = b2.select(r, c).enter(wrongDigitFor(b2, r, c))
        }
        for ((r, c) in empties) {
            assertTrue((r to c) in b2.errorCells, "Cell ($r,$c) should be in errorCells")
        }
    }

    // ── Completion detection ──────────────────────────────────────────────────

    @Test
    fun completedRowsEmptyOnFreshBoard() {
        assertTrue(board.completedRows.isEmpty(), "Fresh board should have no completed rows")
    }

    @Test
    fun completedColsEmptyOnFreshBoard() {
        assertTrue(board.completedCols.isEmpty(), "Fresh board should have no completed cols")
    }

    @Test
    fun completedBoxesEmptyOnFreshBoard() {
        assertTrue(board.completedBoxes.isEmpty(), "Fresh board should have no completed boxes")
    }

    @Test
    fun completedDigitsEmptyOnFreshBoard() {
        assertTrue(board.completedDigits.isEmpty(), "Fresh board should have no completed digits")
    }

    @Test
    fun completedRowsContainsRowWhenFullyCorrect() {
        val b = board
        // Fill a specific row completely and correctly
        val targetRow = findRowWithMostGivens(b)
        var state = b
        for (c in 0..8) {
            if (!state.cells[targetRow][c].given) {
                state = state.select(targetRow, c).enter(state.solution[targetRow][c])
            }
        }
        assertTrue(targetRow in state.completedRows, "Row $targetRow should be in completedRows")
    }

    @Test
    fun completedRowsDoesNotIncludeRowWithWrongDigit() {
        val b = board
        val targetRow = findRowWithMostGivens(b)
        var state = b
        for (c in 0..8) {
            if (!state.cells[targetRow][c].given) {
                val correct = state.solution[targetRow][c]
                val wrong = (1..9).first { it != correct }
                state = state.select(targetRow, c).enter(wrong)
            }
        }
        assertFalse(targetRow in state.completedRows, "Row with wrong digit must not be completed")
    }

    @Test
    fun completedColsContainsColWhenFullyCorrect() {
        val b = board
        val targetCol = findColWithMostGivens(b)
        var state = b
        for (r in 0..8) {
            if (!state.cells[r][targetCol].given) {
                state = state.select(r, targetCol).enter(state.solution[r][targetCol])
            }
        }
        assertTrue(targetCol in state.completedCols, "Col $targetCol should be in completedCols")
    }

    @Test
    fun completedBoxesContainsBoxWhenFullyCorrect() {
        val b = board
        val targetBox = findBoxWithMostGivens(b)
        var state = b
        val br = (targetBox / 3) * 3
        val bc = (targetBox % 3) * 3
        for (dr in 0..2) for (dc in 0..2) {
            if (!state.cells[br + dr][bc + dc].given) {
                state = state.select(br + dr, bc + dc).enter(state.solution[br + dr][bc + dc])
            }
        }
        assertTrue(targetBox in state.completedBoxes, "Box $targetBox should be in completedBoxes")
    }

    @Test
    fun completedDigitsContainsDigitWhenAllNinePlaced() {
        val b = board
        // Find a digit that has the fewest remaining empty cells
        val targetDigit = (1..9).minByOrNull { d ->
            (0..8).sumOf { r -> (0..8).count { c -> b.cells[r][c].value != d && b.solution[r][c] == d && !b.cells[r][c].given } }
        } ?: 1
        var state = b
        for (r in 0..8) for (c in 0..8) {
            if (state.solution[r][c] == targetDigit && !state.cells[r][c].given) {
                state = state.select(r, c).enter(targetDigit)
            }
        }
        assertTrue(targetDigit in state.completedDigits, "Digit $targetDigit should be in completedDigits")
    }

    @Test
    fun allHousesCompleteOnSolvedBoard() {
        val b = fillBoardCorrectly(board)
        assertEquals((0..8).toSet(), b.completedRows, "All rows should be complete on solved board")
        assertEquals((0..8).toSet(), b.completedCols, "All cols should be complete on solved board")
        assertEquals((0..8).toSet(), b.completedBoxes, "All boxes should be complete on solved board")
        assertEquals((1..9).toSet(), b.completedDigits, "All digits should be complete on solved board")
    }

    @Test
    fun remainingCountsZeroForCompletedDigit() {
        val b = board
        val targetDigit = (1..9).minByOrNull { d ->
            (0..8).sumOf { r -> (0..8).count { c -> b.solution[r][c] == d && !b.cells[r][c].given } }
        } ?: 1
        var state = b
        for (r in 0..8) for (c in 0..8) {
            if (state.solution[r][c] == targetDigit && !state.cells[r][c].given) {
                state = state.select(r, c).enter(targetDigit)
            }
        }
        assertEquals(0, state.remainingCounts[targetDigit - 1], "Remaining count should be 0 for completed digit")
    }

    // ── Remaining counts ──────────────────────────────────────────────────────

    @Test
    fun remainingCountsAreCorrect() {
        val b = board
        val counts = b.remainingCounts
        for (i in 0..8) {
            val placed = b.cells.sumOf { row -> row.count { it.value == i + 1 } }
            assertEquals(9 - placed, counts[i])
        }
    }

    @Test
    fun remainingCountsUpdateAfterCorrectEntry() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val digit = b.solution[r][c]
        val before = b.remainingCounts[digit - 1]
        val b2 = b.select(r, c).enter(digit)
        assertEquals(before - 1, b2.remainingCounts[digit - 1])
    }

    @Test
    fun remainingCountsSumToCorrectTotal() {
        val b = board
        val totalEmpty = b.cells.sumOf { row -> row.count { it.value == 0 } }
        val sumRemaining = b.remainingCounts.sum()
        // Each remaining count = 9 - placed; sum = 81 - total placed = totalEmpty + 0-valued cells.
        // More precisely: sum of remaining = 9*9 - sum of placed = 81 - (81 - emptyCount) = emptyCount.
        assertEquals(totalEmpty, sumRemaining)
    }

    // ── Notes cleared on digit entry ──────────────────────────────────────────

    @Test
    fun notesAreCleared_whenDigitEnteredInNormalMode() {
        val b = board
        val (r, c) = findEmptyCell(b)
        // First add some notes in notes mode
        val withNotes = b.toggleNotes().select(r, c).enter(3).enter(5)
        assertTrue(3 in withNotes.cells[r][c].notes)
        assertTrue(5 in withNotes.cells[r][c].notes)
        // Switch back to normal mode and enter a digit
        val afterDigit = withNotes.toggleNotes().enter(withNotes.solution[r][c])
        assertTrue(afterDigit.cells[r][c].notes.isEmpty(), "Notes must be cleared when a digit is entered")
    }

    @Test
    fun notesAreCleared_whenErasing() {
        val b = board
        val (r, c) = findEmptyCell(b)
        val withNotes = b.toggleNotes().select(r, c).enter(7)
        assertTrue(7 in withNotes.cells[r][c].notes)
        val afterErase = withNotes.erase()
        assertTrue(afterErase.cells[r][c].notes.isEmpty(), "Erase must clear notes")
    }

    @Test
    fun enteringSameNoteToggleItOff() {
        val b = board.toggleNotes()
        val (r, c) = findEmptyCell(b)
        val with4 = b.select(r, c).enter(4)
        assertTrue(4 in with4.cells[r][c].notes)
        val without4 = with4.enter(4)
        assertFalse(4 in without4.cells[r][c].notes, "Re-entering same note should remove it")
    }

    // ── Auto-strip peer notes on digit entry ──────────────────────────────────

    @Test
    fun placingDigitStripsThatNoteFromUnitPeers() {
        val b = board
        val empties = allEmptyCells(b)
        val (r, c) = empties.first()
        val digit = b.solution[r][c]
        val rowPeer = empties.firstOrNull { (pr, pc) -> pr == r && pc != c }
        assertNotNull(rowPeer, "seed must leave a second empty cell in the row")
        val (pr, pc) = rowPeer
        // Seed `digit` as a pencil mark in the row peer, then place it in the target cell.
        val seeded = b.toggleNotes().select(pr, pc).enter(digit).toggleNotes()
        assertTrue(digit in seeded.cells[pr][pc].notes)
        val after = seeded.select(r, c).enter(digit)
        assertFalse(digit in after.cells[pr][pc].notes, "digit must be stripped from row-peer notes")
    }

    @Test
    fun placingDigitKeepsNoteInNonPeerCells() {
        val b = board
        val empties = allEmptyCells(b)
        val (r, c) = empties.first()
        val digit = b.solution[r][c]
        val nonPeer = empties.firstOrNull { (pr, pc) ->
            pr != r && pc != c && !(pr / 3 == r / 3 && pc / 3 == c / 3)
        }
        assertNotNull(nonPeer, "seed must leave a non-peer empty cell")
        val (pr, pc) = nonPeer
        val seeded = b.toggleNotes().select(pr, pc).enter(digit).toggleNotes()
        assertTrue(digit in seeded.cells[pr][pc].notes)
        val after = seeded.select(r, c).enter(digit)
        assertTrue(digit in after.cells[pr][pc].notes, "non-peer notes must be preserved")
    }

    // ── Multiple difficulties ─────────────────────────────────────────────────

    @Test
    fun allDifficultiesProduceValidBoardState() {
        Difficulty.entries.forEach { diff ->
            val b = SudokuGenerator.generate(diff, seed = 2024L)
            assertEquals(diff, b.difficulty)
            assertFalse(b.isComplete, "Fresh board must not be complete ($diff)")
            assertTrue(b.errorCells.isEmpty(), "Fresh board must have no errors ($diff)")
            assertEquals(3, b.hintsRemaining)
        }
    }

    @Test
    fun harderDifficultiesHaveFewerGivens() {
        val easy = SudokuGenerator.generate(Difficulty.Easy, seed = 42L)
        val hard = SudokuGenerator.generate(Difficulty.Hard, seed = 42L)
        val easyGivens = easy.cells.sumOf { row -> row.count { it.given } }
        val hardGivens = hard.cells.sumOf { row -> row.count { it.given } }
        assertTrue(easyGivens > hardGivens, "Easy must have more givens than Hard")
    }

    @Test
    fun selectEnterEraseUndoWorkOnAllDifficulties() {
        Difficulty.entries.forEach { diff ->
            val b = SudokuGenerator.generate(diff, seed = 777L)
            val (r, c) = findEmptyCell(b)
            val correct = b.solution[r][c]
            val b2 = b.select(r, c).enter(correct)
            assertEquals(correct, b2.cells[r][c].value, "enter should place digit ($diff)")
            val b3 = b2.erase()
            assertEquals(0, b3.cells[r][c].value, "erase should clear digit ($diff)")
            val b4 = b2.undo()
            assertEquals(0, b4.cells[r][c].value, "undo should revert entry ($diff)")
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun findEmptyCell(b: BoardState): Pair<Int, Int> {
        for (r in 0..8) for (c in 0..8) if (b.cells[r][c].isEmpty) return r to c
        error("No empty cell found")
    }

    private fun findGivenCell(b: BoardState): Pair<Int, Int> {
        for (r in 0..8) for (c in 0..8) if (b.cells[r][c].given) return r to c
        error("No given cell found")
    }

    private fun allEmptyCells(b: BoardState): List<Pair<Int, Int>> =
        (0..8).flatMap { r -> (0..8).mapNotNull { c -> if (b.cells[r][c].isEmpty) r to c else null } }

    private fun wrongDigitFor(b: BoardState, r: Int, c: Int): Int {
        val correct = b.solution[r][c]
        return (1..9).first { it != correct }
    }

    private fun alternateWrongDigit(b: BoardState, r: Int, c: Int, exclude: Int): Int {
        val correct = b.solution[r][c]
        return (1..9).first { it != correct && it != exclude }
    }

    private fun fillBoardCorrectly(b: BoardState): BoardState {
        var state = b
        for (r in 0..8) for (c in 0..8) {
            if (!state.cells[r][c].given) {
                state = state.select(r, c).enter(state.solution[r][c])
            }
        }
        return state
    }

    private fun findRowWithMostGivens(b: BoardState): Int =
        (0..8).maxByOrNull { r -> (0..8).count { c -> b.cells[r][c].given } } ?: 0

    private fun findColWithMostGivens(b: BoardState): Int =
        (0..8).maxByOrNull { c -> (0..8).count { r -> b.cells[r][c].given } } ?: 0

    private fun findBoxWithMostGivens(b: BoardState): Int =
        (0..8).maxByOrNull { b ->
            val br = (b / 3) * 3; val bc = (b % 3) * 3
            (0..2).sumOf { dr -> (0..2).count { dc -> board.cells[br + dr][bc + dc].given } }
        } ?: 0
}
