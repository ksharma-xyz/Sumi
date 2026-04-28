package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.navigation.LicensesRoute
import xyz.ksharma.sumi.navigation.SettingsRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.preferences.ThemePreferences
import xyz.ksharma.sumi.screens.settings.DebugCallbacks
import xyz.ksharma.sumi.screens.settings.SettingsScreen
import xyz.ksharma.sumi.screens.settings.SettingsViewModel
import xyz.ksharma.sumi.screens.settings.ThemeState
import xyz.ksharma.sumi.theme.SumiSeason

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.SettingsEntry(navigator: SumiNavigator) {
    entry<SettingsRoute> {
        val vm: SettingsViewModel = koinViewModel()
        val themePrefs = koinInject<ThemePreferences>()
        val currentSeason by themePrefs.observeSeason().collectAsState(initial = SumiSeason.Spring)
        val isHapticsEnabled by vm.isHapticsEnabled.collectAsState()
        val isSimulatingPro by vm.isSimulatingPro.collectAsState()
        val isAdsEnabled by vm.isAdsEnabled.collectAsState()
        SettingsScreen(
            onBack = { navigator.pop() },
            onLicenses = { navigator.goTo(LicensesRoute) },
            themeState = ThemeState(season = currentSeason, hapticsEnabled = isHapticsEnabled),
            onSeasonSelect = { vm.setSeason(it) },
            onHapticsToggle = { vm.setHapticsEnabled(!isHapticsEnabled) },
            debugCallbacks = if (vm.isDebug) {
                DebugCallbacks(
                    onResetOnboarding = { vm.resetOnboarding() },
                    onClearStats = { vm.clearStats() },
                    onClearSaves = { vm.clearGameSaves() },
                    onClearAll = { vm.clearAll() },
                    isSimulatingPro = isSimulatingPro,
                    onToggleSimulatePro = { vm.toggleSimulatePro() },
                    isAdsEnabled = isAdsEnabled,
                    onToggleAdsEnabled = { vm.toggleAdsEnabled() },
                )
            } else null,
        )
    }
}
