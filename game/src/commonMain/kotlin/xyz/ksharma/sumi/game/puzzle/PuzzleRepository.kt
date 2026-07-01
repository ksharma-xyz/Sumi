package xyz.ksharma.sumi.game.puzzle

import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty

/**
 * Generates Sudoku puzzles. All three methods are `suspend` because puzzle
 * generation is CPU-bound — the backtracking solver can take 100ms+ on Easy
 * and several seconds on Edo. The implementation guarantees the work runs
 * on a background dispatcher (typically `Dispatchers.Default`); marking the
 * interface as `suspend` is the type-system-level guard that keeps callers
 * from accidentally invoking these on Main.
 */
interface PuzzleRepository {
    suspend fun daily(difficulty: Difficulty = Difficulty.Medium): BoardState
    suspend fun getOptions(difficulty: Difficulty, count: Int = 3): List<BoardState>
    suspend fun fromSeed(difficulty: Difficulty, seed: Long): BoardState

    /**
     * The deterministic seed for today's daily puzzle (one per UTC day). Cheap clock
     * math — no generation — so it is non-suspend. Persisting this concrete seed lets a
     * resumed daily game be rebuilt with [fromSeed] on any later day.
     */
    fun dailySeed(): Long
}
