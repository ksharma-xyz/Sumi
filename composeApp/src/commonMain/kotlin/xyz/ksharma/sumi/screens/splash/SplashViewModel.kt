package xyz.ksharma.sumi.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.OnboardingRoute
import xyz.ksharma.sumi.preferences.SumiPreferences

data class SplashUiState(
    val animationComplete: Boolean = false,
    val navigationTarget: NavKey? = null,
)

class SplashViewModel(private val prefs: SumiPreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(SPLASH_DURATION_MS)
            val target = if (prefs.hasSeenOnboarding()) HomeRoute else OnboardingRoute
            _uiState.update { it.copy(animationComplete = true, navigationTarget = target) }
        }
    }

    companion object {
        private const val SPLASH_DURATION_MS = 2000L
    }
}
