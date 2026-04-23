package xyz.ksharma.sumi.preferences

interface SumiPreferences {
    suspend fun hasSeenOnboarding(): Boolean
    suspend fun setSeenOnboarding()
}
