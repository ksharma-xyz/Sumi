@file:Suppress("MagicNumber") // sample puzzle data only

package xyz.ksharma.sumi.screens.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import xyz.ksharma.darpan.ScreenshotTest
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Cell
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.ui.preview.DUO_INNER_HEIGHT_DP
import xyz.ksharma.sumi.ui.preview.DUO_INNER_WIDTH_DP
import xyz.ksharma.sumi.ui.preview.DUO_OUTER_HEIGHT_DP
import xyz.ksharma.sumi.ui.preview.DUO_OUTER_WIDTH_DP
import xyz.ksharma.sumi.ui.preview.PreviewWindow

private val SOLUTION = listOf(
    listOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
    listOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
    listOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
    listOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
    listOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
    listOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
    listOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
    listOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
    listOf(3, 4, 5, 2, 8, 6, 1, 7, 9),
)

private fun previewState() = BoardState(
    cells = List(9) { r ->
        List(9) { c ->
            if ((r + c) % 2 == 0) Cell(value = SOLUTION[r][c], given = true) else Cell(value = 0, given = false)
        }
    },
    selected = 4 to 4,
    difficulty = Difficulty.entries.first(),
    solution = SOLUTION,
)

private val NO_CALLBACKS = GameCallbacks(
    onBack = {},
    onPause = {},
    onResume = {},
    onSelect = { _, _ -> },
    onEnter = {},
    onErase = {},
    onUndo = {},
    onRedo = {},
    onHint = {},
    onToggleNotes = {},
    onFillNotes = {},
    onNewPuzzle = {},
)

@Composable
private fun GameScreenAtWindow(widthDp: Int, heightDp: Int) {
    PreviewWindow(widthDp = widthDp, heightDp = heightDp) {
        GameScreen(
            state = previewState(),
            elapsedMs = 83_000L,
            celebrationCount = 0,
            gridCelebrationCount = 0,
            isInitializing = false,
            paused = false,
            difficulty = Difficulty.entries.first(),
            callbacks = NO_CALLBACKS,
        )
    }
}

// The harness captures one frame per preview at a fixed size, so each window size is its own
// preview, drawn at its true dp size and scaled down by PreviewWindow.

/** Phone portrait: the stacked layout, unchanged. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "1. Phone portrait", group = "Window", showBackground = true)
@Composable
internal fun GameScreenPhonePortraitPreview() = GameScreenAtWindow(411, 891)

/** Duo outer display, portrait: stacked. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "2. Duo outer portrait", group = "Window", showBackground = true)
@Composable
internal fun GameScreenDuoOuterPortraitPreview() = GameScreenAtWindow(DUO_OUTER_WIDTH_DP, DUO_OUTER_HEIGHT_DP)

/** Duo outer display held sideways: side by side with the narrowest panel. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "3. Duo outer landscape", group = "Window", showBackground = true)
@Composable
internal fun GameScreenDuoOuterLandscapePreview() = GameScreenAtWindow(DUO_OUTER_HEIGHT_DP, DUO_OUTER_WIDTH_DP)

/** Duo inner display, portrait: stacked, board at its 560dp cap. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "4. Duo inner portrait", group = "Window", showBackground = true)
@Composable
internal fun GameScreenDuoInnerPortraitPreview() = GameScreenAtWindow(DUO_INNER_WIDTH_DP, DUO_INNER_HEIGHT_DP)

/** Duo inner display, landscape: board on the left at its cap, controls on the right. */
@ScreenshotTest(fontScaleSensitive = false)
@Preview(name = "5. Duo inner landscape", group = "Window", showBackground = true)
@Composable
internal fun GameScreenDuoInnerLandscapePreview() = GameScreenAtWindow(DUO_INNER_HEIGHT_DP, DUO_INNER_WIDTH_DP)
