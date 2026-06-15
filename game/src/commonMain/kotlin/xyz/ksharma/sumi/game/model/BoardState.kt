@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game.model

data class BoardState(
    val cells: List<List<Cell>>,
    val selected: Pair<Int, Int>? = null,
    val notesMode: Boolean = false,
    val hintsRemaining: Int = DEFAULT_HINTS,
    val mistakeCount: Int = 0,
    /** Number of digit-entry actions the player has taken in normal (not notes) mode. */
    val moveCount: Int = 0,
    val elapsedMs: Long = 0L,
    val difficulty: Difficulty,
    val solution: List<List<Int>>,
    private val history: List<List<List<Cell>>> = emptyList(),
    private val future: List<List<List<Cell>>> = emptyList(),
) {
    /** True when [undo] can step back, i.e. at least one move has been made. */
    val canUndo: Boolean get() = history.isNotEmpty()

    /** True when [redo] can replay a move that was undone. */
    val canRedo: Boolean get() = future.isNotEmpty()

    companion object {
        const val DEFAULT_HINTS = 3
        const val HINTS_UNLIMITED = Int.MAX_VALUE
    }

    // Cells where the user's entry doesn't match the solution — computed, never stale.
    val errorCells: Set<Pair<Int, Int>>
        get() = buildSet {
            for (r in 0..8) for (c in 0..8) {
                val v = cells[r][c].value
                if (v != 0 && v != solution[r][c]) add(r to c)
            }
        }

    // Which rows are fully and correctly filled (all 9 cells match solution).
    val completedRows: Set<Int>
        get() = (0..8).filterTo(mutableSetOf()) { r ->
            (0..8).all { c -> cells[r][c].value != 0 && cells[r][c].value == solution[r][c] }
        }

    val completedCols: Set<Int>
        get() = (0..8).filterTo(mutableSetOf()) { c ->
            (0..8).all { r -> cells[r][c].value != 0 && cells[r][c].value == solution[r][c] }
        }

    // Boxes indexed 0–8 in reading order (0 = top-left 3×3, 8 = bottom-right 3×3).
    val completedBoxes: Set<Int>
        get() = (0..8).filterTo(mutableSetOf()) { b ->
            val br = (b / 3) * 3
            val bc = (b % 3) * 3
            (0..2).all { dr ->
                (0..2).all { dc ->
                    val cell = cells[br + dr][bc + dc]
                    cell.value != 0 && cell.value == solution[br + dr][bc + dc]
                }
            }
        }

    // Digits 1–9 that have all 9 instances correctly placed.
    val completedDigits: Set<Int>
        get() = (1..9).filterTo(mutableSetOf()) { d ->
            cells.sumOf { row -> row.count { it.value == d } } == 9
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

    fun select(row: Int, col: Int): BoardState {
        val newSelected = if (selected == Pair(row, col)) null else Pair(row, col)
        return copy(selected = newSelected)
    }

    fun enter(digit: Int): BoardState {
        if (isComplete) return this
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
            // Placing a digit auto-clears that digit from the pencil marks of every
            // peer (same row, column, or 3x3 box) — the manual note cleanup every
            // serious Sudoku app does for you.
            val placedCells = updatedCells(row, col, cell.copy(value = digit, notes = emptySet()))
            val newCells = if (digit == 0) placedCells else stripNoteFromPeers(placedCells, row, col, digit)
            copy(
                cells = newCells,
                mistakeCount = if (incrementMistake) mistakeCount + 1 else mistakeCount,
                moveCount = moveCount + 1,
                history = history + listOf(cells),
                future = emptyList(), // a fresh move invalidates the redo stack
            )
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
        // Push the current grid onto the redo stack so it can be replayed.
        return copy(cells = history.last(), history = history.dropLast(1), future = future + listOf(cells))
    }

    fun redo(): BoardState {
        if (future.isEmpty()) return this
        return copy(cells = future.last(), future = future.dropLast(1), history = history + listOf(cells))
    }

    fun hint(): BoardState {
        if (hintsRemaining <= 0) return this
        val candidates = mutableListOf<Pair<Int, Int>>()
        for (r in 0..8) for (c in 0..8) {
            if (!cells[r][c].given && cells[r][c].value != solution[r][c]) candidates.add(r to c)
        }
        if (candidates.isEmpty()) return this
        val (r, c) = candidates.random()
        val newCells = updatedCells(r, c, cells[r][c].copy(value = solution[r][c], given = true, notes = emptySet()))
        val newHints = if (hintsRemaining == HINTS_UNLIMITED) HINTS_UNLIMITED else hintsRemaining - 1
        return copy(
            cells = newCells,
            hintsRemaining = newHints,
            history = history + listOf(cells),
            future = emptyList(),
        )
    }

    fun toggleNotes(): BoardState = copy(notesMode = !notesMode)

    /**
     * Adds [count] hints to the player's pool. Used by the rewarded-ad flow:
     * watch a video → earn +1 hint. No-op when hints are already unlimited (Pro).
     */
    fun addHints(count: Int): BoardState {
        if (hintsRemaining == HINTS_UNLIMITED || count <= 0) return this
        return copy(hintsRemaining = hintsRemaining + count)
    }

    // Timer stops automatically once the puzzle is solved.
    fun tick(deltaMs: Long): BoardState =
        if (isComplete) this else copy(elapsedMs = elapsedMs + deltaMs)

    private fun updateCell(row: Int, col: Int, cell: Cell): BoardState =
        copy(cells = updatedCells(row, col, cell), history = history + listOf(cells), future = emptyList())

    private fun updatedCells(row: Int, col: Int, cell: Cell): List<List<Cell>> =
        cells.mapIndexed { r, rowList ->
            if (r == row) rowList.mapIndexed { c, existing -> if (c == col) cell else existing }
            else rowList
        }

    // Removes [digit] from the notes of every cell that shares a unit (row, column,
    // or box) with (row, col), leaving the placed cell itself untouched.
    private fun stripNoteFromPeers(grid: List<List<Cell>>, row: Int, col: Int, digit: Int): List<List<Cell>> =
        grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, existing ->
                val isPeer = (r == row || c == col || (r / 3 == row / 3 && c / 3 == col / 3)) &&
                    !(r == row && c == col)
                if (isPeer && digit in existing.notes) existing.copy(notes = existing.notes - digit) else existing
            }
        }
}
