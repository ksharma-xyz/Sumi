package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.FREE_QUOTES
import xyz.ksharma.sumi.ads.AdUnits
import xyz.ksharma.sumi.haptic.rememberHapticEngine
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.navigation.WinRoute
import xyz.ksharma.sumi.preferences.DebugPreferences
import xyz.ksharma.sumi.preferences.ProRepository
import xyz.ksharma.sumi.preferences.ThemePreferences
import xyz.ksharma.sumi.screens.win.WinScreen
import xyz.ksharma.sumi.screens.win.WinViewModel
import xyz.ksharma.sumi.share.ShareManager
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.WinEntry(navigator: SumiNavigator) {
    entry<WinRoute> { key ->
        val vm: WinViewModel = koinViewModel()
        val haptic = rememberHapticEngine()
        val shareManager: ShareManager = koinInject()
        val themePrefs = koinInject<ThemePreferences>()
        val proRepo = koinInject<ProRepository>()
        val debugPrefs = koinInject<DebugPreferences>()
        val adUnits = koinInject<AdUnits>()
        val coroutineScope = rememberCoroutineScope()
        val hapticsEnabled by themePrefs.observeHapticsEnabled().collectAsState(initial = true)
        val isPro by proRepo.isPro().collectAsState(initial = false)
        val isAdsEnabled by debugPrefs.observeAdsEnabled().collectAsState(initial = true)
        val showInterstitial by vm.showInterstitial.collectAsState()

        LaunchedEffect(Unit) {
            if (hapticsEnabled) haptic.win()
            vm.onPuzzleCompleted(difficulty = key.difficulty, elapsedMs = key.elapsedMs)
        }

        @OptIn(ExperimentalTime::class)
        val dayOfYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfYear
        val quote = FREE_QUOTES[dayOfYear % FREE_QUOTES.size]

        WinScreen(
            elapsedMs = key.elapsedMs,
            mistakeCount = key.mistakeCount,
            moveCount = key.moveCount,
            difficulty = key.difficulty,
            quote = quote,
            solution = key.solution,
            onNextPuzzle = {
                // Reset to Home first (clears Win from stack), then push the new game.
                // Without this, pressing back from the new game would return to WinRoute.
                navigator.resetRoot(HomeRoute)
                navigator.goTo(GameRoute(difficulty = key.difficulty))
            },
            onShare = { cardBitmap: ImageBitmap ->
                // The card itself now carries the brand mark (the in-card ChopSeal),
                // so we share it as-is — no Roboto-Bold "Sumi" header strip on top.
                coroutineScope.launch { shareManager.shareImage(cardBitmap) }
            },
            showInterstitialAd = showInterstitial && !isPro && isAdsEnabled,
            onInterstitialDismiss = vm::onInterstitialDone,
            interstitialAdUnitId = adUnits.Interstitial,
        )
    }
}
