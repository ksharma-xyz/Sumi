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
    fun thirdMistakeTriggersGameOver() {
        var b = board
        var mistakes = 0
        outer@ for (r in 0..8) {
            for (c in 0..8) {
                if (b.cells[r][c].given) continue
                val wrong = wrongDigitFor(b, r, c)
                b = b.select(r, c).enter(wrong)
                mistakes++
                if (mistakes == 3) break@outer
            }
        }
        assertTrue(b.isGameOver, "Three mistakes should set isGameOver")
    }

    @Test
    fun boardLockedAfterGameOver() {
        var b = board
        var mistakes = 0
        outer@ for (r in 0..8) {
            for (c in 0..8) {
                if (b.cells[r][c].given) continue
                b = b.select(r, c).enter(wrongDigitFor(b, r, c))
                mistakes++
                if (mistakes == 3) break@outer
            }
        }
        assertTrue(b.isGameOver)
        val (r, c) = findEmptyCell(b)
        val before = b.cells[r][c].value
        val b2 = b.select(r, c).enter(b.solution[r][c])
        assertEquals(before, b2.cells[r][c].value, "Board must be locked after game over")
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
    fun timerStopsWhenGameOver() {
        var b = board
        var mistakes = 0
        outer@ for (r in 0..8) {
            for (c in 0..8) {
                if (b.cells[r][c].given) continue
                b = b.select(r, c).enter(wrongDigitFor(b, r, c))
                mistakes++
                if (mistakes == 3) break@outer
            }
        }
        val elapsed = b.elapsedMs
        assertEquals(elapsed, b.tick(1000L).elapsedMs, "Timer must not advance after game over")
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
}
