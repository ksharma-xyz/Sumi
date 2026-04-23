package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.FREE_QUOTES
import xyz.ksharma.sumi.haptic.rememberHapticEngine
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.navigation.WinRoute
import xyz.ksharma.sumi.screens.win.WinScreen
import xyz.ksharma.sumi.screens.win.WinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.WinEntry(navigator: SumiNavigator) {
    entry<WinRoute> { key ->
        val vm: WinViewModel = koinViewModel()
        val haptic = rememberHapticEngine()
        val streak by vm.streak.collectAsState()

        LaunchedEffect(Unit) {
            haptic.win()
            vm.onPuzzleCompleted()
        }

        @OptIn(ExperimentalTime::class)
        val dayOfYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfYear
        val quote = FREE_QUOTES[dayOfYear % FREE_QUOTES.size]

        WinScreen(
            elapsedMs = key.elapsedMs,
            mistakeCount = key.mistakeCount,
            difficulty = key.difficulty,
            streakDays = streak,
            quote = quote,
            onHome = { navigator.resetRoot(HomeRoute) },
            onNextPuzzle = {
                navigator.goTo(GameRoute(difficulty = key.difficulty))
            },
        )
    }
}
