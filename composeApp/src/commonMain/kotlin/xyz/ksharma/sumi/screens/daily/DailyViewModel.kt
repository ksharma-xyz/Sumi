package xyz.ksharma.sumi.screens.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import xyz.ksharma.sumi.preferences.SumiPreferences

/**
 * Reactive Daily — observes the same DataStore `recordSolve` writes to, so the calendar
 * grid updates immediately after the player wins.
 */
class DailyViewModel(prefs: SumiPreferences) : ViewModel() {

    val solveDays: StateFlow<Set<Long>> = prefs.observeSolveDays().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
        initialValue = emptySet(),
    )

    val streak: StateFlow<Int> = prefs.observeStreak().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
        initialValue = 0,
    )

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}
