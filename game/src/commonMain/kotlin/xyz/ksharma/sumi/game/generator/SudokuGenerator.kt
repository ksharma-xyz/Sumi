@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game.generator

import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Cell
import xyz.ksharma.sumi.game.model.Difficulty
import kotlin.random.Random

object SudokuGenerator {

    fun generate(difficulty: Difficulty, seed: Long): BoardState {
        val rng = Random(seed)
        val solution = IntArray(81) { 0 }
        solve(solution, rng)
        val puzzle = solution.copyOf()
        removeCells(puzzle, difficulty, rng)
        val cells = List(9) { r ->
            List(9) { c ->
                val v = puzzle[r * 9 + c]
                Cell(value = v, given = v != 0)
            }
        }
        val solGrid = List(9) { r -> List(9) { c -> solution[r * 9 + c] } }
        return BoardState(cells = cells, difficulty = difficulty, solution = solGrid)
    }

    private fun solve(board: IntArray, rng: Random): Boolean {
        val pos = board.indexOfFirst { it == 0 }
        if (pos == -1) return true
        val row = pos / 9
        val col = pos % 9
        val digits = (1..9).toMutableList().also { it.shuffle(rng) }
        for (d in digits) {
            if (isValid(board, row, col, d)) {
                board[pos] = d
                if (solve(board, rng)) return true
                board[pos] = 0
            }
        }
        return false
    }

    // Counts solutions up to [limit] (we only ever need to know "exactly one").
    // Uses the MRV heuristic — branch on the empty cell with the FEWEST legal
    // candidates — which prunes the backtracking tree by orders of magnitude
    // versus always taking the first empty cell (Master generation: ~1s -> ~tens
    // of ms). This only affects search speed, never the count returned, so the
    // generated puzzles stay byte-identical per seed (saves remain reproducible).
    private fun countSolutions(board: IntArray, limit: Int): Int {
        // Find the most-constrained empty cell.
        var bestPos = -1
        var bestCount = 10
        for (i in 0..80) {
            if (board[i] != 0) continue
            var candidates = 0
            val row = i / 9
            val col = i % 9
            for (d in 1..9) if (isValid(board, row, col, d)) candidates++
            if (candidates < bestCount) {
                bestCount = candidates
                bestPos = i
                if (candidates <= 1) break // can't do better than 0/1 — stop scanning
            }
        }
        if (bestPos == -1) return 1 // no empty cells left — a complete solution
        if (bestCount == 0) return 0 // a cell with no legal digit — dead end

        val row = bestPos / 9
        val col = bestPos % 9
        var count = 0
        for (d in 1..9) {
            if (isValid(board, row, col, d)) {
                board[bestPos] = d
                count += countSolutions(board, limit)
                board[bestPos] = 0
                if (count >= limit) return count
            }
        }
        return count
    }

    private fun removeCells(puzzle: IntArray, difficulty: Difficulty, rng: Random) {
        val toRemove = 81 - difficulty.givenCount
        val positions = (0..80).toMutableList().also { it.shuffle(rng) }
        var removed = 0
        for (pos in positions) {
            if (removed >= toRemove) break
            val saved = puzzle[pos]
            puzzle[pos] = 0
            val test = puzzle.copyOf()
            if (countSolutions(test, 2) == 1) {
                removed++
            } else {
                puzzle[pos] = saved
            }
        }
    }

    private fun isValid(board: IntArray, row: Int, col: Int, digit: Int): Boolean {
        for (c in 0..8) if (board[row * 9 + c] == digit) return false
        for (r in 0..8) if (board[r * 9 + col] == digit) return false
        val br = (row / 3) * 3
        val bc = (col / 3) * 3
        for (r in br..(br + 2)) for (c in bc..(bc + 2)) if (board[r * 9 + c] == digit) return false
        return true
    }
}
