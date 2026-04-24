package xyz.ksharma.sumi.screens.settings

data class DebugCallbacks(
    val onResetOnboarding: () -> Unit,
    val onClearStats: () -> Unit,
    val onClearSaves: () -> Unit,
    val onClearAll: () -> Unit,
)
