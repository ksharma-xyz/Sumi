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
import xyz.ksharma.sumi.game.manager.BoardManager
import xyz.ksharma.sumi.game.manager.RealBoardManager
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.game.puzzle.PuzzleRepository

class GameViewModel(
    private val puzzleRepository: PuzzleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EMPTY_BOARD)
    val state: StateFlow<BoardState> = _state.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    // Increments each time a new row, column, box, or grid completion is detected.
    // The game screen observes this to trigger a petal burst per event.
    private val _celebrationCount = MutableStateFlow(0)
    val celebrationCount: StateFlow<Int> = _celebrationCount.asStateFlow()

    private var boardManager: BoardManager = RealBoardManager(EMPTY_BOARD)
    private var timerJob: Job? = null
    private var syncJob: Job? = null

    fun init(difficulty: Difficulty) {
        timerJob?.cancel()
        syncJob?.cancel()
        _elapsedMs.value = 0L
        _celebrationCount.value = 0
        boardManager = RealBoardManager(puzzleRepository.daily(difficulty))
        _state.value = boardManager.state.value
        syncJob = viewModelScope.launch {
            var prevCompleted = completionKey(_state.value)
            boardManager.state.collect { newState ->
                _state.value = newState
                val newCompleted = completionKey(newState)
                if ((newCompleted - prevCompleted).isNotEmpty()) {
                    _celebrationCount.update { it + 1 }
                }
                prevCompleted = newCompleted
            }
        }
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

    fun select(row: Int, col: Int) = boardManager.select(row, col)
    fun enter(digit: Int) = boardManager.enter(digit)
    fun erase() = boardManager.erase()
    fun undo() = boardManager.undo()
    fun hint() = boardManager.hint()
    fun toggleNotes() = boardManager.toggleNotes()

    // Returns a stable key for the set of currently completed units.
    // Comparing old vs new tells us if any new completions happened.
    private fun completionKey(state: BoardState): Set<String> = buildSet {
        state.completedRows.forEach { add("row$it") }
        state.completedCols.forEach { add("col$it") }
        state.completedBoxes.forEach { add("box$it") }
        if (state.isComplete) add("grid")
    }

    private companion object {
        const val TIMER_TICK_MS = 1000L
        val EMPTY_BOARD = BoardState(
            cells = List(9) { List(9) { xyz.ksharma.sumi.game.model.Cell(0, false) } },
            difficulty = Difficulty.Medium,
            solution = List(9) { List(9) { 0 } },
        )
    }
}
