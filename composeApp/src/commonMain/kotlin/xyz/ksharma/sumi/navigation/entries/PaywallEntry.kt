package xyz.ksharma.sumi.navigation.entries

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.navigation.PaywallRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.screens.paywall.PaywallScreen

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.PaywallEntry(navigator: SumiNavigator) {
    entry<PaywallRoute> {
        PaywallScreen(
            onBack = { navigator.pop() },
        )
    }
}
