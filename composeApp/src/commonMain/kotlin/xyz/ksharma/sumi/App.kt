package xyz.ksharma.sumi

import androidx.compose.runtime.Composable
import xyz.ksharma.sumi.navigation.SumiNavHost
import xyz.ksharma.sumi.ui.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        SumiNavHost()
    }
}
