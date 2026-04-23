package xyz.ksharma.sumi.navigation.entries

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.OnboardingRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.screens.onboarding.OnboardingScreen

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.OnboardingEntry(navigator: SumiNavigator) {
    entry<OnboardingRoute> {
        OnboardingScreen(
            onComplete = { navigator.goTo(HomeRoute) },
        )
    }
}
