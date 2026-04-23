package xyz.ksharma.sumi.navigation.entries

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.navigation.WinRoute
import xyz.ksharma.sumi.screens.game.GameScreen

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.GameEntry(navigator: SumiNavigator) {
    entry<GameRoute> { key ->
        GameScreen(
            difficulty = key.difficulty,
            onBack = { navigator.pop() },
            onWin = { navigator.goTo(WinRoute) },
        )
    }
}
