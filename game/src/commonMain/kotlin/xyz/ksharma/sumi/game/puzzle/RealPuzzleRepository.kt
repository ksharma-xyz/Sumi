@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game.puzzle

import xyz.ksharma.sumi.game.generator.SudokuGenerator
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RealPuzzleRepository : PuzzleRepository {

    override fun daily(difficulty: Difficulty): BoardState =
        SudokuGenerator.generate(difficulty, derive(dailySeed(), difficulty))

    override fun getOptions(difficulty: Difficulty, count: Int): List<BoardState> {
        val baseSeed = Clock.System.now().toEpochMilliseconds()
        return List(count) { i ->
            SudokuGenerator.generate(difficulty, derive(baseSeed + i * SEED_SPACING, difficulty))
        }
    }

    override fun fromSeed(difficulty: Difficulty, seed: Long): BoardState =
        SudokuGenerator.generate(difficulty, derive(seed, difficulty))

    /**
     * Mixes [difficulty] into [seed] so the same caller-facing seed yields a
     * different puzzle per difficulty. Without this, every difficulty shares
     * one solved grid and Easy's clues are a superset of Hard's clues — a user
     * can read the solution off Easy and apply it to Hard. The XOR with a
     * per-difficulty constant breaks that correlation while keeping the
     * repository's seed-in / puzzle-out contract reproducible.
     */
    private fun derive(seed: Long, difficulty: Difficulty): Long =
        seed xor (DIFFICULTY_SALT * (difficulty.ordinal + 1).toLong())

    /** Days-since-epoch — the seed flips once per UTC day so each day has its own daily puzzle. */
    private fun dailySeed(): Long {
        val days = Clock.System.now().toEpochMilliseconds() / MILLIS_PER_DAY
        return days.absoluteValue
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
        const val SEED_SPACING = 10_000L
        // Large odd offset (within Long range) — multiplied by (ordinal + 1) so
        // Easy / Medium / Hard / Master / Edo each map onto a well-spaced offset
        // in the generator's seed space.
        const val DIFFICULTY_SALT = 0x6E37_79B9_7F4A_7C15L
    }
}
