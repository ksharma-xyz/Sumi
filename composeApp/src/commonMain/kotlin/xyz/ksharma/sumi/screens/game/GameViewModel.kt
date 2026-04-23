package xyz.ksharma.sumi.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private var boardManager: BoardManager = RealBoardManager(EMPTY_BOARD)
    private var timerJob: Job? = null
    private var syncJob: Job? = null

    fun init(difficulty: Difficulty) {
        timerJob?.cancel()
        syncJob?.cancel()
        _elapsedMs.value = 0L
        boardManager = RealBoardManager(puzzleRepository.daily(difficulty))
        _state.value = boardManager.state.value
        syncJob = viewModelScope.launch {
            boardManager.state.collect { _state.value = it }
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

    private companion object {
        const val TIMER_TICK_MS = 1000L
        val EMPTY_BOARD = BoardState(
            cells = List(9) { List(9) { xyz.ksharma.sumi.game.model.Cell(0, false) } },
            difficulty = Difficulty.Medium,
            solution = List(9) { List(9) { 0 } },
        )
    }
}
