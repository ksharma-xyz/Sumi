@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game

import xyz.ksharma.sumi.game.generator.SudokuGenerator
import xyz.ksharma.sumi.game.model.Difficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SudokuGeneratorTest {

    @Test
    fun generatedBoardHasCorrectGivenCount() {
        Difficulty.entries.forEach { diff ->
            val board = SudokuGenerator.generate(diff, seed = 42L)
            val givens = board.cells.sumOf { row -> row.count { it.given } }
            assertTrue(
                givens >= diff.givenCount - 5 && givens <= diff.givenCount + 5,
                "Difficulty ${diff.name}: expected ~${diff.givenCount} givens, got $givens",
            )
        }
    }

    @Test
    fun solutionIsValidSudoku() {
        val board = SudokuGenerator.generate(Difficulty.Medium, seed = 99L)
        val sol = board.solution
        for (i in 0..8) {
            val rowVals = sol[i].toSet()
            assertEquals(9, rowVals.size, "Row $i has duplicate values")
            assertTrue(rowVals.containsAll((1..9).toList()), "Row $i missing values")
        }
        for (c in 0..8) {
            val colVals = sol.map { it[c] }.toSet()
            assertEquals(9, colVals.size, "Col $c has duplicate values")
        }
        for (br in 0..2) {
            for (bc in 0..2) {
                val boxVals = mutableSetOf<Int>()
                for (r in (br * 3)..(br * 3 + 2)) {
                    for (c in (bc * 3)..(bc * 3 + 2)) {
                        boxVals.add(sol[r][c])
                    }
                }
                assertEquals(9, boxVals.size, "Box ($br,$bc) has duplicate values")
            }
        }
    }

    @Test
    fun sameSeedProducesSamePuzzle() {
        val a = SudokuGenerator.generate(Difficulty.Easy, seed = 12345L)
        val b = SudokuGenerator.generate(Difficulty.Easy, seed = 12345L)
        assertEquals(a.cells, b.cells)
        assertEquals(a.solution, b.solution)
    }

    @Test
    fun differentSeedsProduceDifferentPuzzles() {
        val a = SudokuGenerator.generate(Difficulty.Easy, seed = 1L)
        val b = SudokuGenerator.generate(Difficulty.Easy, seed = 2L)
        assertFalse(a.cells == b.cells, "Different seeds should produce different puzzles")
    }

    @Test
    fun givenCellsMatchSolution() {
        val board = SudokuGenerator.generate(Difficulty.Medium, seed = 77L)
        for (r in 0..8) {
            for (c in 0..8) {
                val cell = board.cells[r][c]
                if (cell.given) {
                    assertEquals(board.solution[r][c], cell.value, "Given cell ($r,$c) doesn't match solution")
                }
            }
        }
    }
}
