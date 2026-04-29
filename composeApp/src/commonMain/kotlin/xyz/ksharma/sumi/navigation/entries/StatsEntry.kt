package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.Dispatchers
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.AppLinks
import xyz.ksharma.sumi.coroutines.ext.launchWithExceptionHandler
import xyz.ksharma.sumi.navigation.PaywallRoute
import xyz.ksharma.sumi.navigation.StatsRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.screens.stats.StatsScreen
import xyz.ksharma.sumi.screens.stats.StatsViewModel
import xyz.ksharma.sumi.share.ShareManager

private val SHARE_TEXT = buildString {
    append("My Sumi practice log — quiet sudoku, daily.\n\n")
    append("iOS: ").append(AppLinks.APP_STORE_URL).append('\n')
    append("Android: ").append(AppLinks.PLAY_STORE_URL)
}

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.StatsEntry(navigator: SumiNavigator) {
    entry<StatsRoute> {
        val vm: StatsViewModel = koinViewModel()
        val state by vm.state.collectAsState()
        val shareManager = koinInject<ShareManager>()
        val scope = rememberCoroutineScope()

        StatsScreen(
            state = state,
            onBack = { navigator.pop() },
            onUnlockPro = { navigator.goTo(PaywallRoute) },
            onShare = { bitmap ->
                scope.launchWithExceptionHandler<ShareManager>(Dispatchers.Default) {
                    shareManager.shareImage(
                        bitmap = bitmap,
                        title = "My Sumi practice log",
                        text = SHARE_TEXT,
                    )
                }
            },
        )
    }
}
