package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SumiPreferences {
    suspend fun hasSeenOnboarding(): Boolean
    suspend fun setSeenOnboarding()

    fun themeMode(): Flow<ThemeMode>
    fun setThemeMode(mode: ThemeMode)
}

class InMemorySumiPreferences : SumiPreferences {
    private var seen = false
    private val _themeMode = MutableStateFlow(ThemeMode.FollowSystem)

    override suspend fun hasSeenOnboarding() = seen
    override suspend fun setSeenOnboarding() { seen = true }

    override fun themeMode(): Flow<ThemeMode> = _themeMode.asStateFlow()
    override fun setThemeMode(mode: ThemeMode) { _themeMode.value = mode }
}
