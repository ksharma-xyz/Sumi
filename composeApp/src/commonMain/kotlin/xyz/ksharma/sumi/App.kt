package xyz.ksharma.sumi

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import xyz.ksharma.sumi.navigation.SumiNavHost
import xyz.ksharma.sumi.preferences.SumiPreferences
import xyz.ksharma.sumi.preferences.ThemeMode
import xyz.ksharma.sumi.ui.theme.AppTheme

@Composable
fun App() {
    val prefs = koinInject<SumiPreferences>()
    val themeMode by prefs.themeMode().collectAsState(ThemeMode.FollowSystem)
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.FollowSystem -> systemDark
        ThemeMode.AlwaysLight -> false
        ThemeMode.AlwaysDark -> true
    }
    AppTheme(dark = dark) {
        SumiNavHost()
    }
}
