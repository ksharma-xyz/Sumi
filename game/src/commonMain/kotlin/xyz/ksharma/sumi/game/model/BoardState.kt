@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game.model

data class BoardState(
    val cells: List<List<Cell>>,
    val selected: Pair<Int, Int>? = null,
    val conflict: Pair<Int, Int>? = null,
    val notesMode: Boolean = false,
    val hintsRemaining: Int = 3,
    val elapsedMs: Long = 0L,
    val difficulty: Difficulty,
    val solution: List<List<Int>>,
    private val history: List<List<List<Cell>>> = emptyList(),
) {
    val counts: IntArray
        get() {
            val arr = IntArray(9)
            cells.forEach { row -> row.forEach { cell -> if (cell.value in 1..9) arr[cell.value - 1]++ } }
            return arr
        }

    val remainingCounts: IntArray
        get() = IntArray(9) { i -> 9 - counts[i] }

    val isComplete: Boolean
        get() = cells.all { row -> row.all { cell -> cell.value != 0 } } && !hasConflicts

    val hasConflicts: Boolean
        get() {
            for (r in 0..8) {
                val rowVals = cells[r].filter { !it.isEmpty }.map { it.value }
                if (rowVals.size != rowVals.toSet().size) return true
            }
            for (c in 0..8) {
                val colVals = cells.map { it[c] }.filter { !it.isEmpty }.map { it.value }
                if (colVals.size != colVals.toSet().size) return true
            }
            for (br in 0..2) {
                for (bc in 0..2) {
                    val boxVals = mutableListOf<Int>()
                    for (r in (br * 3)..(br * 3 + 2)) {
                        for (c in (bc * 3)..(bc * 3 + 2)) {
                            if (!cells[r][c].isEmpty) boxVals.add(cells[r][c].value)
                        }
                    }
                    if (boxVals.size != boxVals.toSet().size) return true
                }
            }
            return false
        }

    fun select(row: Int, col: Int): BoardState {
        val newSelected = if (selected == Pair(row, col)) null else Pair(row, col)
        return copy(selected = newSelected)
    }

    fun enter(digit: Int): BoardState {
        val (row, col) = selected ?: return this
        val cell = cells[row][col]
        if (cell.given) return this
        return if (notesMode) {
            val newNotes = if (digit in cell.notes) cell.notes - digit else cell.notes + digit
            updateCell(row, col, cell.copy(notes = newNotes, value = 0))
        } else {
            val conflict = checkConflict(row, col, digit)
            val newCells = updatedCells(row, col, cell.copy(value = digit, notes = emptySet()))
            copy(cells = newCells, conflict = if (conflict) Pair(row, col) else null, history = history + listOf(cells))
        }
    }

    fun erase(): BoardState {
        val (row, col) = selected ?: return this
        val cell = cells[row][col]
        if (cell.given) return this
        return updateCell(row, col, cell.copy(value = 0, notes = emptySet()))
    }

    fun undo(): BoardState {
        if (history.isEmpty()) return this
        val previous = history.last()
        return copy(cells = previous, history = history.dropLast(1), conflict = null)
    }

    fun hint(): BoardState {
        if (hintsRemaining <= 0) return this
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0..8) for (c in 0..8) if (cells[r][c].isEmpty || cells[r][c].value != solution[r][c]) emptyCells.add(r to c)
        if (emptyCells.isEmpty()) return this
        val (r, c) = emptyCells.random()
        val newCells = updatedCells(r, c, cells[r][c].copy(value = solution[r][c], given = true, notes = emptySet()))
        return copy(cells = newCells, hintsRemaining = hintsRemaining - 1, history = history + listOf(cells))
    }

    fun toggleNotes(): BoardState = copy(notesMode = !notesMode)

    fun tick(deltaMs: Long): BoardState = copy(elapsedMs = elapsedMs + deltaMs)

    private fun checkConflict(row: Int, col: Int, digit: Int): Boolean {
        if (digit == 0) return false
        for (c in 0..8) if (c != col && cells[row][c].value == digit) return true
        for (r in 0..8) if (r != row && cells[r][col].value == digit) return true
        val br = (row / 3) * 3
        val bc = (col / 3) * 3
        for (r in br..(br + 2)) for (c in bc..(bc + 2)) if ((r != row || c != col) && cells[r][c].value == digit) return true
        return false
    }

    private fun updateCell(row: Int, col: Int, cell: Cell): BoardState =
        copy(cells = updatedCells(row, col, cell), history = history + listOf(cells))

    private fun updatedCells(row: Int, col: Int, cell: Cell): List<List<Cell>> =
        cells.mapIndexed { r, rowList ->
            if (r == row) rowList.mapIndexed { c, existing -> if (c == col) cell else existing }
            else rowList
        }
}
