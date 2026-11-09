package xyz.ksharma.sumi.screens.game

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Cap board width on tablets so the grid scales up without dominating wide screens. */
internal val MAX_BOARD_WIDTH = 560.dp

/** Widest board (9 cells) the side-by-side layout draws; the stacked layout keeps [MAX_BOARD_WIDTH]. */
internal val SIDE_MAX_BOARD = 640.dp

/** Smallest board cell the side-by-side layout accepts; below it the stacked layout is used. */
internal val MIN_SIDE_CELL = 32.dp

/** The panel is this many board cells wide and the board is [BOARD_CELLS] cells wide and tall. */
internal const val PANEL_CELLS = 6
internal const val BOARD_CELLS = 9

/** Gap between board and panel is half a cell, clamped to this range. */
internal val MIN_SIDE_GAP = 16.dp
internal val MAX_SIDE_GAP = 24.dp

/** Breathing room around the board and panel in the side-by-side layout. */
internal val SIDE_BOARD_MARGIN = 12.dp

/** Height of the top bar (back, difficulty + timer, pause) that spans board and panel. */
internal val SIDE_TOP_BAR_HEIGHT = 48.dp

/** Gap between the top bar and the board / panel row beneath it. */
internal val SIDE_TOP_BAR_GAP = 12.dp

/** Vertical space the top bar and its gap take out of the side-by-side size budget. */
internal val SIDE_TOP_BAR_BLOCK = SIDE_TOP_BAR_HEIGHT + SIDE_TOP_BAR_GAP

/**
 * Height the stacked layout's controls take besides the board: top bar, marks row, tools row,
 * number pad and the gaps between them, at the default font scale.
 */
internal val STACKED_CONTROLS_HEIGHT = 300.dp

/** Smallest board the stacked layout shrinks to before it scrolls instead. */
internal val MIN_STACKED_BOARD = 270.dp

/** How the game screen arranges the board and its controls. */
internal sealed interface GameLayout {
    /** Top bar, marks, board, tools and number pad in one vertical scroll (portrait). */
    data object Stacked : GameLayout

    /**
     * One centred unit on a grid of board cells of size [cell]: a top bar across the whole width,
     * and below it the board (9 x 9 cells) and a panel of 6 x 9 cells on the right, [gap] apart.
     * Nothing scrolls.
     */
    data class SideBySide(val cell: Dp, val gap: Dp) : GameLayout {
        val boardSize: Dp get() = cell * BOARD_CELLS
        val panelWidth: Dp get() = cell * PANEL_CELLS

        /** Width of the unit, which is also the width of the top bar above it. */
        val unitWidth: Dp get() = boardSize + gap + panelWidth
    }
}

/**
 * Picks the layout from the window alone, never from the device type.
 *
 * Side by side needs a window wider than tall and a board cell of at least [MIN_SIDE_CELL] once
 * the top bar and margins are taken off the height and the 15 cells (board + panel) plus gap are
 * fitted into the width. Everything else keeps the stacked, scrolling layout.
 *
 * @param width window width after side insets
 * @param height window height after the status bar, corner clearance and reserved banner space
 */
internal fun gameLayoutFor(width: Dp, height: Dp): GameLayout {
    if (width <= height) return GameLayout.Stacked
    val fromHeight = minOf(
        (height - SIDE_BOARD_MARGIN * 2 - SIDE_TOP_BAR_BLOCK) / BOARD_CELLS,
        SIDE_MAX_BOARD / BOARD_CELLS,
    )
    val gap = (fromHeight / 2).coerceIn(MIN_SIDE_GAP, MAX_SIDE_GAP)
    val fromWidth = (width - SIDE_BOARD_MARGIN * 2 - gap) / (BOARD_CELLS + PANEL_CELLS)
    val cell = minOf(fromHeight, fromWidth)
    if (cell < MIN_SIDE_CELL) return GameLayout.Stacked
    return GameLayout.SideBySide(cell = cell, gap = gap)
}

/**
 * Whether the stacked layout can fit the window without scrolling by sizing the board to the
 * height the controls leave. False on short windows (phones on their side), which scroll.
 *
 * @param height window height after the status bar, corner clearance and reserved banner space
 */
internal fun stackedFitsHeight(height: Dp): Boolean = height - STACKED_CONTROLS_HEIGHT >= MIN_STACKED_BOARD
