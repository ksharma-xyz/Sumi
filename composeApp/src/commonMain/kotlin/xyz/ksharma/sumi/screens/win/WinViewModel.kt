package xyz.ksharma.sumi.screens.win

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.preferences.SumiPreferences

private const val INTERSTITIAL_EVERY_N_SOLVES = 3

class WinViewModel(private val prefs: SumiPreferences) : ViewModel() {

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _showInterstitial = MutableStateFlow(false)
    val showInterstitial: StateFlow<Boolean> = _showInterstitial.asStateFlow()

    fun onPuzzleCompleted() {
        viewModelScope.launch {
            _streak.value = prefs.recordSolve()
            val total = prefs.getTotalPuzzlesSolved()
            _showInterstitial.value = (total % INTERSTITIAL_EVERY_N_SOLVES == 0)
        }
    }
}
