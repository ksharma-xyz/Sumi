@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game.model

private const val MAX_MISTAKES = 3

data class BoardState(
    val cells: List<List<Cell>>,
    val selected: Pair<Int, Int>? = null,
    val notesMode: Boolean = false,
    val hintsRemaining: Int = 3,
    val mistakeCount: Int = 0,
    val elapsedMs: Long = 0L,
    val difficulty: Difficulty,
    val solution: List<List<Int>>,
    private val history: List<List<List<Cell>>> = emptyList(),
) {
    // Cells where the user's entry doesn't match the solution — computed, never stale.
    val errorCells: Set<Pair<Int, Int>>
        get() = buildSet {
            for (r in 0..8) for (c in 0..8) {
                val v = cells[r][c].value
                if (v != 0 && v != solution[r][c]) add(r to c)
            }
        }

    val counts: IntArray
        get() {
            val arr = IntArray(9)
            cells.forEach { row -> row.forEach { cell -> if (cell.value in 1..9) arr[cell.value - 1]++ } }
            return arr
        }

    val remainingCounts: IntArray
        get() = IntArray(9) { i -> 9 - counts[i] }

    // Complete when every cell matches the solution exactly.
    val isComplete: Boolean
        get() = cells.indices.all { r -> cells[r].indices.all { c -> cells[r][c].value == solution[r][c] } }

    val isGameOver: Boolean
        get() = mistakeCount >= MAX_MISTAKES

    fun select(row: Int, col: Int): BoardState {
        val newSelected = if (selected == Pair(row, col)) null else Pair(row, col)
        return copy(selected = newSelected)
    }

    fun enter(digit: Int): BoardState {
        if (isGameOver || isComplete) return this
        val (row, col) = selected ?: return this
        val cell = cells[row][col]
        if (cell.given) return this
        return if (notesMode) {
            val newNotes = if (digit in cell.notes) cell.notes - digit else cell.notes + digit
            updateCell(row, col, cell.copy(notes = newNotes, value = 0))
        } else {
            // Count a mistake only on first entry of a wrong digit for this cell.
            val wasEmpty = cell.value == 0
            val wasCorrect = cell.value == solution[row][col]
            val isWrong = digit != 0 && digit != solution[row][col]
            val incrementMistake = (wasEmpty || wasCorrect) && isWrong
            val newCells = updatedCells(row, col, cell.copy(value = digit, notes = emptySet()))
            copy(
                cells = newCells,
                mistakeCount = if (incrementMistake) mistakeCount + 1 else mistakeCount,
                history = history + listOf(cells),
            )
        }
    }

    fun erase(): BoardState {
        if (isGameOver) return this
        val (row, col) = selected ?: return this
        val cell = cells[row][col]
        if (cell.given) return this
        return updateCell(row, col, cell.copy(value = 0, notes = emptySet()))
    }

    fun undo(): BoardState {
        if (history.isEmpty()) return this
        return copy(cells = history.last(), history = history.dropLast(1))
    }

    fun hint(): BoardState {
        if (isGameOver || hintsRemaining <= 0) return this
        val candidates = mutableListOf<Pair<Int, Int>>()
        for (r in 0..8) for (c in 0..8) {
            if (!cells[r][c].given && cells[r][c].value != solution[r][c]) candidates.add(r to c)
        }
        if (candidates.isEmpty()) return this
        val (r, c) = candidates.random()
        val newCells = updatedCells(r, c, cells[r][c].copy(value = solution[r][c], given = true, notes = emptySet()))
        return copy(cells = newCells, hintsRemaining = hintsRemaining - 1, history = history + listOf(cells))
    }

    fun toggleNotes(): BoardState = copy(notesMode = !notesMode)

    // Timer stops automatically once the game ends.
    fun tick(deltaMs: Long): BoardState =
        if (isGameOver || isComplete) this else copy(elapsedMs = elapsedMs + deltaMs)

    private fun updateCell(row: Int, col: Int, cell: Cell): BoardState =
        copy(cells = updatedCells(row, col, cell), history = history + listOf(cells))

    private fun updatedCells(row: Int, col: Int, cell: Cell): List<List<Cell>> =
        cells.mapIndexed { r, rowList ->
            if (r == row) rowList.mapIndexed { c, existing -> if (c == col) cell else existing }
            else rowList
        }
}
