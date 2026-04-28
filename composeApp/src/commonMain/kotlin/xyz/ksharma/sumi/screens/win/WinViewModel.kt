package xyz.ksharma.sumi.screens.win

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.ads.AdOrchestrator
import xyz.ksharma.sumi.preferences.SumiPreferences

private const val INTERSTITIAL_EVERY_N_SOLVES = 3

class WinViewModel(
    private val prefs: SumiPreferences,
    private val adOrchestrator: AdOrchestrator,
) : ViewModel() {

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _showInterstitial = MutableStateFlow(false)
    val showInterstitial: StateFlow<Boolean> = _showInterstitial.asStateFlow()

    fun onPuzzleCompleted() {
        viewModelScope.launch {
            _streak.value = prefs.recordSolve()
            val total = prefs.getTotalPuzzlesSolved()
            val nthSolve = (total % INTERSTITIAL_EVERY_N_SOLVES == 0)
            // Frequency cap is the orchestrator's job — never two interstitials within 90s,
            // never more than 3 in a single calendar day.
            _showInterstitial.value = nthSolve && adOrchestrator.mayShowInterstitial()
        }
    }

    /** Called from the Win screen once the interstitial finishes (dismissed or failed-to-load). */
    fun onInterstitialDone() {
        if (_showInterstitial.value) adOrchestrator.onInterstitialShown()
        _showInterstitial.value = false
    }
}
