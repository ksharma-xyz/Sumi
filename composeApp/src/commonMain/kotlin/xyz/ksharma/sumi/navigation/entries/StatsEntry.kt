package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.navigation.StatsRoute
import xyz.ksharma.sumi.screens.stats.StatsScreen

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.StatsEntry() {
    entry<StatsRoute> {
        StatsScreen()
    }
}
