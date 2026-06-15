@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game

import xyz.ksharma.sumi.game.model.Cell
import xyz.ksharma.sumi.game.model.SudokuLogic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SudokuLogicTest {

    private fun grid(values: List<List<Int>>): List<List<Cell>> =
        values.map { row -> row.map { Cell(value = it, given = it != 0) } }

    @Test
    fun candidatesExcludeRowColBoxDigits() {
        val g = MutableList(9) { MutableList(9) { 0 } }
        g[0][1] = 2 // same row
        g[0][2] = 3 // same row (and box)
        g[3][0] = 4 // same column
        g[1][1] = 5 // same box
        val cand = SudokuLogic.candidates(grid(g), 0, 0)
        assertFalse(2 in cand)
        assertFalse(3 in cand)
        assertFalse(4 in cand)
        assertFalse(5 in cand)
        assertTrue(1 in cand)
        assertTrue(6 in cand)
    }

    @Test
    fun nakedSingleLeavesExactlyOneCandidate() {
        // Row 0 holds 1..8, so the only empty cell (0,0) can only be 9.
        val g = MutableList(9) { MutableList(9) { 0 } }
        for (c in 1..8) g[0][c] = c
        assertEquals(setOf(9), SudokuLogic.candidates(grid(g), 0, 0))
    }

    @Test
    fun filledCellHasNoCandidates() {
        val g = MutableList(9) { MutableList(9) { 0 } }
        g[4][4] = 7
        assertTrue(SudokuLogic.candidates(grid(g), 4, 4).isEmpty())
    }
}
