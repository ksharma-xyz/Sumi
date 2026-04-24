package xyz.ksharma.sumi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import xyz.ksharma.sumi.theme.SumiSeason
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.rememberSumiFonts
import xyz.ksharma.sumi.theme.sumiTypography

@Composable
fun AppTheme(
    season: SumiSeason = SumiSeason.Spring,
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val fonts = rememberSumiFonts()
    val typography = sumiTypography(
        display = fonts.display,
        body = fonts.body,
        ui = fonts.ui,
        hand = fonts.hand,
        cjk = fonts.cjk,
    )
    SumiTheme(typography = typography, season = season, dark = dark, content = content)
}
