package xyz.ksharma.sumi.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import xyz.ksharma.sumi.game.manager.BoardManager
import xyz.ksharma.sumi.game.manager.RealBoardManager
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Cell
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.game.puzzle.PuzzleRepository
import xyz.ksharma.sumi.preferences.GameSave
import xyz.ksharma.sumi.preferences.GameSaveRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class GameViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val saveRepository: GameSaveRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EMPTY_BOARD)
    val state: StateFlow<BoardState> = _state.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private val _celebrationCount = MutableStateFlow(0)
    val celebrationCount: StateFlow<Int> = _celebrationCount.asStateFlow()

    private var boardManager: BoardManager = RealBoardManager(EMPTY_BOARD)
    private var currentDifficulty: Difficulty = Difficulty.Medium
    private var timerJob: Job? = null
    private var syncJob: Job? = null

    fun init(difficulty: Difficulty) {
        timerJob?.cancel()
        syncJob?.cancel()
        currentDifficulty = difficulty
        _elapsedMs.value = 0L
        _celebrationCount.value = 0

        viewModelScope.launch {
            val freshPuzzle = puzzleRepository.daily(difficulty)
            val save = saveRepository.loadSave(difficulty)
            val restoredState = if (save != null) restoreState(freshPuzzle, save) else freshPuzzle
            val restoredElapsed = save?.elapsedMs ?: 0L

            boardManager = RealBoardManager(restoredState)
            val resolved = boardManager.state.value
            if (resolved.isComplete || resolved.isGameOver) {
                // Stale save from a completed game (process was killed before clear ran) — start fresh.
                saveRepository.clearSave(difficulty)
                boardManager = RealBoardManager(freshPuzzle)
                _elapsedMs.value = 0L
            } else {
                _elapsedMs.value = restoredElapsed
            }

            _state.value = boardManager.state.value
            startSync()
            startTimer()
        }
    }

    fun clearSave() {
        viewModelScope.launch { saveRepository.clearSave(currentDifficulty) }
    }

    fun select(row: Int, col: Int) = boardManager.select(row, col)
    fun enter(digit: Int) = boardManager.enter(digit)
    fun erase() = boardManager.erase()
    fun undo() = boardManager.undo()
    fun hint() = boardManager.hint()
    fun toggleNotes() = boardManager.toggleNotes()

    private fun startSync() {
        syncJob = viewModelScope.launch {
            var prevCompleted = completionKey(_state.value)
            boardManager.state.collect { newState ->
                _state.value = newState
                val newCompleted = completionKey(newState)
                if ((newCompleted - prevCompleted).isNotEmpty()) {
                    _celebrationCount.update { it + 1 }
                }
                prevCompleted = newCompleted

                when {
                    newState.isComplete || newState.isGameOver ->
                        // Game has ended — clear the save slot so there's nothing stale to resume.
                        launch { saveRepository.clearSave(currentDifficulty) }
                    hasAnyUserMoves(newState) ->
                        // Auto-save after every user move so the game survives process death.
                        launch { saveRepository.writeSave(currentDifficulty, buildSave(newState)) }
                }
            }
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(TIMER_TICK_MS)
                val current = _state.value
                if (!current.isGameOver && !current.isComplete) {
                    _elapsedMs.value += TIMER_TICK_MS
                }
            }
        }
    }

    private fun buildSave(state: BoardState): GameSave {
        val cells = state.cells
            .flatMap { row -> row.map { cell -> if (cell.given) 0 else cell.value } }
            .joinToString(",")
        return GameSave(
            epochDay = todayEpochDay(),
            cells = cells,
            elapsedMs = _elapsedMs.value,
            mistakeCount = state.mistakeCount,
            hintsRemaining = state.hintsRemaining,
        )
    }

    private fun hasAnyUserMoves(state: BoardState): Boolean =
        state.cells.any { row -> row.any { cell -> !cell.given && cell.value != 0 } }

    // Reconstructs a BoardState from a fresh puzzle and a persisted save.
    // Given cells are always taken from the fresh puzzle (same seed = same puzzle).
    // Non-given cells use the saved user value (0 = empty).
    private fun restoreState(fresh: BoardState, save: GameSave): BoardState {
        val savedValues = save.cells.split(",").map { it.trim().toIntOrNull() ?: 0 }
        val restoredCells = fresh.cells.mapIndexed { r, row ->
            row.mapIndexed { c, cell ->
                if (cell.given) cell
                else cell.copy(value = savedValues.getOrElse(r * BOARD_SIZE + c) { 0 })
            }
        }
        return fresh.copy(
            cells = restoredCells,
            mistakeCount = save.mistakeCount,
            hintsRemaining = save.hintsRemaining,
            selected = null,
            notesMode = false,
        )
    }

    private fun completionKey(state: BoardState): Set<String> = buildSet {
        state.completedRows.forEach { add("row$it") }
        state.completedCols.forEach { add("col$it") }
        state.completedBoxes.forEach { add("box$it") }
        if (state.isComplete) add("grid")
    }

    @OptIn(ExperimentalTime::class)
    private fun todayEpochDay(): Long {
        val now = Clock.System.now()
        val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return local.date.toEpochDays()
    }

    private companion object {
        const val TIMER_TICK_MS = 1000L
        const val BOARD_SIZE = 9
        val EMPTY_BOARD = BoardState(
            cells = List(9) { List(9) { Cell(0, false) } },
            difficulty = Difficulty.Medium,
            solution = List(9) { List(9) { 0 } },
        )
    }
}
