package xyz.ksharma.sumi.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val KEY_SEEN_ONBOARDING = booleanPreferencesKey("seen_onboarding")
private val KEY_STREAK = intPreferencesKey("streak_count")
private val KEY_LAST_SOLVE_DAY = longPreferencesKey("last_solve_day")
private val KEY_SOLVE_DAYS = stringSetPreferencesKey("solve_days")

class DataStoreSumiPreferences(private val store: DataStore<Preferences>) : SumiPreferences {

    override suspend fun hasSeenOnboarding(): Boolean =
        store.data.first()[KEY_SEEN_ONBOARDING] ?: false

    override suspend fun setSeenOnboarding() {
        store.edit { it[KEY_SEEN_ONBOARDING] = true }
    }

    override suspend fun getStreak(): Int = store.data.first()[KEY_STREAK] ?: 0

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
            }
            // Record this day in the solve-days set (store as string for DataStore compatibility)
            val existing = prefs[KEY_SOLVE_DAYS] ?: emptySet()
            prefs[KEY_SOLVE_DAYS] = existing + today.toString()
        }
        return newStreak
    }

    override suspend fun getSolveDays(): Set<Long> =
        store.data.first()[KEY_SOLVE_DAYS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
}

@OptIn(ExperimentalTime::class)
private fun todayEpochDay(): Long {
    val now = Clock.System.now()
    val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
    return local.date.toEpochDays()
}
