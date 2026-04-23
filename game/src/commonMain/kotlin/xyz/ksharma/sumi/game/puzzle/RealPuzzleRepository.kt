@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game.puzzle

import xyz.ksharma.sumi.game.generator.SudokuGenerator
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty
import kotlin.math.absoluteValue

class RealPuzzleRepository : PuzzleRepository {

    override fun daily(difficulty: Difficulty): BoardState {
        val seed = dailySeed()
        return SudokuGenerator.generate(difficulty, seed)
    }

    override fun getOptions(difficulty: Difficulty, count: Int): List<BoardState> {
        val baseSeed = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        return List(count) { i ->
            SudokuGenerator.generate(difficulty, baseSeed + i * SEED_SPACING)
        }
    }

    override fun fromSeed(difficulty: Difficulty, seed: Long): BoardState =
        SudokuGenerator.generate(difficulty, seed)

    private fun dailySeed(): Long {
        val now = kotlinx.datetime.Clock.System.now()
        val days = now.toEpochMilliseconds() / MILLIS_PER_DAY
        return days.absoluteValue
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
        const val SEED_SPACING = 10_000L
    }
}
