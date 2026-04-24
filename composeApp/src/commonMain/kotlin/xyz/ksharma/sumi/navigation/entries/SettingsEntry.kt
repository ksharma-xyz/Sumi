package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.navigation.LicensesRoute
import xyz.ksharma.sumi.navigation.SettingsRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.screens.settings.DebugCallbacks
import xyz.ksharma.sumi.screens.settings.SettingsScreen
import xyz.ksharma.sumi.screens.settings.SettingsViewModel

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.SettingsEntry(navigator: SumiNavigator) {
    entry<SettingsRoute> {
        val vm: SettingsViewModel = koinViewModel()
        SettingsScreen(
            onBack = { navigator.pop() },
            onLicenses = { navigator.goTo(LicensesRoute) },
            isDebug = vm.isDebug,
            debugCallbacks = DebugCallbacks(
                onResetOnboarding = { vm.resetOnboarding() },
                onClearStats = { vm.clearStats() },
                onClearSaves = { vm.clearGameSaves() },
                onClearAll = { vm.clearAll() },
            ),
        )
    }
}
