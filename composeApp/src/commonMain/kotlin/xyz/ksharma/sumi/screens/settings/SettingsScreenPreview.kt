package xyz.ksharma.sumi.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import xyz.ksharma.darpan.ScreenshotTest
import xyz.ksharma.sumi.navigation.SplashRoute
import xyz.ksharma.sumi.navigation.SumiNavShell
import xyz.ksharma.sumi.theme.SumiSeason
import xyz.ksharma.sumi.ui.preview.PreviewWindow
import xyz.ksharma.sumi.ui.preview.TABLET_LONG_DP
import xyz.ksharma.sumi.ui.preview.TABLET_SHORT_DP

@Composable
private fun SettingsAtWindow(widthDp: Int, heightDp: Int) {
    PreviewWindow(widthDp = widthDp, heightDp = heightDp) {
        SumiNavShell(showNav = true, currentTab = SplashRoute, onTabClick = {}) { contentModifier ->
            SettingsScreen(
                onBack = {},
                onLicenses = {},
                themeState = ThemeState(
                    season = SumiSeason.entries.first(),
                    hapticsEnabled = true,
                    showMistakes = true,
                    highLegibility = false,
                    strictConflicts = false,
                    digitFirstInput = false,
                    livesMode = false,
                ),
                onSeasonSelect = {},
                onHapticsToggle = {},
                onShowMistakesToggle = {},
                onHighLegibilityToggle = {},
                onStrictConflictsToggle = {},
                onDigitFirstToggle = {},
                onLivesModeToggle = {},
                debugCallbacks = null,
                modifier = contentModifier,
            )
        }
    }
}

/** Tablet landscape: navigation rail, Theme on the left, Play and About on the right. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "1. Tablet landscape", group = "Window", showBackground = true)
@Composable
internal fun SettingsScreenTabletLandscapePreview() = SettingsAtWindow(TABLET_LONG_DP, TABLET_SHORT_DP)
