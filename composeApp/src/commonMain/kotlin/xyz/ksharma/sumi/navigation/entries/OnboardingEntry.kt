package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.OnboardingRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.preferences.SumiPreferences
import xyz.ksharma.sumi.preferences.ThemePreferences
import xyz.ksharma.sumi.screens.onboarding.OnboardingScreen

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.OnboardingEntry(navigator: SumiNavigator) {
    entry<OnboardingRoute> {
        val prefs = koinInject<SumiPreferences>()
        val themePrefs = koinInject<ThemePreferences>()
        val scope = rememberCoroutineScope()
        OnboardingScreen(
            onComplete = { season ->
                scope.launch {
                    themePrefs.setSeason(season)
                    prefs.setSeenOnboarding()
                }
                navigator.goTo(HomeRoute)
            },
        )
    }
}
