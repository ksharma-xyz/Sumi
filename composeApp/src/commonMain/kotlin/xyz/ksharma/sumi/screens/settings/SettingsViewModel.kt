package xyz.ksharma.sumi.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.preferences.DebugPreferences
import xyz.ksharma.sumi.preferences.GameSaveRepository

class SettingsViewModel(
    private val debug: DebugPreferences,
    private val gameSaves: GameSaveRepository,
    val isDebug: Boolean,
) : ViewModel() {

    fun resetOnboarding() = viewModelScope.launch { debug.resetOnboarding() }

    fun clearStats() = viewModelScope.launch { debug.clearStats() }

    fun clearGameSaves() = viewModelScope.launch { gameSaves.clearAllSaves() }

    fun clearAll() = viewModelScope.launch { debug.clearAll() }
}
