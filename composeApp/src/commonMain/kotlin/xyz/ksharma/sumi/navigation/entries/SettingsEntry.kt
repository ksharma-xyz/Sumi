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
import xyz.ksharma.sumi.navigation.WinRoute
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
        val showMistakes by vm.showMistakes.collectAsState()
        val highLegibility by vm.highLegibility.collectAsState()
        val isSimulatingPro by vm.isSimulatingPro.collectAsState()
        val isAdsEnabled by vm.isAdsEnabled.collectAsState()
        SettingsScreen(
            onBack = { navigator.pop() },
            onLicenses = { navigator.goTo(LicensesRoute) },
            themeState = ThemeState(
                season = currentSeason,
                hapticsEnabled = isHapticsEnabled,
                showMistakes = showMistakes,
                highLegibility = highLegibility,
            ),
            onSeasonSelect = { vm.setSeason(it) },
            onHapticsToggle = { vm.setHapticsEnabled(!isHapticsEnabled) },
            onShowMistakesToggle = { vm.setShowMistakes(!showMistakes) },
            onHighLegibilityToggle = { vm.setHighLegibility(!highLegibility) },
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
                    onSeedSampleStreak = { vm.seedSampleStreak() },
                    onOpenSampleWin = {
                        // Navigates to a sample Win screen with realistic stats + a real
                        // solved puzzle so designers / reviewers can preview the result
                        // page (and especially the share-image grid) on demand.
                        navigator.goTo(
                            WinRoute(
                                elapsedMs = 7L * 60_000L + 12L * 1_000L, // 7:12
                                mistakeCount = 2,
                                moveCount = 41,
                                difficulty = "Medium",
                                // A known-valid 9×9 solution, row-major. Lets the
                                // SudokuThumbnail render in the debug share preview.
                                solution = "534678912" +
                                    "672195348" +
                                    "198342567" +
                                    "859761423" +
                                    "426853791" +
                                    "713924856" +
                                    "961537284" +
                                    "287419635" +
                                    "345286179",
                            ),
                        )
                    },
                )
            } else null,
        )
    }
}
