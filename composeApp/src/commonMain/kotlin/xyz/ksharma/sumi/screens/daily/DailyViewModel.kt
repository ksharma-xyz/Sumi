package xyz.ksharma.sumi.screens.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.preferences.GameSaveRepository
import xyz.ksharma.sumi.preferences.SumiPreferences

/**
 * What the [DailyScreen] needs to render the calendar + today card + previous months.
 */
data class DailyState(
    val solveDays: Set<Long> = emptySet(),
    val streak: Int = 0,
    /** Difficulty the player most recently solved at, or Medium for first-timers. */
    val currentDifficulty: Difficulty = Difficulty.Medium,
    /** Today's puzzle status for [currentDifficulty]: Unstarted, InProgress(ms), Done. */
    val todayStatus: TodayStatus = TodayStatus.Unstarted,
)

sealed interface TodayStatus {
    data object Unstarted : TodayStatus
    data class InProgress(val elapsedMs: Long) : TodayStatus
    data object Done : TodayStatus
}

class DailyViewModel(
    prefs: SumiPreferences,
    private val saveRepository: GameSaveRepository,
) : ViewModel() {

    val state: StateFlow<DailyState> = combine(
        prefs.observeSolveDays(),
        prefs.observeStreak(),
        prefs.observeLastPlayedDifficulty().map { name ->
            Difficulty.entries.firstOrNull { it.name == name } ?: Difficulty.Medium
        },
    ) { days, streak, difficulty ->
        // Fetch today's save synchronously inside the flow update — the load is fast
        // (DataStore single read) and only re-runs when one of the upstream flows ticks.
        val save = saveRepository.loadSave(difficulty)
        val todayStatus: TodayStatus = when {
            save == null -> TodayStatus.Unstarted
            // A non-zero elapsed + cells != all-empty → in progress.
            save.cells.split(",").any { it.trim().toIntOrNull() != null && it.trim().toInt() != 0 } ->
                TodayStatus.InProgress(save.elapsedMs)
            else -> TodayStatus.Unstarted
        }
        DailyState(
            solveDays = days,
            streak = streak,
            currentDifficulty = difficulty,
            todayStatus = todayStatus,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
        initialValue = DailyState(),
    )

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}
