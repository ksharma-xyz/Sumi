package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.haptic.HapticEngine
import xyz.ksharma.sumi.haptic.rememberHapticEngine
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.navigation.WinRoute
import xyz.ksharma.sumi.screens.game.GameCallbacks
import xyz.ksharma.sumi.screens.game.GameScreen
import xyz.ksharma.sumi.screens.game.GameViewModel

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.GameEntry(navigator: SumiNavigator) {
    entry<GameRoute> { key ->
        val vm: GameViewModel = koinViewModel()
        val haptic = rememberHapticEngine()
        val diff = Difficulty.entries.firstOrNull { it.name == key.difficulty } ?: Difficulty.Medium

        LaunchedEffect(key.difficulty) { vm.init(diff) }

        val state by vm.state.collectAsState()
        val elapsedMs by vm.elapsedMs.collectAsState()

        GameEntryContent(
            vm = vm,
            state = state,
            elapsedMs = elapsedMs,
            diff = diff,
            haptic = haptic,
            routeKey = key,
            navigator = navigator,
        )
    }
}

@Composable
private fun GameEntryContent(
    vm: GameViewModel,
    state: BoardState,
    elapsedMs: Long,
    diff: Difficulty,
    haptic: HapticEngine,
    routeKey: GameRoute,
    navigator: SumiNavigator,
) {
    var paused by rememberSaveable { mutableStateOf(false) }
    var gameOver by rememberSaveable { mutableStateOf(false) }
    val celebrationCount by vm.celebrationCount.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            haptic.win()
            navigator.goTo(
                WinRoute(elapsedMs = elapsedMs, mistakeCount = state.mistakeCount, difficulty = routeKey.difficulty),
            )
        }
    }
    LaunchedEffect(state.isGameOver) { if (state.isGameOver) gameOver = true }

    GameScreen(
        state = state,
        elapsedMs = elapsedMs,
        celebrationCount = celebrationCount,
        paused = paused,
        gameOver = gameOver,
        difficulty = diff,
        callbacks = buildGameCallbacks(
            vm = vm,
            haptic = haptic,
            state = state,
            navigator = navigator,
            onPause = { paused = true },
            onResume = { paused = false },
            onNewPuzzle = {
                paused = false
                gameOver = false
                vm.init(diff)
            },
        ),
    )
}

private fun buildGameCallbacks(
    vm: GameViewModel,
    haptic: HapticEngine,
    state: BoardState,
    navigator: SumiNavigator,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onNewPuzzle: () -> Unit,
): GameCallbacks = GameCallbacks(
    onBack = { navigator.pop() },
    onPause = onPause,
    onResume = onResume,
    onSelect = { r, c ->
        haptic.tick()
        vm.select(r, c)
    },
    onEnter = { digit ->
        val sel = state.selected
        when {
            state.notesMode -> haptic.tick()
            sel != null && digit == state.solution[sel.first][sel.second] -> haptic.confirm()
            else -> haptic.error()
        }
        vm.enter(digit)
    },
    onErase = {
        haptic.tick()
        vm.erase()
    },
    onUndo = {
        haptic.tick()
        vm.undo()
    },
    onHint = {
        haptic.tick()
        vm.hint()
    },
    onToggleNotes = {
        haptic.tick()
        vm.toggleNotes()
    },
    onNewPuzzle = onNewPuzzle,
)
