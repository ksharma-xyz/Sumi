@file:Suppress("MagicNumber") // 0..8 grid indices and 1..9 digits are intrinsic to Sudoku

package xyz.ksharma.sumi.game.model

/**
 * Pure Sudoku deduction helpers operating on the current (possibly partial) board.
 *
 * These reason about the *player's* grid, not the solution, so they power features
 * like auto-candidates and logical hints. Callers that place a value still use the
 * known [BoardState.solution] for the actual digit, so a deduction made on a board
 * that contains a wrong entry can never write an incorrect value.
 */
internal object SudokuLogic {

    /** Legal candidates for the empty cell (r, c): 1..9 minus digits already in its row, column, or box. */
    fun candidates(cells: List<List<Cell>>, r: Int, c: Int): Set<Int> {
        if (cells[r][c].value != 0) return emptySet()
        val used = buildSet {
            for (i in 0..8) {
                add(cells[r][i].value)
                add(cells[i][c].value)
            }
            val br = (r / 3) * 3
            val bc = (c / 3) * 3
            for (dr in 0..2) for (dc in 0..2) add(cells[br + dr][bc + dc].value)
        }
        return (1..9).filterTo(mutableSetOf()) { it !in used }
    }

    /**
     * The next cell solvable by simple logic — a naked single (only one candidate fits the
     * cell) or a hidden single (a digit fits only one cell in some unit) — or null if neither
     * exists. Used to give a hint that points at a genuinely deducible cell instead of a random one.
     */
    fun nextLogicalCell(state: BoardState): Pair<Int, Int>? {
        val cells = state.cells
        nakedSingle(cells)?.let { return it }
        return hiddenSingle(cells)
    }

    private fun nakedSingle(cells: List<List<Cell>>): Pair<Int, Int>? {
        for (r in 0..8) for (c in 0..8) {
            if (cells[r][c].value == 0 && candidates(cells, r, c).size == 1) return r to c
        }
        return null
    }

    private fun hiddenSingle(cells: List<List<Cell>>): Pair<Int, Int>? {
        for (unit in allUnits()) {
            for (digit in 1..9) {
                val fits = unit.filter { (r, c) -> cells[r][c].value == 0 && digit in candidates(cells, r, c) }
                if (fits.size == 1) return fits.first()
            }
        }
        return null
    }

    // The 27 units of a Sudoku board: 9 rows, 9 columns, 9 boxes.
    private fun allUnits(): List<List<Pair<Int, Int>>> = buildList {
        for (r in 0..8) add((0..8).map { c -> r to c })
        for (c in 0..8) add((0..8).map { r -> r to c })
        for (b in 0..8) {
            val br = (b / 3) * 3
            val bc = (b % 3) * 3
            add((0..2).flatMap { dr -> (0..2).map { dc -> (br + dr) to (bc + dc) } })
        }
    }
}
