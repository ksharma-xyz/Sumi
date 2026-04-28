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

private class HapticContext(private val engine: HapticEngine, private val enabled: Boolean) {
    fun tick() { if (enabled) engine.tick() }
    fun confirm() { if (enabled) engine.confirm() }
    fun error() { if (enabled) engine.error() }
    fun win() { if (enabled) engine.win() }
}

private class GameContext(val haptic: HapticContext, val analytics: SumiAnalytics)

// Entry composables glue many flows + ads — splitting hurts traceability.
@Suppress("ComposableNaming", "LongMethod", "CyclomaticComplexMethod")
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
        val diff = Difficulty.entries.firstOrNull { it.name == key.difficulty } ?: Difficulty.Medium
        val isPro by proRepo.isPro().collectAsState(initial = false)
        val isAdsEnabled by debugPrefs.observeAdsEnabled().collectAsState(initial = true)
        var paused by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(key.difficulty) {
            vm.init(diff, proHints = isPro)
            analytics.logGameStarted(key.difficulty)
        }

        val state by vm.state.collectAsState()
        val elapsedMs by vm.elapsedMs.collectAsState()
        val celebrationCount by vm.celebrationCount.collectAsState()
        val showIdleInterstitial by vm.showIdleInterstitial.collectAsState()
        val showRewardedHintAd by vm.showRewardedHintAd.collectAsState()
        val hapticsEnabled by themePrefs.observeHapticsEnabled().collectAsState(initial = true)
        val ctx = GameContext(HapticContext(haptic, hapticsEnabled), analytics)

        // Pre-load idle interstitial — see WinScreen comment for the rationale (basic-ads
        // crashes if InterstitialAd is composed before async load completes). Always called
        // (composable rules) but only rendered when the orchestrator decides the moment + the
        // pre-load reports READY.
        val idleAdState = rememberInterstitialAd(
            adUnitId = AdUnits.Interstitial,
            onLoad = {},
            onFailure = {},
        )

        LaunchedEffect(state.isComplete) {
            if (state.isComplete) {
                ctx.haptic.win()
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
            paused = paused,
            difficulty = diff,
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
            ),
            bottomBanner = if (showBanner) {
                @Composable { BannerAd(adUnitId = AdUnits.Banner) }
            } else null,
            rewardedHintAvailable = !isPro && isAdsEnabled,
        )

        // Idle interstitial — gated by !isPro && isAdsEnabled. Only show when the pre-load
        // (declared above) reports READY; otherwise release the flag so the player isn't
        // stuck waiting on an ad that never appears.
        if (showIdleInterstitial && !isPro && isAdsEnabled) {
            when (idleAdState.value.state) {
                AdState.READY -> InterstitialAd(
                    loadedAd = idleAdState.value,
                    onDismissed = vm::onIdleInterstitialDone,
                    onFailure = { _ -> vm.onIdleInterstitialDone() },
                )
                AdState.SHOWING, AdState.SHOWN -> Unit
                else -> LaunchedEffect(showIdleInterstitial) { vm.onIdleInterstitialDone() }
            }
        }

        // Rewarded ad → +1 hint. The user opted into this by tapping Hint at zero count,
        // so it bypasses the orchestrator's interstitial frequency cap.
        if (showRewardedHintAd && !isPro && isAdsEnabled) {
            RewardedAd(
                adUnitId = AdUnits.Rewarded,
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
    onNewPuzzle = onNewPuzzle,
)
