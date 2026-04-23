package xyz.ksharma.sumi.game.puzzle

import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty

interface PuzzleRepository {
    fun daily(difficulty: Difficulty = Difficulty.Medium): BoardState
    fun getOptions(difficulty: Difficulty, count: Int = 3): List<BoardState>
    fun fromSeed(difficulty: Difficulty, seed: Long): BoardState
}
