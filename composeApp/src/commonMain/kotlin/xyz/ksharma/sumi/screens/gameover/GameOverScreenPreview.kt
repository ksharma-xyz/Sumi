package xyz.ksharma.sumi.screens.gameover

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import xyz.ksharma.darpan.ScreenshotTest
import xyz.ksharma.sumi.navigation.SplashRoute
import xyz.ksharma.sumi.navigation.SumiNavShell
import xyz.ksharma.sumi.ui.preview.DUO_INNER_HEIGHT_DP
import xyz.ksharma.sumi.ui.preview.DUO_INNER_WIDTH_DP
import xyz.ksharma.sumi.ui.preview.PreviewWindow
import xyz.ksharma.sumi.ui.preview.TABLET_LONG_DP
import xyz.ksharma.sumi.ui.preview.TABLET_SHORT_DP

@Composable
private fun GameOverAtWindow(widthDp: Int, heightDp: Int) {
    PreviewWindow(widthDp = widthDp, heightDp = heightDp) {
        SumiNavShell(showNav = false, currentTab = SplashRoute, onTabClick = {}) { contentModifier ->
            GameOverScreen(
                difficulty = "Medium",
                elapsedLabel = "02:41",
                onRetry = {},
                onHome = {},
                modifier = contentModifier,
            )
        }
    }
}

/** Duo inner display, landscape: centred, buttons capped instead of full width. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "1. Duo inner landscape", group = "Window", showBackground = true)
@Composable
internal fun GameOverScreenDuoInnerLandscapePreview() =
    GameOverAtWindow(DUO_INNER_HEIGHT_DP, DUO_INNER_WIDTH_DP)

/** Tablet landscape. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "2. Tablet landscape", group = "Window", showBackground = true)
@Composable
internal fun GameOverScreenTabletLandscapePreview() = GameOverAtWindow(TABLET_LONG_DP, TABLET_SHORT_DP)
