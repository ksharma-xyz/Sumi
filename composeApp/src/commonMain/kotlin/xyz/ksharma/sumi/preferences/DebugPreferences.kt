package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow

interface DebugPreferences {
    suspend fun resetOnboarding()
    suspend fun clearStats()
    suspend fun clearAll()

    fun observeSimulatePro(): Flow<Boolean>
    suspend fun setSimulatePro(enabled: Boolean)

    fun observeAdsEnabled(): Flow<Boolean>
    suspend fun setAdsEnabled(enabled: Boolean)

    /**
     * Pre-fills streak / total / last-N-days as if the player had been solving for
     * that many consecutive days. Lets us QA Stats + Win + Daily without grinding
     * through real puzzles.
     */
    suspend fun seedSolveData(streakDays: Int, totalPuzzles: Int)
}
