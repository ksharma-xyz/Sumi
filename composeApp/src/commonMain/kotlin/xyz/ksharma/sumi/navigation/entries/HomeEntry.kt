package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.FREE_QUOTES
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.SettingsRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.screens.home.HomeScreen
import xyz.ksharma.sumi.screens.home.HomeViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.HomeEntry(navigator: SumiNavigator) {
    entry<HomeRoute> {
        val vm: HomeViewModel = koinViewModel()
        val streak by vm.streak.collectAsState()

        @OptIn(ExperimentalTime::class)
        val dayOfYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfYear
        val quote = FREE_QUOTES[dayOfYear % FREE_QUOTES.size]

        HomeScreen(
            streakDays = streak,
            quote = quote,
            onStartGame = { difficulty -> navigator.goTo(GameRoute(difficulty = difficulty)) },
            onSettings = { navigator.goTo(SettingsRoute) },
        )
    }
}
