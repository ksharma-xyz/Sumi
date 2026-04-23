package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.navigation.WinRoute
import xyz.ksharma.sumi.screens.game.GameCallbacks
import xyz.ksharma.sumi.screens.game.GameScreen
import xyz.ksharma.sumi.screens.game.GameViewModel

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.GameEntry(navigator: SumiNavigator) {
    entry<GameRoute> { key ->
        val vm: GameViewModel = koinViewModel()
        val diff = Difficulty.entries.firstOrNull { it.name == key.difficulty } ?: Difficulty.Medium

        LaunchedEffect(key.difficulty) { vm.init(diff) }

        val state by vm.state.collectAsState()
        val elapsedMs by vm.elapsedMs.collectAsState()
        var paused by rememberSaveable { mutableStateOf(false) }
        var gameOver by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(state.isComplete) {
            if (state.isComplete) {
                navigator.goTo(
                    WinRoute(elapsedMs = elapsedMs, mistakeCount = state.mistakeCount, difficulty = key.difficulty),
                )
            }
        }
        LaunchedEffect(state.isGameOver) { if (state.isGameOver) gameOver = true }

        GameScreen(
            state = state,
            elapsedMs = elapsedMs,
            paused = paused,
            gameOver = gameOver,
            difficulty = diff,
            callbacks = GameCallbacks(
                onBack = { navigator.pop() },
                onPause = { paused = true },
                onResume = { paused = false },
                onSelect = { r, c -> vm.select(r, c) },
                onEnter = { digit -> vm.enter(digit) },
                onErase = { vm.erase() },
                onUndo = { vm.undo() },
                onHint = { vm.hint() },
                onToggleNotes = { vm.toggleNotes() },
                onNewPuzzle = {
                    paused = false
                    gameOver = false
                    vm.init(diff)
                },
            ),
        )
    }
}
