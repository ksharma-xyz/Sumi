package xyz.ksharma.sumi.navigation.entries

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.lexilabs.basic.ads.AdState
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.BannerAd
import app.lexilabs.basic.ads.composable.InterstitialAd
import app.lexilabs.basic.ads.composable.RewardedAd
import app.lexilabs.basic.ads.composable.rememberInterstitialAd
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.ads.AdUnits
import xyz.ksharma.sumi.analytics.SumiAnalytics
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.haptic.HapticEngine
import xyz.ksharma.sumi.haptic.rememberHapticEngine
import xyz.ksharma.sumi.navigation.GameOverRoute
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.navigation.WinRoute
import xyz.ksharma.sumi.preferences.DebugPreferences
import xyz.ksharma.sumi.preferences.ProRepository
import xyz.ksharma.sumi.preferences.ThemePreferences
import xyz.ksharma.sumi.screens.game.GameCallbacks
import xyz.ksharma.sumi.screens.game.GameScreen
import xyz.ksharma.sumi.screens.game.GameViewModel

private const val MIN_VALID_SOLVE_MS = 1_000L
private const val WIN_CELEBRATION_DWELL_MS = 3_500L

private class HapticContext(private val engine: HapticEngine, private val enabled: Boolean) {
    fun tick() { if (enabled) engine.tick() }
    fun confirm() { if (enabled) engine.confirm() }
    fun error() { if (enabled) engine.error() }
    fun win() { if (enabled) engine.win() }
}

private class GameContext(val haptic: HapticContext, val analytics: SumiAnalytics)

// Entry composables glue many flows + ads — splitting hurts traceability.
// ModifierMissing suppressed: this is a Navigation 3 entry function, not a
// regular UI composable — the modifier doesn't apply at this layer.
@Suppress("ComposableNaming", "LongMethod", "CyclomaticComplexMethod", "ModifierMissing")
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun EntryProviderScope<NavKey>.GameEntry(navigator: SumiNavigator) {
    entry<GameRoute> { key ->
        val vm: GameViewModel = koinViewModel()
        val haptic = rememberHapticEngine()
        val themePrefs = koinInject<ThemePreferences>()
        val analytics = koinInject<SumiAnalytics>()
        val proRepo = koinInject<ProRepository>()
        val debugPrefs = koinInject<DebugPreferences>()
        val adUnits = koinInject<AdUnits>()
        val diff = Difficulty.entries.firstOrNull { it.name == key.difficulty } ?: Difficulty.Medium
        val isPro by proRepo.isPro().collectAsState(initial = false)
        val isAdsEnabled by debugPrefs.observeAdsEnabled().collectAsState(initial = true)
        var paused by rememberSaveable { mutableStateOf(false) }

        // Gate the board on init having started *for this entry*. The first
        // composition runs before the LaunchedEffect below fires vm.init(), so
        // a reused/retained VM would briefly expose a stale board + stale
        // isInitializing=false — that's the "puzzle flashes, then loading
        // animation, then puzzle" the player sees. initStarted stays false
        // until we've actually kicked off generation for this route.
        var initStarted by remember(key) { mutableStateOf(false) }
        LaunchedEffect(key.difficulty) {
            paused = false // new game is never paused — keep the overlay state in sync with the VM
            vm.init(diff, proHints = isPro)
            initStarted = true
            analytics.logGameStarted(key.difficulty)
        }

        val state by vm.state.collectAsState()
        val elapsedMs by vm.elapsedMs.collectAsState()
        val celebrationCount by vm.celebrationCount.collectAsState()
        val gridCelebrationCount by vm.gridCelebrationCount.collectAsState()
        val isInitializing by vm.isInitializing.collectAsState()
        val showIdleInterstitial by vm.showIdleInterstitial.collectAsState()
        val showRewardedHintAd by vm.showRewardedHintAd.collectAsState()
        val hapticsEnabled by themePrefs.observeHapticsEnabled().collectAsState(initial = true)
        val showMistakes by themePrefs.observeShowMistakes().collectAsState(initial = true)
        val highLegibility by themePrefs.observeHighLegibility().collectAsState(initial = false)
        val strictConflicts by themePrefs.observeStrictConflicts().collectAsState(initial = false)
        val digitFirst by themePrefs.observeDigitFirstInput().collectAsState(initial = false)
        val livesMode by themePrefs.observeLivesMode().collectAsState(initial = false)
        val selectedDigit by vm.selectedDigit.collectAsState()
        val gameOver by vm.gameOver.collectAsState()
        val ctx = GameContext(HapticContext(haptic, hapticsEnabled), analytics)

        // Keep the VM's lives rule in sync with the setting (so a mid-game toggle applies).
        LaunchedEffect(livesMode) { vm.setLivesEnabled(livesMode) }

        // Out of lives → game over. resetRoot(Home) first so back from game-over goes Home,
        // not back onto the dead board (mirrors the win flow).
        LaunchedEffect(gameOver) {
            if (gameOver) {
                navigator.resetRoot(HomeRoute)
                navigator.goTo(GameOverRoute(difficulty = key.difficulty, elapsedMs = elapsedMs))
            }
        }

        // Auto-pause when the app is backgrounded so the clock freezes and the player
        // returns to a paused board instead of a running timer. Skipped while one of our
        // own ads is showing (those background the activity but aren't the player leaving).
        LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
            // Skip when we're leaving for the win / game-over screens (and while our own ads
            // show) — otherwise the pause overlay flashes during that navigation.
            val leavingForResult = state.isComplete || gameOver
            val adShowing = showIdleInterstitial || showRewardedHintAd
            if (!adShowing && !leavingForResult) {
                paused = true
                vm.setPaused(true)
            }
        }

        // Pre-load idle interstitial — see WinScreen comment for the rationale (basic-ads
        // crashes if InterstitialAd is composed before async load completes). Always called
        // (composable rules) but only rendered when the orchestrator decides the moment + the
        // pre-load reports READY.
        val idleAdState = rememberInterstitialAd(
            adUnitId = adUnits.interstitial,
            onLoad = {},
            onFailure = {},
        )

        LaunchedEffect(state.isComplete) {
            // Guard: never trigger Win for elapsedMs == 0. We've seen a race after
            // "Next Practice" where state briefly satisfies isComplete before the
            // timer has ticked, recording a 0-min solve and corrupting Stats.
            // The puzzle takes seconds to solve at minimum; a sub-second "win" is
            // never legitimate.
            if (state.isComplete && elapsedMs >= MIN_VALID_SOLVE_MS) {
                // Hold on the board for a beat so the celebration shower can play
                // out before we cut to Win — premium pacing, not abrupt.
                ctx.haptic.win()
                kotlinx.coroutines.delay(WIN_CELEBRATION_DWELL_MS)
                ctx.analytics.logGameCompleted(
                    difficulty = key.difficulty,
                    elapsedSeconds = elapsedMs / 1000L,
                    mistakes = state.mistakeCount,
                )
                // Clear back to home first so Win is never stacked on top of Game.
                // System back from Win → Home; "Next Practice" from Win → new Game on fresh stack.
                navigator.resetRoot(HomeRoute)
                navigator.goTo(
                    WinRoute(
                        elapsedMs = elapsedMs,
                        mistakeCount = state.mistakeCount,
                        moveCount = state.moveCount,
                        difficulty = key.difficulty,
                        // 81 digits, row-major. Used by WinShareCard's grid thumbnail.
                        solution = state.solution.flatMap { it }.joinToString("") { it.toString() },
                    ),
                )
            }
        }

        val showBanner = !isPro && isAdsEnabled

        GameScreen(
            state = state,
            elapsedMs = elapsedMs,
            celebrationCount = celebrationCount,
            gridCelebrationCount = gridCelebrationCount,
            // "Not ready" until init has started for this entry AND finished —
            // GameScreen hides the board and shows the loading overlay while
            // this is true, so no stale puzzle frame ever leaks through.
            isInitializing = !(initStarted && !isInitializing),
            paused = paused,
            difficulty = diff,
            showMistakes = showMistakes,
            highLegibility = highLegibility,
            strictConflicts = strictConflicts,
            selectedDigit = if (digitFirst) selectedDigit else null,
            livesMode = livesMode,
            callbacks = buildGameCallbacks(
                vm = vm,
                ctx = ctx,
                state = state,
                navigator = navigator,
                onPause = {
                    paused = true
                    vm.setPaused(true)
                },
                onResume = {
                    paused = false
                    vm.setPaused(false)
                },
                onNewPuzzle = {
                    paused = false
                    vm.setPaused(false)
                    vm.init(diff, fresh = true, proHints = isPro)
                },
                rewardedHintAvailable = !isPro && isAdsEnabled,
                digitFirst = digitFirst,
                selectedDigit = selectedDigit,
            ),
            bottomBanner = if (showBanner) {
                @Composable {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                    ) { BannerAd(adUnitId = adUnits.banner) }
                }
            } else null,
            rewardedHintAvailable = !isPro && isAdsEnabled,
        )

        // Idle interstitial — gated by !isPro && isAdsEnabled. Only show when the pre-load
        // (declared above) reports READY; otherwise release the flag so the player isn't
        // stuck waiting on an ad that never appears.
        if (showIdleInterstitial && !isPro && isAdsEnabled) {
            // Keep InterstitialAd composed across READY → SHOWING → SHOWN.
            // Removing it mid-show (returning Unit on SHOWING/SHOWN) disposes the
            // composable's DisposableEffect and the underlying iOS GADInterstitialAd
            // while presentation is still in flight — that's the iOS crash inside
            // basic-ads' InterstitialAd seen in production. Only the dismissal
            // callback should drop the composable.
            when (idleAdState.value.state) {
                AdState.READY, AdState.SHOWING, AdState.SHOWN -> InterstitialAd(
                    loadedAd = idleAdState.value,
                    onDismissed = vm::onIdleInterstitialDone,
                    onFailure = { _ -> vm.onIdleInterstitialDone() },
                )
                else -> LaunchedEffect(showIdleInterstitial) { vm.onIdleInterstitialDone() }
            }
        }

        // Rewarded ad → +1 hint. The user opted into this by tapping Hint at zero count,
        // so it bypasses the orchestrator's interstitial frequency cap.
        if (showRewardedHintAd && !isPro && isAdsEnabled) {
            RewardedAd(
                adUnitId = adUnits.rewarded,
                onRewardEarned = { _ -> vm.grantHintsFromAd(count = 1) },
                onDismissed = vm::onRewardedHintAdDone,
                onFailure = { _ -> vm.onRewardedHintAdDone() },
            )
        }
    }
}

@Suppress("LongParameterList")
private fun buildGameCallbacks(
    vm: GameViewModel,
    ctx: GameContext,
    state: BoardState,
    navigator: SumiNavigator,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onNewPuzzle: () -> Unit,
    rewardedHintAvailable: Boolean,
    digitFirst: Boolean,
    selectedDigit: Int?,
): GameCallbacks = GameCallbacks(
    onBack = { navigator.pop() },
    onPause = onPause,
    onResume = onResume,
    onSelect = onCellTap(vm, ctx, state, digitFirst, selectedDigit),
    onEnter = onDigitTap(vm, ctx, state, digitFirst),
    onErase = {
        ctx.haptic.tick()
        vm.erase()
    },
    onUndo = {
        ctx.haptic.tick()
        vm.undo()
    },
    onRedo = {
        ctx.haptic.tick()
        vm.redo()
    },
    onHint = {
        ctx.haptic.tick()
        if (state.hintsRemaining > 0) {
            ctx.analytics.logHintUsed(state.difficulty.name)
            vm.hint()
        } else if (rewardedHintAvailable) {
            // Out of hints — surface the rewarded ad. The reward (+1 hint) is granted
            // in the entry's onRewardEarned handler; the user then taps Hint again to spend it.
            vm.requestRewardedHintAd()
        }
    },
    onToggleNotes = {
        ctx.haptic.tick()
        vm.toggleNotes()
    },
    onFillNotes = {
        ctx.haptic.confirm()
        vm.fillNotes()
    },
    onNewPuzzle = onNewPuzzle,
)

// Cell tap: digit-first places the armed digit (haptic reflects the outcome); otherwise selects.
private fun onCellTap(
    vm: GameViewModel,
    ctx: GameContext,
    state: BoardState,
    digitFirst: Boolean,
    selectedDigit: Int?,
): (Int, Int) -> Unit = { r, c ->
    if (digitFirst) {
        val d = selectedDigit
        when {
            d == null || state.cells[r][c].given -> ctx.haptic.tick()
            d == state.solution[r][c] -> ctx.haptic.confirm()
            else -> ctx.haptic.error()
        }
        vm.placeOrSelect(r, c)
    } else {
        ctx.haptic.tick()
        vm.select(r, c)
    }
}

// Number-pad tap: digit-first arms a digit; otherwise places it in the selected cell.
private fun onDigitTap(
    vm: GameViewModel,
    ctx: GameContext,
    state: BoardState,
    digitFirst: Boolean,
): (Int) -> Unit = { digit ->
    if (digitFirst) {
        ctx.haptic.tick()
        vm.selectDigit(digit)
    } else {
        val sel = state.selected
        when {
            state.notesMode -> ctx.haptic.tick()
            sel != null && digit == state.solution[sel.first][sel.second] -> ctx.haptic.confirm()
            else -> ctx.haptic.error()
        }
        vm.enter(digit)
    }
}
