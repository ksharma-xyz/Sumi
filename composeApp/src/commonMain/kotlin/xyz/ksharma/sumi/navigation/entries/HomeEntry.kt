package xyz.ksharma.sumi.navigation.entries

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.SettingsRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.screens.home.HomeScreen

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.HomeEntry(navigator: SumiNavigator) {
    entry<HomeRoute> {
        HomeScreen(
            onStartGame = { difficulty -> navigator.goTo(GameRoute(difficulty = difficulty)) },
            onSettings = { navigator.goTo(SettingsRoute) },
        )
    }
}
