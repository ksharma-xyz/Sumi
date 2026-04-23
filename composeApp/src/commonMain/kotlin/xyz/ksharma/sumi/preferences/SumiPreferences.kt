package xyz.ksharma.sumi.preferences

interface SumiPreferences {
    suspend fun hasSeenOnboarding(): Boolean
    suspend fun setSeenOnboarding()
}

class InMemorySumiPreferences : SumiPreferences {
    private var seen = false
    override suspend fun hasSeenOnboarding() = seen
    override suspend fun setSeenOnboarding() { seen = true }
}
