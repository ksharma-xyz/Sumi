package xyz.ksharma.sumi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import xyz.ksharma.sumi.navigation.SumiNavHost
import xyz.ksharma.sumi.preferences.ThemePreferences
import xyz.ksharma.sumi.theme.SumiSeason
import xyz.ksharma.sumi.ui.theme.AppTheme

@Composable
fun App() {
    val themePrefs = koinInject<ThemePreferences>()
    val season by themePrefs.observeSeason().collectAsState(initial = SumiSeason.Spring)
    AppTheme(season = season) {
        SumiNavHost()
    }
}
