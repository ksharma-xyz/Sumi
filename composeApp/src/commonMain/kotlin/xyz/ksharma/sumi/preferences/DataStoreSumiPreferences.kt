// Implements three prefs interfaces in one DataStore — splitting fragments storage.
@file:Suppress("TooManyFunctions")

package xyz.ksharma.sumi.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
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
private val KEY_SHOW_MISTAKES = booleanPreferencesKey("show_mistakes")
private val KEY_HIGH_LEGIBILITY = booleanPreferencesKey("high_legibility")
private val KEY_STRICT_CONFLICTS = booleanPreferencesKey("strict_conflicts")
private val KEY_STREAK = intPreferencesKey("streak_count")
private val KEY_BEST_STREAK = intPreferencesKey("best_streak")
private val KEY_LAST_SOLVE_DAY = longPreferencesKey("last_solve_day")
private val KEY_SOLVE_DAYS = stringSetPreferencesKey("solve_days")
private val KEY_TOTAL_PUZZLES = intPreferencesKey("total_puzzles")
private val KEY_LAST_DIFFICULTY = stringPreferencesKey("last_difficulty")
// Per-difficulty best times: stored as "<diff>:<ms>" entries in a string set.
private val KEY_BEST_TIMES = stringSetPreferencesKey("best_times")
// Rolling list of recent elapsed times for the Improvement chart, oldest→newest, "ms,ms,ms"
private val KEY_RECENT_TIMES = stringPreferencesKey("recent_times")
// Signature of the last recorded solve ("<diff>:<elapsedMs>") — dedupe guard so
// one solved puzzle can't be counted twice if recordSolve is reached again.
private val KEY_LAST_SOLVE_SIG = stringPreferencesKey("last_solve_sig")
// Per-day solve counts: "<epochDay>:<count>" entries. Drives Stats' "this week
// solved" (a puzzle count, not a distinct-day count). Pruned to recent days.
private val KEY_DAILY_COUNTS = stringSetPreferencesKey("daily_solve_counts")
private const val DAILY_COUNTS_RETENTION_DAYS = 60
private const val WEEK_WINDOW_DAYS = 7L
private const val RECENT_TIMES_CAP = 30
private val KEY_DEBUG_SIMULATE_PRO = booleanPreferencesKey("debug_simulate_pro")
private val KEY_DEBUG_ADS_ENABLED = booleanPreferencesKey("debug_ads_enabled")
private val KEY_IS_PRO = booleanPreferencesKey("is_pro")

class DataStoreSumiPreferences(
    private val store: DataStore<Preferences>,
) : SumiPreferences, DebugPreferences, ThemePreferences, ProPreferences {

    override suspend fun hasSeenOnboarding(): Boolean =
        store.data.first()[KEY_SEEN_ONBOARDING] ?: false

    override suspend fun setSeenOnboarding() {
        store.edit { it[KEY_SEEN_ONBOARDING] = true }
    }

    override suspend fun getStreak(): Int = store.data.first()[KEY_STREAK] ?: 0

    override suspend fun getBestStreak(): Int = store.data.first()[KEY_BEST_STREAK] ?: 0

    override suspend fun getTotalPuzzlesSolved(): Int = store.data.first()[KEY_TOTAL_PUZZLES] ?: 0

    // Flow-based observers — DataStore emits on every edit, so screens see updates
    // without re-creating their ViewModel.
    override fun observeStreak(): Flow<Int> =
        store.data.map { it[KEY_STREAK] ?: 0 }

    override fun observeBestStreak(): Flow<Int> =
        store.data.map { it[KEY_BEST_STREAK] ?: 0 }

    override fun observeTotalPuzzlesSolved(): Flow<Int> =
        store.data.map { it[KEY_TOTAL_PUZZLES] ?: 0 }

    override fun observeSolveDays(): Flow<Set<Long>> =
        store.data.map { prefs ->
            prefs[KEY_SOLVE_DAYS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        }

    override fun observeWeeklySolvedCount(): Flow<Int> =
        store.data.map { prefs ->
            val counts = decodeDayCounts(prefs[KEY_DAILY_COUNTS])
            val today = todayEpochDay()
            (0L until WEEK_WINDOW_DAYS).sumOf { offset -> counts[today - offset] ?: 0 }
        }

    override suspend fun recordSolve(difficulty: String, elapsedMs: Long): Int {
        val today = todayEpochDay()
        var newStreak = 0
        store.edit { prefs ->
            // Idempotency guard. The same solved puzzle can reach recordSolve
            // more than once — e.g. the Win screen is re-entered after process
            // death or a config change (its LaunchedEffect re-fires with the
            // same route args). Without this, total / recent-times / the
            // improvement chart silently inflate. elapsedMs is millisecond-
            // precise, so two genuinely distinct solves never collide.
            val sig = "$difficulty:$elapsedMs"
            if (prefs[KEY_LAST_SOLVE_SIG] == sig) {
                newStreak = prefs[KEY_STREAK] ?: 0
                return@edit
            }
            prefs[KEY_LAST_SOLVE_SIG] = sig

            newStreak = applyStreak(prefs, today)
            val existing = prefs[KEY_SOLVE_DAYS] ?: emptySet()
            prefs[KEY_SOLVE_DAYS] = existing + today.toString()
            prefs[KEY_TOTAL_PUZZLES] = (prefs[KEY_TOTAL_PUZZLES] ?: 0) + 1
            applyDailyCount(prefs, today)

            // Track most recent difficulty for the Daily "Today" card.
            prefs[KEY_LAST_DIFFICULTY] = difficulty
            applyBestTime(prefs, difficulty, elapsedMs)

            // Rolling list of recent solve times for the Improvement chart.
            val recent = prefs[KEY_RECENT_TIMES]
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                ?: emptyList()
            val updated = (recent + elapsedMs).takeLast(RECENT_TIMES_CAP)
            prefs[KEY_RECENT_TIMES] = updated.joinToString(",")
        }
        return newStreak
    }

    override fun observeBestTimes(): Flow<Map<String, Long>> =
        store.data.map { prefs -> decodeBestTimes(prefs[KEY_BEST_TIMES]) }

    override fun observeRecentSolveTimes(): Flow<List<Long>> =
        store.data.map { prefs ->
            prefs[KEY_RECENT_TIMES]
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                ?: emptyList()
        }

    override fun observeLastPlayedDifficulty(): Flow<String?> =
        store.data.map { it[KEY_LAST_DIFFICULTY] }

    override suspend fun getSolveDays(): Set<Long> =
        store.data.first()[KEY_SOLVE_DAYS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()

    /**
     * Debug-only: pre-fill streak / total / last-N-day solve marks.
     *
     * Marks `streakDays` consecutive days ending today as solved, sets the streak counter
     * to `streakDays`, and sets total puzzles to `totalPuzzles`. Best-streak is bumped if
     * the new value exceeds it. Use from the Settings → Debug panel to test Win and Stats
     * screens without grinding through real puzzles.
     */
    override suspend fun seedSolveData(streakDays: Int, totalPuzzles: Int) {
        val today = todayEpochDay()
        store.edit { prefs ->
            // Mark consecutive days ending today as solved.
            val marks = (0 until streakDays.coerceAtLeast(0))
                .map { offset -> (today - offset).toString() }
                .toSet()
            prefs[KEY_SOLVE_DAYS] = marks
            prefs[KEY_STREAK] = streakDays.coerceAtLeast(0)
            prefs[KEY_LAST_SOLVE_DAY] = today
            prefs[KEY_TOTAL_PUZZLES] = totalPuzzles.coerceAtLeast(0)
            val prevBest = prefs[KEY_BEST_STREAK] ?: 0
            if (streakDays > prevBest) prefs[KEY_BEST_STREAK] = streakDays
        }
    }

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
            prefs.remove(KEY_LAST_DIFFICULTY)
            prefs.remove(KEY_BEST_TIMES)
            prefs.remove(KEY_RECENT_TIMES)
            prefs.remove(KEY_LAST_SOLVE_SIG)
            prefs.remove(KEY_DAILY_COUNTS)
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

    override fun observeIsPro(): Flow<Boolean> =
        store.data.map { it[KEY_IS_PRO] ?: false }

    override suspend fun setIsPro(value: Boolean) {
        store.edit { it[KEY_IS_PRO] = value }
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

    override fun observeShowMistakes(): Flow<Boolean> =
        store.data.map { it[KEY_SHOW_MISTAKES] ?: true }

    override suspend fun setShowMistakes(enabled: Boolean) {
        store.edit { it[KEY_SHOW_MISTAKES] = enabled }
    }

    override fun observeHighLegibility(): Flow<Boolean> =
        store.data.map { it[KEY_HIGH_LEGIBILITY] ?: false }

    override suspend fun setHighLegibility(enabled: Boolean) {
        store.edit { it[KEY_HIGH_LEGIBILITY] = enabled }
    }

    override fun observeStrictConflicts(): Flow<Boolean> =
        store.data.map { it[KEY_STRICT_CONFLICTS] ?: false }

    override suspend fun setStrictConflicts(enabled: Boolean) {
        store.edit { it[KEY_STRICT_CONFLICTS] = enabled }
    }
}

@OptIn(ExperimentalTime::class)
private fun todayEpochDay(): Long {
    val now = Clock.System.now()
    val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
    return local.date.toEpochDays()
}

/**
 * Advance the daily streak for a solve on [today] and return the new value.
 * Streak only changes on the first solve of a calendar day; same-day solves
 * keep the current count. Best-streak is bumped when the streak exceeds it.
 */
private fun applyStreak(prefs: MutablePreferences, today: Long): Int {
    val lastDay = prefs[KEY_LAST_SOLVE_DAY] ?: Long.MIN_VALUE
    val current = prefs[KEY_STREAK] ?: 0
    val newStreak = when {
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
    return newStreak
}

/** Record [elapsedMs] as the personal best for [difficulty] if it beats the prior one. */
private fun applyBestTime(prefs: MutablePreferences, difficulty: String, elapsedMs: Long) {
    val bests = decodeBestTimes(prefs[KEY_BEST_TIMES])
    val prevBestForDiff = bests[difficulty]
    if (prevBestForDiff == null || elapsedMs < prevBestForDiff) {
        prefs[KEY_BEST_TIMES] = encodeBestTimes(bests + (difficulty to elapsedMs))
    }
}

/**
 * Increment the solve count for [today] and prune entries older than
 * [DAILY_COUNTS_RETENTION_DAYS] so storage stays bounded.
 */
private fun applyDailyCount(prefs: MutablePreferences, today: Long) {
    val counts = decodeDayCounts(prefs[KEY_DAILY_COUNTS]).toMutableMap()
    counts[today] = (counts[today] ?: 0) + 1
    val cutoff = today - DAILY_COUNTS_RETENTION_DAYS
    prefs[KEY_DAILY_COUNTS] = encodeDayCounts(counts.filterKeys { it >= cutoff })
}

private fun encodeDayCounts(counts: Map<Long, Int>): Set<String> =
    counts.entries.map { "${it.key}:${it.value}" }.toSet()

private fun decodeDayCounts(raw: Set<String>?): Map<Long, Int> {
    if (raw.isNullOrEmpty()) return emptyMap()
    return buildMap {
        raw.forEach { entry ->
            val idx = entry.lastIndexOf(':')
            if (idx > 0) {
                val day = entry.substring(0, idx).toLongOrNull()
                val count = entry.substring(idx + 1).toIntOrNull()
                if (day != null && count != null) put(day, count)
            }
        }
    }
}

/** Encode a difficulty→best-time map as a String set: `["Easy:240000", "Medium:412000", ...]`. */
private fun encodeBestTimes(times: Map<String, Long>): Set<String> =
    times.entries.map { "${it.key}:${it.value}" }.toSet()

private fun decodeBestTimes(raw: Set<String>?): Map<String, Long> {
    if (raw.isNullOrEmpty()) return emptyMap()
    return buildMap {
        raw.forEach { entry ->
            val idx = entry.lastIndexOf(':')
            if (idx > 0) {
                val key = entry.substring(0, idx)
                val value = entry.substring(idx + 1).toLongOrNull()
                if (value != null) put(key, value)
            }
        }
    }
}
