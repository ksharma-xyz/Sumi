package xyz.ksharma.sumi.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import xyz.ksharma.sumi.theme.SumiSeason
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val KEY_SEEN_ONBOARDING = booleanPreferencesKey("seen_onboarding")
private val KEY_SEASON = stringPreferencesKey("season")
private val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
private val KEY_STREAK = intPreferencesKey("streak_count")
private val KEY_BEST_STREAK = intPreferencesKey("best_streak")
private val KEY_LAST_SOLVE_DAY = longPreferencesKey("last_solve_day")
private val KEY_SOLVE_DAYS = stringSetPreferencesKey("solve_days")
private val KEY_TOTAL_PUZZLES = intPreferencesKey("total_puzzles")
private val KEY_DEBUG_SIMULATE_PRO = booleanPreferencesKey("debug_simulate_pro")
private val KEY_DEBUG_ADS_ENABLED = booleanPreferencesKey("debug_ads_enabled")

class DataStoreSumiPreferences(
    private val store: DataStore<Preferences>,
) : SumiPreferences, DebugPreferences, ThemePreferences {

    override suspend fun hasSeenOnboarding(): Boolean =
        store.data.first()[KEY_SEEN_ONBOARDING] ?: false

    override suspend fun setSeenOnboarding() {
        store.edit { it[KEY_SEEN_ONBOARDING] = true }
    }

    override suspend fun getStreak(): Int = store.data.first()[KEY_STREAK] ?: 0

    override suspend fun getBestStreak(): Int = store.data.first()[KEY_BEST_STREAK] ?: 0

    override suspend fun getTotalPuzzlesSolved(): Int = store.data.first()[KEY_TOTAL_PUZZLES] ?: 0

    override suspend fun recordSolve(): Int {
        val today = todayEpochDay()
        var newStreak = 0
        store.edit { prefs ->
            val lastDay = prefs[KEY_LAST_SOLVE_DAY] ?: Long.MIN_VALUE
            val current = prefs[KEY_STREAK] ?: 0
            newStreak = when {
                lastDay == today -> current
                lastDay == today - 1 -> current + 1
                else -> 1
            }
            if (lastDay != today) {
                prefs[KEY_STREAK] = newStreak
                prefs[KEY_LAST_SOLVE_DAY] = today
                val prevBest = prefs[KEY_BEST_STREAK] ?: 0
                if (newStreak > prevBest) prefs[KEY_BEST_STREAK] = newStreak
            }
            val existing = prefs[KEY_SOLVE_DAYS] ?: emptySet()
            prefs[KEY_SOLVE_DAYS] = existing + today.toString()
            prefs[KEY_TOTAL_PUZZLES] = (prefs[KEY_TOTAL_PUZZLES] ?: 0) + 1
        }
        return newStreak
    }

    override suspend fun getSolveDays(): Set<Long> =
        store.data.first()[KEY_SOLVE_DAYS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()

    override suspend fun resetOnboarding() {
        store.edit { it[KEY_SEEN_ONBOARDING] = false }
    }

    override suspend fun clearStats() {
        store.edit { prefs ->
            prefs.remove(KEY_STREAK)
            prefs.remove(KEY_BEST_STREAK)
            prefs.remove(KEY_LAST_SOLVE_DAY)
            prefs.remove(KEY_SOLVE_DAYS)
            prefs.remove(KEY_TOTAL_PUZZLES)
        }
    }

    override suspend fun clearAll() {
        store.edit { it.clear() }
    }

    override fun observeSimulatePro(): Flow<Boolean> =
        store.data.map { it[KEY_DEBUG_SIMULATE_PRO] ?: false }

    override suspend fun setSimulatePro(enabled: Boolean) {
        store.edit { it[KEY_DEBUG_SIMULATE_PRO] = enabled }
    }

    override fun observeAdsEnabled(): Flow<Boolean> =
        store.data.map { it[KEY_DEBUG_ADS_ENABLED] ?: true }

    override suspend fun setAdsEnabled(enabled: Boolean) {
        store.edit { it[KEY_DEBUG_ADS_ENABLED] = enabled }
    }

    override fun observeSeason(): Flow<SumiSeason> = store.data.map { prefs ->
        prefs[KEY_SEASON]?.let { runCatching { SumiSeason.valueOf(it) }.getOrNull() }
            ?: SumiSeason.Spring
    }

    override suspend fun setSeason(season: SumiSeason) {
        store.edit { it[KEY_SEASON] = season.name }
    }

    override fun observeHapticsEnabled(): Flow<Boolean> =
        store.data.map { it[KEY_HAPTICS] ?: true }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        store.edit { it[KEY_HAPTICS] = enabled }
    }
}

@OptIn(ExperimentalTime::class)
private fun todayEpochDay(): Long {
    val now = Clock.System.now()
    val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
    return local.date.toEpochDays()
}
