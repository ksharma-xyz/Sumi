@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game.puzzle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import xyz.ksharma.sumi.game.generator.SudokuGenerator
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RealPuzzleRepository : PuzzleRepository {

    // Generated boards are deterministic in their derived seed, so we cache them
    // in memory keyed by that seed. Re-opening / resuming the same puzzle (the
    // common navigate-away-then-back case) is then instant instead of paying the
    // backtracking generator + per-cell uniqueness checks again (~1s on Edo).
    // BoardState is immutable (callers .copy() before mutating), so the cached
    // instance is safe to share. Generation runs outside the lock so concurrent
    // misses don't serialise behind each other; the Mutex only guards the map.
    private val boardCache = LinkedHashMap<Long, BoardState>()
    private val cacheMutex = Mutex()

    // Each method wraps the synchronous generator in withContext(Default).
    // Defense in depth: even if a future caller invokes from Main (despite the
    // suspend signature), the heavy backtracking work is guaranteed off-main.

    override suspend fun daily(difficulty: Difficulty): BoardState =
        cachedBoard(difficulty, derive(dailySeed(), difficulty))

    override suspend fun getOptions(difficulty: Difficulty, count: Int): List<BoardState> =
        withContext(Dispatchers.Default) {
            // Fresh random seeds every call — these never repeat, so caching them
            // would only grow the map without ever producing a hit. Left uncached.
            val baseSeed = Clock.System.now().toEpochMilliseconds()
            List(count) { i ->
                SudokuGenerator.generate(difficulty, derive(baseSeed + i * SEED_SPACING, difficulty))
            }
        }

    override suspend fun fromSeed(difficulty: Difficulty, seed: Long): BoardState =
        cachedBoard(difficulty, derive(seed, difficulty))

    /**
     * Returns the cached board for [derivedSeed], generating + caching it on a miss.
     * The derived seed already encodes [difficulty] (see [derive]), so it alone is a
     * unique cache key; [difficulty] is still passed through because the generator
     * needs its given-count to know how many cells to remove.
     */
    private suspend fun cachedBoard(difficulty: Difficulty, derivedSeed: Long): BoardState {
        cacheMutex.withLock { boardCache[derivedSeed] }?.let { return it }
        val board = withContext(Dispatchers.Default) {
            SudokuGenerator.generate(difficulty, derivedSeed)
        }
        cacheMutex.withLock {
            boardCache[derivedSeed] = board
            // Bound the cache — a player rolling many "New Puzzle"s shouldn't grow it
            // without limit. Evict the oldest insertion (LinkedHashMap iteration order).
            if (boardCache.size > MAX_CACHED_BOARDS) {
                boardCache.remove(boardCache.keys.first())
            }
        }
        return board
    }

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
    override fun dailySeed(): Long {
        val days = Clock.System.now().toEpochMilliseconds() / MILLIS_PER_DAY
        return days.absoluteValue
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
        const val SEED_SPACING = 10_000L
        // One slot per difficulty's current puzzle, plus daily, plus headroom for a
        // few "New Puzzle" rolls before the oldest is evicted. Small — boards are light.
        const val MAX_CACHED_BOARDS = 16
        // Large odd offset (within Long range) — multiplied by (ordinal + 1) so
        // Easy / Medium / Hard / Master / Edo each map onto a well-spaced offset
        // in the generator's seed space.
        const val DIFFICULTY_SALT = 0x6E37_79B9_7F4A_7C15L
    }
}
