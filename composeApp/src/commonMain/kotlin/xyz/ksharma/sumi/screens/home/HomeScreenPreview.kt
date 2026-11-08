package xyz.ksharma.sumi.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import xyz.ksharma.darpan.ScreenshotTest
import xyz.ksharma.sumi.Quote
import xyz.ksharma.sumi.navigation.SplashRoute
import xyz.ksharma.sumi.navigation.SumiNavShell
import xyz.ksharma.sumi.ui.preview.DUO_INNER_HEIGHT_DP
import xyz.ksharma.sumi.ui.preview.DUO_INNER_WIDTH_DP
import xyz.ksharma.sumi.ui.preview.PreviewWindow
import xyz.ksharma.sumi.ui.preview.TABLET_LONG_DP
import xyz.ksharma.sumi.ui.preview.TABLET_SHORT_DP

/** Home inside the same tab chrome the app uses: bottom bar when compact, rail when expanded. */
@Composable
private fun HomeAtWindow(widthDp: Int, heightDp: Int) {
    PreviewWindow(widthDp = widthDp, heightDp = heightDp) {
        SumiNavShell(
            showNav = true,
            currentTab = SplashRoute,
            onTabClick = {},
        ) { contentModifier ->
            HomeScreen(
                streakDays = 5,
                quote = Quote("Simplicity is the ultimate sophistication.", "Leonardo da Vinci"),
                onStartGame = {},
                onSettings = {},
                onInvite = {},
                isPro = false,
                modifier = contentModifier,
                lockedDifficulties = setOf("Master", "Edo"),
            )
        }
    }
}

/** Tablet landscape: navigation rail, two top-aligned panes. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "1. Tablet landscape", group = "Window", showBackground = true)
@Composable
internal fun HomeScreenTabletLandscapePreview() = HomeAtWindow(TABLET_LONG_DP, TABLET_SHORT_DP)

/** Tablet portrait: bottom bar, single column. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "2. Tablet portrait", group = "Window", showBackground = true)
@Composable
internal fun HomeScreenTabletPortraitPreview() = HomeAtWindow(TABLET_SHORT_DP, TABLET_LONG_DP)

/** Duo inner display, landscape: rail and two panes. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "3. Duo inner landscape", group = "Window", showBackground = true)
@Composable
internal fun HomeScreenDuoInnerLandscapePreview() = HomeAtWindow(DUO_INNER_HEIGHT_DP, DUO_INNER_WIDTH_DP)

/** Duo inner display, portrait: bottom bar, single column. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "4. Duo inner portrait", group = "Window", showBackground = true)
@Composable
internal fun HomeScreenDuoInnerPortraitPreview() = HomeAtWindow(DUO_INNER_WIDTH_DP, DUO_INNER_HEIGHT_DP)
