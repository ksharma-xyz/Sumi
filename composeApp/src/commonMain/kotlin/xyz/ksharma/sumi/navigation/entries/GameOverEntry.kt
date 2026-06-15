package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.koin.compose.koinInject
import xyz.ksharma.sumi.haptic.rememberHapticEngine
import xyz.ksharma.sumi.navigation.GameOverRoute
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.preferences.ThemePreferences
import xyz.ksharma.sumi.screens.gameover.GameOverScreen

private const val MILLIS_PER_SECOND = 1000L
private const val SECONDS_PER_MINUTE = 60L

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.GameOverEntry(navigator: SumiNavigator) {
    entry<GameOverRoute> { key ->
        val haptic = rememberHapticEngine()
        val themePrefs = koinInject<ThemePreferences>()
        val hapticsEnabled by themePrefs.observeHapticsEnabled().collectAsState(initial = true)

        LaunchedEffect(Unit) { if (hapticsEnabled) haptic.error() }

        GameOverScreen(
            difficulty = key.difficulty,
            elapsedLabel = formatElapsed(key.elapsedMs),
            onRetry = {
                // Clears the game-over screen from the stack, then starts a fresh game.
                navigator.resetRoot(HomeRoute)
                navigator.goTo(GameRoute(difficulty = key.difficulty))
            },
            onHome = { navigator.resetRoot(HomeRoute) },
        )
    }
}

private fun formatElapsed(ms: Long): String {
    val totalSeconds = ms / MILLIS_PER_SECOND
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
