package xyz.ksharma.sumi.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import xyz.ksharma.sumi.navigation.entries.DailyEntry
import xyz.ksharma.sumi.navigation.entries.GameEntry
import xyz.ksharma.sumi.navigation.entries.GameOverEntry
import xyz.ksharma.sumi.navigation.entries.HomeEntry
import xyz.ksharma.sumi.navigation.entries.LicensesEntry
import xyz.ksharma.sumi.navigation.entries.OnboardingEntry
import xyz.ksharma.sumi.navigation.entries.PaywallEntry
import xyz.ksharma.sumi.navigation.entries.SettingsEntry
import xyz.ksharma.sumi.navigation.entries.SplashEntry
import xyz.ksharma.sumi.navigation.entries.StatsEntry
import xyz.ksharma.sumi.navigation.entries.WinEntry
import xyz.ksharma.sumi.navigation.entries.ZenEntry

@Composable
fun SumiNavHost(modifier: Modifier = Modifier) {
    val state = rememberSumiNavigationState()
    val navigator = rememberSumiNavigator(state)

    // On a non-Play tab (Daily/Stats/Zen) system back returns to Play rather than exiting the app.
    // The Play tab is rooted at SplashRoute — any other topLevelRoute is a secondary tab.
    TabBackHandler(enabled = state.topLevelRoute != SplashRoute) {
        navigator.switchTab(SplashRoute)
    }

    val entries: (NavKey) -> NavEntry<NavKey> = entryProvider {
        SplashEntry(navigator)
        OnboardingEntry(navigator)
        HomeEntry(navigator)
        DailyEntry(navigator)
        StatsEntry(navigator)
        GameEntry(navigator)
        WinEntry(navigator)
        GameOverEntry(navigator)
        ZenEntry(navigator)
        PaywallEntry(navigator)
        SettingsEntry(navigator)
        LicensesEntry(navigator)
    }

    // contentWindowInsets = 0 so Scaffold doesn't consume system bar insets.
    // Screens that need inset protection (title bars, bottom nav) handle it themselves.
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (state.showBottomNav) {
                BottomNavBar(
                    currentTab = state.topLevelRoute,
                    onTabClick = { navigator.switchTab(it) },
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            entries = state.toEntries(entries),
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            onBack = { navigator.pop() },
        )
    }
}
