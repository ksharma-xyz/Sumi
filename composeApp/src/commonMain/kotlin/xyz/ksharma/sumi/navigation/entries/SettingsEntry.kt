package xyz.ksharma.sumi.navigation.entries

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.navigation.LicensesRoute
import xyz.ksharma.sumi.navigation.SettingsRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.screens.settings.SettingsScreen

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.SettingsEntry(navigator: SumiNavigator) {
    entry<SettingsRoute> {
        SettingsScreen(
            onBack = { navigator.pop() },
            onLicenses = { navigator.goTo(LicensesRoute) },
        )
    }
}
