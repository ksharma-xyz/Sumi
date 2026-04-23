package xyz.ksharma.sumi.screens.win

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.preferences.SumiPreferences

class WinViewModel(private val prefs: SumiPreferences) : ViewModel() {

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    fun onPuzzleCompleted() {
        viewModelScope.launch {
            _streak.value = prefs.recordSolve()
        }
    }
}
