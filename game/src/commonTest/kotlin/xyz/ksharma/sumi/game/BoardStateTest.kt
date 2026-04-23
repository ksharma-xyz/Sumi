@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game

import xyz.ksharma.sumi.game.generator.SudokuGenerator
import xyz.ksharma.sumi.game.model.Difficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BoardStateTest {

    private val board get() = SudokuGenerator.generate(Difficulty.Easy, seed = 1000L)

    @Test
    fun selectAndDeselectCell() {
        val b = board.select(0, 0)
        assertEquals(Pair(0, 0), b.selected)
        val b2 = b.select(0, 0)
        assertNull(b2.selected, "Selecting same cell twice should deselect")
    }

    @Test
    fun enterDigitInEmptyCell() {
        val b = board
        val (row, col) = findEmptyCell(b)
        val b2 = b.select(row, col).enter(5)
        assertEquals(5, b2.cells[row][col].value)
    }

    @Test
    fun enterDigitDoesNotOverwriteGiven() {
        val b = board
        val (row, col) = findGivenCell(b)
        val original = b.cells[row][col].value
        val b2 = b.select(row, col).enter(9)
        assertEquals(original, b2.cells[row][col].value, "Given cell must not be overwritten")
    }

    @Test
    fun eraseRemovesUserEntry() {
        val b = board
        val (row, col) = findEmptyCell(b)
        val b2 = b.select(row, col).enter(3).erase()
        assertEquals(0, b2.cells[row][col].value)
    }

    @Test
    fun eraseDoesNotAffectGiven() {
        val b = board
        val (row, col) = findGivenCell(b)
        val original = b.cells[row][col].value
        val b2 = b.select(row, col).erase()
        assertEquals(original, b2.cells[row][col].value)
    }

    @Test
    fun undoRestoresPreviousState() {
        val b = board
        val (row, col) = findEmptyCell(b)
        val b2 = b.select(row, col).enter(7)
        assertEquals(7, b2.cells[row][col].value)
        val b3 = b2.undo()
        assertEquals(0, b3.cells[row][col].value)
    }

    @Test
    fun notesModeTogglesNotes() {
        val b = board.toggleNotes()
        assertTrue(b.notesMode)
        val (row, col) = findEmptyCell(b)
        val b2 = b.select(row, col).enter(4)
        assertTrue(4 in b2.cells[row][col].notes)
        val b3 = b2.enter(4)
        assertFalse(4 in b3.cells[row][col].notes, "Entering same note twice should remove it")
    }

    @Test
    fun hintRevealsSolutionValue() {
        val b = board
        val (row, col) = findEmptyCell(b)
        val b2 = b.hint()
        val revealedRow = b2.cells.indexOfFirst { row2 -> row2.any { it.given && board.cells[b2.cells.indexOf(row2)][row2.indexOf(it)].isEmpty } }
        assertTrue(b2.hintsRemaining <= b.hintsRemaining, "Hints should decrease after using hint")
    }

    @Test
    fun isNotCompleteWithEmptyCells() {
        assertFalse(board.isComplete)
    }

    @Test
    fun conflictDetectedForDuplicateRow() {
        val b = board
        val (row, col) = findEmptyCell(b)
        val existingInRow = b.cells[row].firstOrNull { !it.isEmpty }?.value ?: return
        val b2 = b.select(row, col).enter(existingInRow)
        assertNotNull(b2.conflict, "Duplicate in row should set conflict")
    }

    @Test
    fun remainingCountsAreCorrect() {
        val b = board
        val counts = b.remainingCounts
        for (i in 0..8) {
            val digit = i + 1
            val placed = b.cells.sumOf { row -> row.count { it.value == digit } }
            assertEquals(9 - placed, counts[i], "remainingCounts[$i] is wrong")
        }
    }

    private fun findEmptyCell(b: xyz.ksharma.sumi.game.model.BoardState): Pair<Int, Int> {
        for (r in 0..8) for (c in 0..8) if (b.cells[r][c].isEmpty) return r to c
        error("No empty cell found")
    }

    private fun findGivenCell(b: xyz.ksharma.sumi.game.model.BoardState): Pair<Int, Int> {
        for (r in 0..8) for (c in 0..8) if (b.cells[r][c].given) return r to c
        error("No given cell found")
    }
}
