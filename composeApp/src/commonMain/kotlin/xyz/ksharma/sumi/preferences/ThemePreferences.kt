package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow
import xyz.ksharma.sumi.theme.SumiSeason

interface ThemePreferences {
    fun observeSeason(): Flow<SumiSeason>
    suspend fun setSeason(season: SumiSeason)
    fun observeHapticsEnabled(): Flow<Boolean>
    suspend fun setHapticsEnabled(enabled: Boolean)

    /**
     * Whether the running mistake count is shown on the Game screen.
     * Default true. Some players prefer to play without seeing the count
     * (less anxiety-inducing); they can toggle it off in Settings.
     */
    fun observeShowMistakes(): Flow<Boolean>
    suspend fun setShowMistakes(enabled: Boolean)
}
