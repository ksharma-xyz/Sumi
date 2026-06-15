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

    /**
     * Whether board digits use a high-legibility sans typeface instead of the
     * Cormorant serif numerals. Default false. Helps players who find the serif
     * 1/4/7 ambiguous, and improves readability at a glance.
     */
    fun observeHighLegibility(): Flow<Boolean>
    suspend fun setHighLegibility(enabled: Boolean)
}
