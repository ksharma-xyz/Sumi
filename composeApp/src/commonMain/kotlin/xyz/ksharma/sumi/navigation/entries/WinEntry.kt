package xyz.ksharma.sumi.navigation.entries

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.navigation.WinRoute
import xyz.ksharma.sumi.screens.win.WinScreen

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.WinEntry(navigator: SumiNavigator) {
    entry<WinRoute> {
        WinScreen(
            onHome = { navigator.resetRoot(HomeRoute) },
        )
    }
}
