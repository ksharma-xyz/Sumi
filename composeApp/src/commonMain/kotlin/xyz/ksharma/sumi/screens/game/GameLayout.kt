package xyz.ksharma.sumi.screens.game

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Cap on the stacked layout's column on tablets, so the board scales up without dominating wide
 * screens. The top bar, marks, tools and number pad share the board's width.
 */
internal val MAX_BOARD_WIDTH = 720.dp

/** Widest board (9 cells) the side-by-side layout draws; the stacked layout caps at [MAX_BOARD_WIDTH]. */
internal val SIDE_MAX_BOARD = 640.dp

/** Smallest board cell the side-by-side layout accepts; below it the stacked layout is used. */
internal val MIN_SIDE_CELL = 32.dp

/** The panel is this many board cells wide and the board is [BOARD_CELLS] cells wide and tall. */
internal const val PANEL_CELLS = 6
internal const val BOARD_CELLS = 9

/** Gap between board and panel. */
internal val SIDE_GAP = 20.dp

/** Equal left and right gutter of the side-by-side unit, measured inside the safe area. */
internal val SIDE_MARGIN = 28.dp

/** Minimum space above and below the unit; the unit is centred vertically in what is left. */
internal val SIDE_VERTICAL_MARGIN = 12.dp

/** Panel rows, in board cells: header, marks and hints, tools. The keypad takes the other six. */
internal const val PANEL_HEADER_CELLS = 1f
internal const val PANEL_MARKS_CELLS = 0.75f
internal const val PANEL_TOOLS_CELLS = 1.25f
internal const val PANEL_KEYPAD_CELLS = 6

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
     * One centred unit on a grid of board cells of size [cell]: the board (9 x 9 cells) and a
     * panel of 6 x 9 cells on the right, [SIDE_GAP] apart. Board and panel share their top and
     * bottom edges. Nothing scrolls.
     */
    data class SideBySide(val cell: Dp) : GameLayout {
        val gap: Dp get() = SIDE_GAP
        val boardSize: Dp get() = cell * BOARD_CELLS
        val panelWidth: Dp get() = cell * PANEL_CELLS

        /** Width of the unit: board, gap and panel. */
        val unitWidth: Dp get() = boardSize + gap + panelWidth
    }
}

/**
 * Picks the layout from the window alone, never from the device type.
 *
 * Side by side needs a window wider than tall and a board cell of at least [MIN_SIDE_CELL] once
 * the vertical margins are taken off the height and the 15 cells (board + panel), the gap and the
 * two equal side gutters are fitted into the width. Everything else keeps the stacked, scrolling
 * layout.
 *
 * @param width window width after side insets
 * @param height window height after the status bar, corner clearance and reserved banner space
 */
internal fun gameLayoutFor(width: Dp, height: Dp): GameLayout {
    if (width <= height) return GameLayout.Stacked
    val fromHeight = minOf(
        (height - SIDE_VERTICAL_MARGIN * 2) / BOARD_CELLS,
        SIDE_MAX_BOARD / BOARD_CELLS,
    )
    val fromWidth = (width - SIDE_MARGIN * 2 - SIDE_GAP) / (BOARD_CELLS + PANEL_CELLS)
    val cell = minOf(fromHeight, fromWidth)
    if (cell < MIN_SIDE_CELL) return GameLayout.Stacked
    return GameLayout.SideBySide(cell = cell)
}

/**
 * Whether the stacked layout can fit the window without scrolling by sizing the board to the
 * height the controls leave. False on short windows (phones on their side), which scroll.
 *
 * @param height window height after the status bar, corner clearance and reserved banner space
 */
internal fun stackedFitsHeight(height: Dp): Boolean = height - STACKED_CONTROLS_HEIGHT >= MIN_STACKED_BOARD
