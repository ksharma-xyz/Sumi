package xyz.ksharma.sumi.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.game.manager.BoardManager
import xyz.ksharma.sumi.game.manager.RealBoardManager
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.game.puzzle.PuzzleRepository

class GameViewModel(
    private val puzzleRepository: PuzzleRepository,
) : ViewModel() {

    private lateinit var boardManager: BoardManager

    val state: StateFlow<BoardState>
        get() = boardManager.state

    fun init(difficulty: Difficulty) {
        val initialState = puzzleRepository.daily(difficulty)
        boardManager = RealBoardManager(initialState)
        startTimer()
    }

    fun select(row: Int, col: Int) = boardManager.select(row, col)
    fun enter(digit: Int) = boardManager.enter(digit)
    fun erase() = boardManager.erase()
    fun undo() = boardManager.undo()
    fun hint() = boardManager.hint()
    fun toggleNotes() = boardManager.toggleNotes()

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(TIMER_TICK_MS)
                boardManager.tick(TIMER_TICK_MS)
            }
        }
    }

    private companion object {
        const val TIMER_TICK_MS = 1000L
    }
}
