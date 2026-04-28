package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.navigation.SplashRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.navigation.ZenRoute
import xyz.ksharma.sumi.preferences.ProRepository
import xyz.ksharma.sumi.screens.paywall.PaywallScreen
import xyz.ksharma.sumi.screens.zen.BookDesignerCallbacks
import xyz.ksharma.sumi.screens.zen.BookDesignerState
import xyz.ksharma.sumi.screens.zen.ProZenScreen
import xyz.ksharma.sumi.screens.zen.ZenViewModel

@Suppress("ComposableNaming")
@Composable
fun EntryProviderScope<NavKey>.ZenEntry(navigator: SumiNavigator) {
    entry<ZenRoute> {
        val proRepo = koinInject<ProRepository>()
        val isPro by proRepo.isPro().collectAsState(initial = false)
        val scope = rememberCoroutineScope()

        if (isPro) {
            val vm: ZenViewModel = koinViewModel()
            val quoteIndex by vm.quoteIndex.collectAsState()
            val bookDifficultyMix by vm.bookDifficultyMix.collectAsState()
            val bookTheme by vm.bookTheme.collectAsState()
            val bookIncludeAnswers by vm.bookIncludeAnswers.collectAsState()
            val bookGenState by vm.bookGenState.collectAsState()

            ProZenScreen(
                quotes = vm.quotes,
                quoteIndex = quoteIndex,
                designerState = BookDesignerState(
                    difficultyMix = bookDifficultyMix,
                    theme = bookTheme,
                    genState = bookGenState,
                    includeAnswers = bookIncludeAnswers,
                ),
                designerCallbacks = BookDesignerCallbacks(
                    onSetDifficultyMix = { vm.setBookDifficultyMix(it) },
                    onSetPaper = { vm.setBookPaper(it) },
                    onSetInk = { vm.setBookInk(it) },
                    onGenerate = { vm.generateBook() },
                    onClearError = { vm.clearError() },
                    onToggleAnswers = { vm.setBookIncludeAnswers(it) },
                    onShare = { vm.shareBook() },
                ),
                onPageChange = { vm.setQuoteIndex(it) },
                onRestorePurchase = { scope.launch { proRepo.restorePurchases() } },
                onDesignerVisibilityChange = { navigator.setDesignerOpen(it) },
            )
        } else {
            PaywallScreen(onBack = { navigator.switchTab(SplashRoute) })
        }
    }
}
