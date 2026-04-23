package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.navigation.DailyRoute
import xyz.ksharma.sumi.screens.daily.DailyScreen
import xyz.ksharma.sumi.screens.daily.DailyViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.DailyEntry() {
    entry<DailyRoute> {
        val vm: DailyViewModel = koinViewModel()
        val solveDays by vm.solveDays.collectAsState()
        val streak by vm.streak.collectAsState()

        @OptIn(ExperimentalTime::class)
        val todayEpoch = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date.toEpochDays()

        DailyScreen(
            solveDays = solveDays,
            streak = streak,
            todayEpoch = todayEpoch,
        )
    }
}
