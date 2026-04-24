@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game

import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.game.puzzle.RealPuzzleRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PuzzleRepositoryTest {

    private val repo = RealPuzzleRepository()

    // ── daily() ───────────────────────────────────────────────────────────────

    @Test
    fun dailyReturnsBoardForEachDifficulty() {
        Difficulty.entries.forEach { diff ->
            val board = repo.daily(diff)
            assertNotNull(board)
            assertEquals(diff, board.difficulty)
        }
    }

    @Test
    fun dailyIsDeterministicForSameDifficulty() {
        // Two calls on the same process share the same clock day, so seeds match.
        val a = repo.daily(Difficulty.Medium)
        val b = repo.daily(Difficulty.Medium)
        assertEquals(a.cells, b.cells, "daily() must return identical boards when called twice in the same day")
        assertEquals(a.solution, b.solution)
    }

    @Test
    fun dailyDiffersBetweenDifficulties() {
        val easy = repo.daily(Difficulty.Easy)
        val hard = repo.daily(Difficulty.Hard)
        // Same seed, different difficulty → different given-counts/layouts.
        assertFalse(
            easy.cells == hard.cells,
            "daily() for Easy and Hard must produce different boards",
        )
    }

    @Test
    fun dailyBoardHasValidSolution() {
        val board = repo.daily(Difficulty.Easy)
        val sol = board.solution
        // Every row 1–9
        for (r in 0..8) assertEquals((1..9).toSet(), sol[r].toSet(), "Row $r invalid")
        // Every col 1–9
        for (c in 0..8) assertEquals((1..9).toSet(), sol.map { it[c] }.toSet(), "Col $c invalid")
        // Every box 1–9
        for (br in 0..2) for (bc in 0..2) {
            val boxVals = buildSet { for (dr in 0..2) for (dc in 0..2) add(sol[br * 3 + dr][bc * 3 + dc]) }
            assertEquals((1..9).toSet(), boxVals, "Box ($br,$bc) invalid")
        }
    }

    @Test
    fun dailyBoardGivensMatchSolution() {
        val board = repo.daily(Difficulty.Hard)
        for (r in 0..8) for (c in 0..8) {
            val cell = board.cells[r][c]
            if (cell.given) assertEquals(board.solution[r][c], cell.value, "Given ($r,$c) mismatch")
        }
    }

    // ── fromSeed() ────────────────────────────────────────────────────────────

    @Test
    fun fromSeedIsDeterministic() {
        val a = repo.fromSeed(Difficulty.Master, 9999L)
        val b = repo.fromSeed(Difficulty.Master, 9999L)
        assertEquals(a.cells, b.cells)
        assertEquals(a.solution, b.solution)
    }

    @Test
    fun fromSeedDifferentSeedsProduceDifferentBoards() {
        val a = repo.fromSeed(Difficulty.Easy, 1L)
        val b = repo.fromSeed(Difficulty.Easy, 2L)
        assertFalse(a.cells == b.cells)
    }

    // ── getOptions() ──────────────────────────────────────────────────────────

    @Test
    fun getOptionsReturnsRequestedCount() {
        val options = repo.getOptions(Difficulty.Easy, count = 5)
        assertEquals(5, options.size)
    }

    @Test
    fun getOptionsAllHaveCorrectDifficulty() {
        val options = repo.getOptions(Difficulty.Medium, count = 3)
        options.forEach { assertEquals(Difficulty.Medium, it.difficulty) }
    }

    @Test
    fun getOptionsProducesDistinctBoards() {
        val options = repo.getOptions(Difficulty.Easy, count = 3)
        val boards = options.map { it.cells }
        val uniqueBoards = boards.toSet()
        assertEquals(boards.size, uniqueBoards.size, "getOptions should produce distinct boards")
    }
}
