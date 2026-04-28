package xyz.ksharma.sumi.screens.win

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.BuildKonfig
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

    fun onPuzzleCompleted(difficulty: String, elapsedMs: Long) {
        viewModelScope.launch {
            _streak.value = prefs.recordSolve(difficulty = difficulty, elapsedMs = elapsedMs)
            val total = prefs.getTotalPuzzlesSolved()
            val nthSolve = (total % INTERSTITIAL_EVERY_N_SOLVES == 0)
            // Frequency cap is the orchestrator's job — never two interstitials within 90s,
            // never more than 3 in a single calendar day.
            // Debug-build shield: basic-ads' InterstitialAd composable crashes if it's
            // rendered before any ad has been pre-loaded by AdMob (fragile lifecycle inside
            // the library). Until a real AdMob app is registered for the package, skip
            // interstitials entirely in debug. Release builds with real unit IDs are fine.
            _showInterstitial.value = !BuildKonfig.IS_DEBUG &&
                nthSolve &&
                adOrchestrator.mayShowInterstitial()
        }
    }

    /** Called from the Win screen once the interstitial finishes (dismissed or failed-to-load). */
    fun onInterstitialDone() {
        if (_showInterstitial.value) adOrchestrator.onInterstitialShown()
        _showInterstitial.value = false
    }
}
