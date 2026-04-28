package xyz.ksharma.sumi.screens.settings

data class DebugCallbacks(
    val onResetOnboarding: () -> Unit,
    val onClearStats: () -> Unit,
    val onClearSaves: () -> Unit,
    val onClearAll: () -> Unit,
    val isSimulatingPro: Boolean,
    val onToggleSimulatePro: () -> Unit,
    val isAdsEnabled: Boolean,
    val onToggleAdsEnabled: () -> Unit,
)
