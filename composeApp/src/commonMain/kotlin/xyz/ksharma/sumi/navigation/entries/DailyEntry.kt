package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.navigation.DailyRoute
import xyz.ksharma.sumi.screens.daily.DailyScreen

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.DailyEntry() {
    entry<DailyRoute> {
        DailyScreen()
    }
}
