package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.BannerAd
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.FREE_QUOTES
import xyz.ksharma.sumi.ads.AdUnits
import xyz.ksharma.sumi.coroutines.ext.launchWithExceptionHandler
import xyz.ksharma.sumi.navigation.GameRoute
import xyz.ksharma.sumi.navigation.HomeRoute
import xyz.ksharma.sumi.navigation.PaywallRoute
import xyz.ksharma.sumi.navigation.SettingsRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.preferences.DebugPreferences
import xyz.ksharma.sumi.preferences.ProRepository
import xyz.ksharma.sumi.screens.home.HomeScreen
import xyz.ksharma.sumi.screens.home.HomeViewModel
import xyz.ksharma.sumi.share.ShareManager
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val PRO_DIFFICULTIES = setOf("Hard", "Master", "Edo")

// TODO: replace with the real App Store / Play Store listing URLs once the app
// is published. Until then we share a stand-in landing page.
private const val INVITE_TEXT =
    "I've been finding quiet in this Sudoku app — Sumi. " +
        "Daily puzzles, no noise. https://sumi.app"

@Suppress("ComposableNaming")
@OptIn(DependsOnGoogleMobileAds::class)
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.HomeEntry(navigator: SumiNavigator) {
    entry<HomeRoute> {
        val vm: HomeViewModel = koinViewModel()
        val streak by vm.streak.collectAsState()
        val proRepo = koinInject<ProRepository>()
        val debugPrefs = koinInject<DebugPreferences>()
        val shareManager = koinInject<ShareManager>()
        val scope = rememberCoroutineScope()
        val isPro by proRepo.isPro().collectAsState(initial = false)
        val isAdsEnabled by debugPrefs.observeAdsEnabled().collectAsState(initial = true)

        @OptIn(ExperimentalTime::class)
        val dayOfYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfYear
        val quote = FREE_QUOTES[dayOfYear % FREE_QUOTES.size]

        val showBanner = !isPro && isAdsEnabled

        HomeScreen(
            streakDays = streak,
            quote = quote,
            onStartGame = { difficulty ->
                if (difficulty in PRO_DIFFICULTIES && !isPro) navigator.goTo(PaywallRoute)
                else navigator.goTo(GameRoute(difficulty = difficulty))
            },
            onSettings = { navigator.goTo(SettingsRoute) },
            onInvite = {
                scope.launchWithExceptionHandler<ShareManager>(Dispatchers.Default) {
                    shareManager.shareText(text = INVITE_TEXT, title = "Pass Sumi along")
                }
            },
            lockedDifficulties = if (isPro) emptySet() else PRO_DIFFICULTIES,
            bottomBanner = if (showBanner) {
                @Composable { BannerAd(adUnitId = AdUnits.Banner) }
            } else null,
        )
    }
}
