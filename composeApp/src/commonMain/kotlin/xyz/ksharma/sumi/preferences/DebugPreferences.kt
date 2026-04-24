package xyz.ksharma.sumi.preferences

interface DebugPreferences {
    suspend fun resetOnboarding()
    suspend fun clearStats()
    suspend fun clearAll()
}
