package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.koin.compose.koinInject
import xyz.ksharma.sumi.navigation.LicensesRoute
import xyz.ksharma.sumi.navigation.SettingsRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.preferences.SumiPreferences
import xyz.ksharma.sumi.preferences.ThemeMode
import xyz.ksharma.sumi.screens.settings.SettingsScreen

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.SettingsEntry(navigator: SumiNavigator) {
    entry<SettingsRoute> {
        val prefs = koinInject<SumiPreferences>()
        val themeMode by prefs.themeMode().collectAsState(ThemeMode.FollowSystem)
        SettingsScreen(
            onBack = { navigator.pop() },
            onLicenses = { navigator.goTo(LicensesRoute) },
            currentThemeMode = themeMode,
            onThemeModeChange = { prefs.setThemeMode(it) },
        )
    }
}
