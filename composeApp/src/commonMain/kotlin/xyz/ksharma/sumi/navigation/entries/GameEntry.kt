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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.analytics.SumiAnalytics
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.haptic.HapticEngine
import xyz.ksharma.sumi.haptic.rememberHapticEngine
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.navigation.WinRoute
import xyz.ksharma.sumi.preferences.ThemePreferences
import xyz.ksharma.sumi.screens.game.GameCallbacks
import xyz.ksharma.sumi.screens.game.GameScreen
import xyz.ksharma.sumi.screens.game.GameViewModel

private class HapticContext(private val engine: HapticEngine, private val enabled: Boolean) {
    fun tick() { if (enabled) engine.tick() }
    fun confirm() { if (enabled) engine.confirm() }
    fun error() { if (enabled) engine.error() }
    fun win() { if (enabled) engine.win() }
}

private class GameContext(val haptic: HapticContext, val analytics: SumiAnalytics)

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.GameEntry(navigator: SumiNavigator) {
    entry<GameRoute> { key ->
        val vm: GameViewModel = koinViewModel()
        val haptic = rememberHapticEngine()
        val themePrefs = koinInject<ThemePreferences>()
        val analytics = koinInject<SumiAnalytics>()
        val diff = Difficulty.entries.firstOrNull { it.name == key.difficulty } ?: Difficulty.Medium

        LaunchedEffect(key.difficulty) {
            vm.init(diff)
            analytics.logGameStarted(key.difficulty)
        }

        val state by vm.state.collectAsState()
        val elapsedMs by vm.elapsedMs.collectAsState()
        val hapticsEnabled by themePrefs.observeHapticsEnabled().collectAsState(initial = true)
        val ctx = GameContext(HapticContext(haptic, hapticsEnabled), analytics)

        GameEntryContent(
            vm = vm,
            state = state,
            elapsedMs = elapsedMs,
            diff = diff,
            ctx = ctx,
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
    ctx: GameContext,
    routeKey: GameRoute,
    navigator: SumiNavigator,
) {
    var paused by rememberSaveable { mutableStateOf(false) }
    val celebrationCount by vm.celebrationCount.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            ctx.haptic.win()
            ctx.analytics.logGameCompleted(
                difficulty = routeKey.difficulty,
                elapsedSeconds = elapsedMs / 1000L,
                mistakes = state.mistakeCount,
            )
            // Clear back to home first so Win is never stacked on top of Game.
            // System back from Win → Home; "Next Practice" from Win → new Game on fresh stack.
            navigator.resetRoot(HomeRoute)
            navigator.goTo(
                WinRoute(elapsedMs = elapsedMs, mistakeCount = state.mistakeCount, difficulty = routeKey.difficulty),
            )
        }
    }

    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) ctx.analytics.logGameOver(routeKey.difficulty)
    }

    GameScreen(
        state = state,
        elapsedMs = elapsedMs,
        celebrationCount = celebrationCount,
        paused = paused,
        // Derive directly from state so the overlay only shows when the game is actually over.
        // Using rememberSaveable caused the stale "game over" state from a previous play session
        // to re-appear when the VM hadn't yet been reset via init().
        gameOver = state.isGameOver,
        difficulty = diff,
        callbacks = buildGameCallbacks(
            vm = vm,
            ctx = ctx,
            state = state,
            navigator = navigator,
            onPause = { paused = true },
            onResume = { paused = false },
            onNewPuzzle = {
                paused = false
                vm.clearSave()
                vm.init(diff)
            },
        ),
    )
}

private fun buildGameCallbacks(
    vm: GameViewModel,
    ctx: GameContext,
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
        ctx.haptic.tick()
        vm.select(r, c)
    },
    onEnter = { digit ->
        val sel = state.selected
        when {
            state.notesMode -> ctx.haptic.tick()
            sel != null && digit == state.solution[sel.first][sel.second] -> ctx.haptic.confirm()
            else -> ctx.haptic.error()
        }
        vm.enter(digit)
    },
    onErase = {
        ctx.haptic.tick()
        vm.erase()
    },
    onUndo = {
        ctx.haptic.tick()
        vm.undo()
    },
    onHint = {
        ctx.haptic.tick()
        ctx.analytics.logHintUsed(state.difficulty.name)
        vm.hint()
    },
    onToggleNotes = {
        ctx.haptic.tick()
        vm.toggleNotes()
    },
    onNewPuzzle = onNewPuzzle,
)
