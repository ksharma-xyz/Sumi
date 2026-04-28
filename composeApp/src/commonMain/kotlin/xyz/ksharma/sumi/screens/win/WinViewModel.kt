package xyz.ksharma.sumi.screens.win

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.ksharma.sumi.ads.AdOrchestrator
import xyz.ksharma.sumi.coroutines.ext.launchWithExceptionHandler
import xyz.ksharma.sumi.preferences.SumiPreferences

private const val INTERSTITIAL_EVERY_N_SOLVES = 3
private const val MIN_VALID_SOLVE_MS = 1_000L

class WinViewModel(
    private val prefs: SumiPreferences,
    private val adOrchestrator: AdOrchestrator,
) : ViewModel() {

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _showInterstitial = MutableStateFlow(false)
    val showInterstitial: StateFlow<Boolean> = _showInterstitial.asStateFlow()

    fun onPuzzleCompleted(difficulty: String, elapsedMs: Long) {
        // Belt-and-braces: GameEntry's win-trigger already gates on a minimum
        // valid elapsed time, but the WinRoute can also be reached via the
        // debug "Open sample win screen" entry. Skip recording sub-second
        // solves so debug + race-condition wins never pollute Stats.
        if (elapsedMs < MIN_VALID_SOLVE_MS) return
        viewModelScope.launchWithExceptionHandler<WinViewModel>(dispatcher = Dispatchers.Default) {
            _streak.value = prefs.recordSolve(difficulty = difficulty, elapsedMs = elapsedMs)
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
