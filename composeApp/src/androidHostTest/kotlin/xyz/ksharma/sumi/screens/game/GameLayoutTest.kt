package xyz.ksharma.sumi.screens.game

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
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

    @Test
    fun duoInnerLandscapeIsSideBySideAndFitsTheWindow() {
        val layout = assertIs<GameLayout.SideBySide>(gameLayoutFor(width = 951.dp, height = 669.dp))
        assertEquals(MAX_BOARD_WIDTH, layout.boardSize) // 645dp of height, capped at 560dp
        assertTrue(layout.boardSize + SIDE_BOARD_MARGIN * 2 <= 669.dp)
        assertTrue(layout.panelWidth >= MIN_PANEL_WIDTH)
        assertTrue(layout.boardSize + SIDE_GAP + layout.panelWidth + SIDE_BOARD_MARGIN * 2 <= 951.dp)
    }

    @Test
    fun duoOuterLandscapeGivesWidthToThePanel() {
        val layout = assertIs<GameLayout.SideBySide>(gameLayoutFor(width = 628.dp, height = 450.dp))
        assertEquals(MIN_PANEL_WIDTH, layout.panelWidth)
        assertTrue(layout.boardSize >= MIN_SIDE_BOARD)
    }

    @Test
    fun boardIsCappedOnVeryLargeWindows() {
        val layout = assertIs<GameLayout.SideBySide>(gameLayoutFor(width = 1366.dp, height = 1024.dp))
        assertEquals(MAX_BOARD_WIDTH, layout.boardSize)
        assertEquals(MAX_PANEL_WIDTH, layout.panelWidth)
    }

    @Test
    fun tooShortWindowScrollsInsteadOfSqueezing() {
        assertEquals(GameLayout.Stacked, gameLayoutFor(width = 900.dp, height = 340.dp))
    }

    @Test
    fun tooNarrowForAPanelStaysStacked() {
        assertEquals(GameLayout.Stacked, gameLayoutFor(width = 520.dp, height = 400.dp))
    }
}
