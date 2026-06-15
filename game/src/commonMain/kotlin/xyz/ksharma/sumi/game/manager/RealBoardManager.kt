package xyz.ksharma.sumi.game.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.ksharma.sumi.game.model.BoardState

class RealBoardManager(initialState: BoardState) : BoardManager {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<BoardState> = _state.asStateFlow()

    override fun select(row: Int, col: Int) {
        _state.value = _state.value.select(row, col)
    }

    override fun enter(digit: Int) {
        _state.value = _state.value.enter(digit)
    }

    override fun erase() {
        _state.value = _state.value.erase()
    }

    override fun undo() {
        _state.value = _state.value.undo()
    }

    override fun redo() {
        _state.value = _state.value.redo()
    }

    override fun hint(): Boolean {
        val before = _state.value.hintsRemaining
        _state.value = _state.value.hint()
        return _state.value.hintsRemaining < before
    }

    override fun addHints(count: Int) {
        _state.value = _state.value.addHints(count)
    }

    override fun toggleNotes() {
        _state.value = _state.value.toggleNotes()
    }

    override fun tick(deltaMs: Long) {
        _state.value = _state.value.tick(deltaMs)
    }
}
