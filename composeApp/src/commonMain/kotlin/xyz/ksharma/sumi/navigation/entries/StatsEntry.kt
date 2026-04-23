package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.navigation.StatsRoute
import xyz.ksharma.sumi.screens.stats.StatsScreen
import xyz.ksharma.sumi.screens.stats.StatsViewModel

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.StatsEntry() {
    entry<StatsRoute> {
        val vm: StatsViewModel = koinViewModel()
        val state by vm.state.collectAsState()
        StatsScreen(state = state)
    }
}
