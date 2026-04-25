package xyz.ksharma.sumi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.lexilabs.basic.ads.BasicAds
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import org.koin.compose.koinInject
import xyz.ksharma.sumi.navigation.SumiNavHost
import xyz.ksharma.sumi.preferences.ThemePreferences
import xyz.ksharma.sumi.theme.SumiSeason
import xyz.ksharma.sumi.ui.theme.AppTheme

@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun App() {
    BasicAds.Initialize()
    val themePrefs = koinInject<ThemePreferences>()
    val season by themePrefs.observeSeason().collectAsState(initial = SumiSeason.Spring)
    AppTheme(season = season) {
        SumiNavHost()
    }
}
