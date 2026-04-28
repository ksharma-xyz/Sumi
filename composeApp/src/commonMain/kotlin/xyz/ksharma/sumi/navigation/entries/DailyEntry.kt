package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.navigation.DailyRoute
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.screens.daily.DailyScreen
import xyz.ksharma.sumi.screens.daily.DailyViewModel

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.DailyEntry(navigator: SumiNavigator) {
    entry<DailyRoute> {
        val vm: DailyViewModel = koinViewModel()
        val state by vm.state.collectAsState()

        DailyScreen(
            state = state,
            onBack = { navigator.pop() },
            onPlayToday = {
                navigator.goTo(GameRoute(difficulty = state.currentDifficulty.name))
            },
        )
    }
}
