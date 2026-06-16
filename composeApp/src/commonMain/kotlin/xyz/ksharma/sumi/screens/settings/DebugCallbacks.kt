package xyz.ksharma.sumi.screens.settings

import xyz.ksharma.sumi.design.board.BoardEntrance

data class DebugCallbacks(
    val onResetOnboarding: () -> Unit,
    val onClearStats: () -> Unit,
    val onClearSaves: () -> Unit,
    val onClearAll: () -> Unit,
    val isSimulatingPro: Boolean,
    val onToggleSimulatePro: () -> Unit,
    val isAdsEnabled: Boolean,
    val onToggleAdsEnabled: () -> Unit,
    /** Current board entrance animation + a tap-to-cycle through the options. */
    val boardEntrance: BoardEntrance,
    val onCycleBoardEntrance: () -> Unit,
    /** Seeds streak=5, totalPuzzles=20, last 5 days marked as solved. */
    val onSeedSampleStreak: () -> Unit,
    /** Pushes a sample WinRoute so the screen can be QA'd without grinding through a puzzle. */
    val onOpenSampleWin: () -> Unit,
)
