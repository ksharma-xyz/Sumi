// App-wide prefs surface — splitting hurts cohesion.
@file:Suppress("TooManyFunctions")

package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow

interface SumiPreferences {
    suspend fun hasSeenOnboarding(): Boolean
    suspend fun setSeenOnboarding()

    // ── One-shot reads (used inside recordSolve, etc.) ────────────────────────
    suspend fun getStreak(): Int
    suspend fun getBestStreak(): Int
    suspend fun getTotalPuzzlesSolved(): Int
    suspend fun getSolveDays(): Set<Long>

    // ── Flow-based observers ───────────────────────────────────────────────────
    // Use these from ViewModels so screens reactively update after recordSolve()
    // — DataStore emits on every edit, eliminating the "Stats screen shows 0
    // because the VM cached the value at init" bug.
    fun observeStreak(): Flow<Int>
    fun observeBestStreak(): Flow<Int>
    fun observeTotalPuzzlesSolved(): Flow<Int>
    fun observeSolveDays(): Flow<Set<Long>>

    // ── Mutations ──────────────────────────────────────────────────────────────
    /**
     * Records today as a solve day for the given difficulty + elapsed time. Returns the
     * updated streak. Also updates per-difficulty best time + appends to the rolling
     * recent-solve-times list used by the Stats Improvement chart.
     */
    suspend fun recordSolve(difficulty: String, elapsedMs: Long): Int

    /**
     * Debug-only: seed streak / total / last-N-days as if the player had been
     * solving for that many days. Used by the Settings → Debug panel to test
     * Win and Stats screens without grinding through real puzzles.
     */
    suspend fun seedSolveData(streakDays: Int, totalPuzzles: Int)

    // ── Per-difficulty best times (for Stats → Personal Bests) ────────────────
    /** Map of difficulty name → best (lowest) elapsed time in ms. Empty if not played. */
    fun observeBestTimes(): Flow<Map<String, Long>>

    // ── Recent solve times (for Stats → Improvement line chart) ───────────────
    /**
     * Rolling list of the last N (~30) solve elapsed-times in milliseconds, oldest first.
     * Each call to [recordSolve] appends to this list (capped).
     */
    fun observeRecentSolveTimes(): Flow<List<Long>>

    // ── Last-played-difficulty (for Daily → Today card) ───────────────────────
    /** Difficulty name of the player's most recent solve, or null if they've never solved. */
    fun observeLastPlayedDifficulty(): Flow<String?>
}
