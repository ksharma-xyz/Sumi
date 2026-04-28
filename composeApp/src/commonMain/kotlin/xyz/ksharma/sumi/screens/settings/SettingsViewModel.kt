package xyz.ksharma.sumi.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.preferences.DebugPreferences
import xyz.ksharma.sumi.preferences.GameSaveRepository
import xyz.ksharma.sumi.preferences.ThemePreferences
import xyz.ksharma.sumi.theme.SumiSeason

class SettingsViewModel(
    private val debug: DebugPreferences,
    private val gameSaves: GameSaveRepository,
    private val themes: ThemePreferences,
    val isDebug: Boolean,
) : ViewModel() {

    val isHapticsEnabled: StateFlow<Boolean> = themes.observeHapticsEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), true)

    val isSimulatingPro: StateFlow<Boolean> = debug.observeSimulatePro()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    val isAdsEnabled: StateFlow<Boolean> = debug.observeAdsEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), true)

    fun setSeason(season: SumiSeason) = viewModelScope.launch { themes.setSeason(season) }

    fun setHapticsEnabled(enabled: Boolean) = viewModelScope.launch {
        themes.setHapticsEnabled(enabled)
    }

    fun resetOnboarding() = viewModelScope.launch { debug.resetOnboarding() }

    fun clearStats() = viewModelScope.launch { debug.clearStats() }

    fun clearGameSaves() = viewModelScope.launch { gameSaves.clearAllSaves() }

    fun clearAll() = viewModelScope.launch { debug.clearAll() }

    fun toggleSimulatePro() = viewModelScope.launch {
        debug.setSimulatePro(!isSimulatingPro.value)
    }

    fun toggleAdsEnabled() = viewModelScope.launch {
        debug.setAdsEnabled(!isAdsEnabled.value)
    }
}
