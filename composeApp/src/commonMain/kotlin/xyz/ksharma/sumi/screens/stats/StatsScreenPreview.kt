@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.stats

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import xyz.ksharma.darpan.ScreenshotTest
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.navigation.SplashRoute
import xyz.ksharma.sumi.navigation.SumiNavShell
import xyz.ksharma.sumi.ui.preview.PreviewWindow
import xyz.ksharma.sumi.ui.preview.TABLET_LONG_DP
import xyz.ksharma.sumi.ui.preview.TABLET_SHORT_DP

private fun previewStats() = StatsState(
    totalPuzzlesSolved = 48,
    currentStreak = 5,
    bestStreak = 9,
    daysPlayed = 21,
    solvedThisWeek = 6,
    bestTimes = Difficulty.entries.withIndex().associate { (i, d) -> d.name to (150_000L + i * 120_000L) },
    recentSolveTimes = List(30) { 420_000L - it * 6_000L + (it % 4) * 15_000L },
    lastDifficulty = Difficulty.entries.first().name,
    isPro = true,
)

@Composable
private fun StatsAtWindow(widthDp: Int, heightDp: Int) {
    PreviewWindow(widthDp = widthDp, heightDp = heightDp) {
        SumiNavShell(showNav = true, currentTab = SplashRoute, onTabClick = {}) { contentModifier ->
            StatsScreen(
                state = previewStats(),
                onBack = {},
                onUnlockPro = {},
                onShare = {},
                modifier = contentModifier,
            )
        }
    }
}

/** Tablet landscape: navigation rail, two columns that share top and bottom edges. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "1. Tablet landscape", group = "Window", showBackground = true)
@Composable
internal fun StatsScreenTabletLandscapePreview() = StatsAtWindow(TABLET_LONG_DP, TABLET_SHORT_DP)
