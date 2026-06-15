package xyz.ksharma.sumi.game.manager

import kotlinx.coroutines.flow.StateFlow
import xyz.ksharma.sumi.game.model.BoardState

// Cohesive command surface for one board — each method maps to a single player action.
// Splitting it would scatter tightly-related operations across artificial interfaces.
@Suppress("TooManyFunctions")
interface BoardManager {
    val state: StateFlow<BoardState>

    fun select(row: Int, col: Int)
    fun enter(digit: Int)
    fun placeAt(row: Int, col: Int, digit: Int)
    fun erase()
    fun undo()
    fun redo()
    fun hint(): Boolean
    fun addHints(count: Int)
    fun toggleNotes()
    fun fillNotes()
    fun tick(deltaMs: Long)
}
