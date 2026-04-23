package xyz.ksharma.sumi.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.preferences.SumiPreferences

data class StatsState(
    val totalPuzzlesSolved: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val daysPlayed: Int = 0,
)

class StatsViewModel(private val prefs: SumiPreferences) : ViewModel() {

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val solveDays = prefs.getSolveDays()
            _state.value = StatsState(
                totalPuzzlesSolved = prefs.getTotalPuzzlesSolved(),
                currentStreak = prefs.getStreak(),
                bestStreak = prefs.getBestStreak(),
                daysPlayed = solveDays.size,
            )
        }
    }
}
