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
}
