package xyz.ksharma.sumi.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import xyz.ksharma.sumi.preferences.SumiPreferences

data class StatsState(
    val totalPuzzlesSolved: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val daysPlayed: Int = 0,
)

/**
 * Reactive Stats — combines DataStore-backed flows so the screen updates the moment a
 * puzzle is solved (`recordSolve` writes the same DataStore that these flows observe).
 *
 * Replaces the previous one-shot `init { launch { _state.value = ... } }` which read
 * prefs once at construction and never refreshed — the cause of the "Stats shows 0
 * after solving" bug.
 */
class StatsViewModel(prefs: SumiPreferences) : ViewModel() {

    val state: StateFlow<StatsState> = combine(
        prefs.observeTotalPuzzlesSolved(),
        prefs.observeStreak(),
        prefs.observeBestStreak(),
        prefs.observeSolveDays(),
    ) { total, streak, best, days ->
        StatsState(
            totalPuzzlesSolved = total,
            currentStreak = streak,
            bestStreak = best,
            daysPlayed = days.size,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
        initialValue = StatsState(),
    )

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}
