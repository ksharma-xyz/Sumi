package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow
import xyz.ksharma.sumi.design.board.BoardEntrance

@Suppress("TooManyFunctions")
interface DebugPreferences {
    suspend fun resetOnboarding()
    suspend fun clearStats()
    suspend fun clearAll()

    fun observeSimulatePro(): Flow<Boolean>
    suspend fun setSimulatePro(enabled: Boolean)

    fun observeAdsEnabled(): Flow<Boolean>
    suspend fun setAdsEnabled(enabled: Boolean)

    /** Debug-only: which board entrance animation to play (see [BoardEntrance]). */
    fun observeBoardEntrance(): Flow<BoardEntrance>
    suspend fun setBoardEntrance(entrance: BoardEntrance)

    /**
     * Pre-fills streak / total / last-N-days as if the player had been solving for
     * that many consecutive days. Lets us QA Stats + Win + Daily without grinding
     * through real puzzles.
     */
    suspend fun seedSolveData(streakDays: Int, totalPuzzles: Int)
}
