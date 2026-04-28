package xyz.ksharma.sumi.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import xyz.ksharma.sumi.preferences.ProRepository
import xyz.ksharma.sumi.preferences.SumiPreferences

data class StatsState(
    val totalPuzzlesSolved: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val daysPlayed: Int = 0,
    val solveDays: Set<Long> = emptySet(),
    /** Solve times in ms keyed by difficulty name; empty if never played that difficulty. */
    val bestTimes: Map<String, Long> = emptyMap(),
    /** Rolling 30-entry list of solve elapsed-times in ms, oldest→newest. Drives the chart. */
    val recentSolveTimes: List<Long> = emptyList(),
    /** Difficulty most recently solved at, used as the "Level" cell in the THIS WEEK grid. */
    val lastDifficulty: String? = null,
    /** Pro user — unlocks Improvement chart, Personal Bests, Export CSV. */
    val isPro: Boolean = false,
)

/**
 * Reactive Stats — combines DataStore-backed flows so the screen updates the moment a
 * puzzle is solved (`recordSolve` writes the same DataStore that these flows observe).
 */
class StatsViewModel(
    prefs: SumiPreferences,
    proRepo: ProRepository,
) : ViewModel() {

    val state: StateFlow<StatsState> = combine(
        prefs.observeTotalPuzzlesSolved(),
        prefs.observeStreak(),
        prefs.observeBestStreak(),
        prefs.observeSolveDays(),
        combine(
            prefs.observeBestTimes(),
            prefs.observeRecentSolveTimes(),
            prefs.observeLastPlayedDifficulty(),
            proRepo.isPro(),
        ) { bests, recent, lastDiff, isPro ->
            StatsExtras(bests, recent, lastDiff, isPro)
        },
    ) { total, streak, best, days, extras ->
        StatsState(
            totalPuzzlesSolved = total,
            currentStreak = streak,
            bestStreak = best,
            daysPlayed = days.size,
            solveDays = days,
            bestTimes = extras.bestTimes,
            recentSolveTimes = extras.recentSolveTimes,
            lastDifficulty = extras.lastDifficulty,
            isPro = extras.isPro,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
        initialValue = StatsState(),
    )

    /** Bag for combining 4 extra flows into the main `combine` (which is capped at 5 sources). */
    private data class StatsExtras(
        val bestTimes: Map<String, Long>,
        val recentSolveTimes: List<Long>,
        val lastDifficulty: String?,
        val isPro: Boolean,
    )

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}
