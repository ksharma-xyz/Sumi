package xyz.ksharma.sumi.screens.game

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GameLayoutTest {

    @Test
    fun portraitWindowsStayStacked() {
        assertEquals(GameLayout.Stacked, gameLayoutFor(width = 411.dp, height = 800.dp))
        assertEquals(GameLayout.Stacked, gameLayoutFor(width = 669.dp, height = 951.dp))
    }

    @Test
    fun squareWindowStaysStacked() {
        assertEquals(GameLayout.Stacked, gameLayoutFor(width = 600.dp, height = 600.dp))
    }

    private fun sideBySide(width: Int, height: Int) =
        assertIs<GameLayout.SideBySide>(gameLayoutFor(width = width.dp, height = height.dp))

    private fun assertDp(expected: Float, actual: androidx.compose.ui.unit.Dp) =
        assertTrue(kotlin.math.abs(expected - actual.value) < 0.01f, "expected $expected but was ${actual.value}")

    @Test
    fun panelIsSixCellsWideAndBoardNine() {
        val layout = sideBySide(1280, 800)
        assertEquals(layout.cell * 9, layout.boardSize)
        assertEquals(layout.cell * 6, layout.panelWidth)
        assertEquals(layout.boardSize + layout.gap + layout.panelWidth, layout.unitWidth)
    }

    @Test
    fun tabletLandscapeFillsTheHeightUpToTheBoardCap() {
        val layout = sideBySide(1280, 800)
        assertDp(640f / 9, layout.cell) // height would allow 79.6dp; the 640dp board cap wins
        assertEquals(MAX_SIDE_GAP, layout.gap)
        assertTrue(layout.boardSize + SIDE_TOP_BAR_BLOCK + SIDE_BOARD_MARGIN * 2 <= 800.dp)
    }

    @Test
    fun duoInnerLandscapeIsWidthBound() {
        val layout = sideBySide(951, 669)
        assertDp((951f - 24 - 24) / 15, layout.cell)
        assertTrue(layout.unitWidth + SIDE_BOARD_MARGIN * 2 <= 951.dp)
    }

    @Test
    fun duoOuterLandscapeIsSideBySideWithAHalfCellGap() {
        val layout = sideBySide(678, 466)
        val fromHeight = (466f - 24 - 60) / 9
        assertDp((678f - 24 - fromHeight / 2) / 15, layout.cell)
        assertDp(fromHeight / 2, layout.gap)
        assertTrue(layout.cell >= MIN_SIDE_CELL)
    }

    @Test
    fun cellBelowTheMinimumFallsBackToStacked() {
        // 340dp of height leaves a 28dp cell.
        assertEquals(GameLayout.Stacked, gameLayoutFor(width = 900.dp, height = 340.dp))
    }

    @Test
    fun tooNarrowForFifteenCellsStaysStacked() {
        assertEquals(GameLayout.Stacked, gameLayoutFor(width = 520.dp, height = 400.dp))
    }

    @Test
    fun duoOuterPortraitFitsWithoutScrolling() {
        // 678dp tall, minus the corner clearance and the banner reserve.
        assertTrue(stackedFitsHeight(678.dp - 16.dp - 64.dp))
    }

    @Test
    fun phoneOnItsSideScrolls() {
        assertFalse(stackedFitsHeight(402.dp - 64.dp))
    }
}
