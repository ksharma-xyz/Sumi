package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow
import xyz.ksharma.sumi.theme.SumiSeason

interface ThemePreferences {
    fun observeSeason(): Flow<SumiSeason>
    suspend fun setSeason(season: SumiSeason)
    fun observeHapticsEnabled(): Flow<Boolean>
    suspend fun setHapticsEnabled(enabled: Boolean)
}
