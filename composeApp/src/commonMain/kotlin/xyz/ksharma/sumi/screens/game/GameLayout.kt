package xyz.ksharma.sumi.screens.game

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Cap board width on tablets so the grid scales up without dominating wide screens. */
internal val MAX_BOARD_WIDTH = 560.dp

/** Gap between the board and the control panel in the side-by-side layout. */
internal val SIDE_GAP = 24.dp

/** Breathing room around the board and panel in the side-by-side layout. */
internal val SIDE_BOARD_MARGIN = 12.dp

/** Narrowest control panel that still gives the number pad comfortable tap targets. */
internal val MIN_PANEL_WIDTH = 280.dp

/** Widest control panel; beyond this the extra width goes to the margins. */
internal val MAX_PANEL_WIDTH = 400.dp

/** Smallest board worth drawing beside a panel; below this the stacked layout is used. */
internal val MIN_SIDE_BOARD = 260.dp

/** Height of the top bar (back, difficulty + timer, pause) that spans board and panel. */
internal val SIDE_TOP_BAR_HEIGHT = 48.dp

/** Gap between the top bar and the board / panel row beneath it. */
internal val SIDE_TOP_BAR_GAP = 12.dp

/** Vertical space the top bar and its gap take out of the side-by-side size budget. */
internal val SIDE_TOP_BAR_BLOCK = SIDE_TOP_BAR_HEIGHT + SIDE_TOP_BAR_GAP

/**
 * Shortest window the side-by-side unit (top bar, then a board with the marks row, tools row and
 * a 3x3 number pad beside it) fits in without scrolling.
 */
internal val MIN_SIDE_HEIGHT = 360.dp

/** Keypad digit size as a fraction of the keypad cell height, and its clamp in sp. */
internal const val PAD_DIGIT_RATIO = 0.38f
internal const val PAD_DIGIT_MIN_SP = 26f
internal const val PAD_DIGIT_MAX_SP = 44f

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
     * One centred unit: a top bar across the whole width, and below it the board on the left and
     * a panel of exactly the board's height on the right; nothing scrolls.
     */
    data class SideBySide(val boardSize: Dp, val panelWidth: Dp) : GameLayout {
        /** Width of the unit, which is also the width of the top bar above it. */
        val unitWidth: Dp get() = boardSize + SIDE_GAP + panelWidth
    }
}

/**
 * Picks the layout from the window alone, never from the device type.
 *
 * Side by side needs a window wider than tall, enough height for the controls to fit without
 * scrolling, and enough width left for a usable panel after the board has taken the height left
 * under the top bar.
 * Everything else keeps the stacked, scrolling layout.
 *
 * @param width window width after side insets
 * @param height window height after the status bar, corner clearance and reserved banner space
 */
internal fun gameLayoutFor(width: Dp, height: Dp): GameLayout {
    if (width <= height || height < MIN_SIDE_HEIGHT) return GameLayout.Stacked
    val usableWidth = width - SIDE_BOARD_MARGIN * 2
    val boardSize = minOf(
        height - SIDE_BOARD_MARGIN * 2 - SIDE_TOP_BAR_BLOCK,
        MAX_BOARD_WIDTH,
        usableWidth - MIN_PANEL_WIDTH - SIDE_GAP,
    )
    if (boardSize < MIN_SIDE_BOARD) return GameLayout.Stacked
    val panelWidth = minOf(usableWidth - boardSize - SIDE_GAP, MAX_PANEL_WIDTH)
    return GameLayout.SideBySide(boardSize = boardSize, panelWidth = panelWidth)
}

/**
 * Whether the stacked layout can fit the window without scrolling by sizing the board to the
 * height the controls leave. False on short windows (phones on their side), which scroll.
 *
 * @param height window height after the status bar, corner clearance and reserved banner space
 */
internal fun stackedFitsHeight(height: Dp): Boolean = height - STACKED_CONTROLS_HEIGHT >= MIN_STACKED_BOARD
