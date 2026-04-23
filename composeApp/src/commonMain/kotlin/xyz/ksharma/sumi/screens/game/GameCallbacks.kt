package xyz.ksharma.sumi.screens.game

data class GameCallbacks(
    val onBack: () -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onSelect: (Int, Int) -> Unit,
    val onEnter: (Int) -> Unit,
    val onErase: () -> Unit,
    val onUndo: () -> Unit,
    val onHint: () -> Unit,
    val onToggleNotes: () -> Unit,
    val onNewPuzzle: () -> Unit,
)
