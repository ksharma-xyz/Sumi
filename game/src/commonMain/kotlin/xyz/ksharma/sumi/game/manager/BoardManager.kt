package xyz.ksharma.sumi.game.manager

import kotlinx.coroutines.flow.StateFlow
import xyz.ksharma.sumi.game.model.BoardState

interface BoardManager {
    val state: StateFlow<BoardState>

    fun select(row: Int, col: Int)
    fun enter(digit: Int)
    fun erase()
    fun undo()
    fun hint(): Boolean
    fun toggleNotes()
    fun tick(deltaMs: Long)
}
