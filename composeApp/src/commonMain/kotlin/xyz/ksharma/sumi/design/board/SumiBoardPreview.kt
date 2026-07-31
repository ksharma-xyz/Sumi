@file:Suppress("MagicNumber") // sample puzzle data + layout maths only

package xyz.ksharma.sumi.design.board

import androidx.compose.runtime.Composable
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Cell
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.ui.preview.AppPreviewTheme
import xyz.ksharma.sumi.ui.preview.PreviewComponent
import xyz.ksharma.sumi.ui.preview.ScreenshotTest

// A solved grid drives the previews so error/teal states can be derived from it.
private val PREVIEW_SOLUTION = listOf(
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

private fun boardOf(
    selected: Pair<Int, Int>? = null,
    cell: (r: Int, c: Int) -> Cell,
) = BoardState(
    cells = List(9) { r -> List(9) { c -> cell(r, c) } },
    selected = selected,
    difficulty = Difficulty.entries.first(),
    solution = PREVIEW_SOLUTION,
)

private fun empty() = Cell(value = 0, given = false)

private fun given(r: Int, c: Int) = Cell(value = PREVIEW_SOLUTION[r][c], given = true)

/**
 * Representative half-filled board exercising every cell state at once: given digits (ink),
 * a correct user entry (teal), a wrong entry (error red), empty cells, pencil marks, and a
 * selected cell driving the row/column/box highlight.
 */
private fun previewBoard(): BoardState = boardOf(selected = 4 to 4) { r, c ->
    when {
        r == 0 && c == 1 -> Cell(value = 9, given = false) // wrong (solution is 3) -> error red
        r == 0 && c == 3 -> Cell(value = PREVIEW_SOLUTION[r][c], given = false) // correct -> teal
        r == 2 && c == 5 -> Cell(value = PREVIEW_SOLUTION[r][c], given = false) // correct -> teal
        r == 1 && c == 2 -> Cell(value = 0, given = false, notes = setOf(2, 5, 8))
        r == 4 && c == 4 -> Cell(value = 0, given = false, notes = setOf(1, 3, 5, 7, 9))
        (r + c) % 2 == 0 -> given(r, c)
        else -> empty()
    }
}

@ScreenshotTest(fontScaleSensitive = false)
@PreviewComponent
@Composable
internal fun SumiBoardPreview() {
    AppPreviewTheme {
        SumiBoard(state = previewBoard())
    }
}

/** Fresh puzzle: givens only, nothing entered, nothing selected. */
@ScreenshotTest(fontScaleSensitive = false)
@PreviewComponent
@Composable
internal fun SumiBoardEmptyPreview() {
    AppPreviewTheme {
        SumiBoard(
            state = boardOf { r, c -> if ((r + c) % 3 == 0) given(r, c) else empty() },
        )
    }
}

/**
 * Worst case for pencil-mark legibility: every empty cell carrying all nine notes. This is the
 * preview that catches the notes colliding with each other or with the cell border, which is
 * what [NOTE_SIZE_RATIO] trades off against.
 */
@ScreenshotTest(fontScaleSensitive = false)
@PreviewComponent
@Composable
internal fun SumiBoardAllNotesPreview() {
    AppPreviewTheme {
        SumiBoard(
            state = boardOf { r, c ->
                if ((r + c) % 4 == 0) {
                    given(r, c)
                } else {
                    Cell(value = 0, given = false, notes = (1..9).toSet())
                }
            },
        )
    }
}

/** Selection highlight with no other state competing for attention. */
@ScreenshotTest(fontScaleSensitive = false)
@PreviewComponent
@Composable
internal fun SumiBoardSelectionPreview() {
    AppPreviewTheme {
        SumiBoard(
            state = boardOf(selected = 4 to 4) { r, c ->
                if ((r + c) % 2 == 0) given(r, c) else empty()
            },
        )
    }
}

/** Several conflicting entries at once, to check the error red does not overwhelm the grid. */
@ScreenshotTest(fontScaleSensitive = false)
@PreviewComponent
@Composable
internal fun SumiBoardErrorsPreview() {
    AppPreviewTheme {
        SumiBoard(
            state = boardOf(selected = 3 to 3) { r, c ->
                val wrong = (r == 0 && c == 1) || (r == 3 && c == 3) || (r == 6 && c == 7)
                when {
                    // 10 - solution is always a different digit in 1..9.
                    wrong -> Cell(value = 10 - PREVIEW_SOLUTION[r][c], given = false)
                    (r + c) % 2 == 0 -> given(r, c)
                    else -> empty()
                }
            },
        )
    }
}

/** Solved board: every cell filled, half given and half entered by the player. */
@ScreenshotTest(fontScaleSensitive = false)
@PreviewComponent
@Composable
internal fun SumiBoardCompletePreview() {
    AppPreviewTheme {
        SumiBoard(
            state = boardOf { r, c ->
                Cell(value = PREVIEW_SOLUTION[r][c], given = (r + c) % 2 == 0)
            },
        )
    }
}

/** High-legibility accessibility mode, on the same board as [SumiBoardPreview]. */
@ScreenshotTest(fontScaleSensitive = false)
@PreviewComponent
@Composable
internal fun SumiBoardHighLegibilityPreview() {
    AppPreviewTheme {
        SumiBoard(state = previewBoard(), highLegibility = true)
    }
}

/** Strict-conflicts mode, which highlights peers that clash rather than only wrong entries. */
@ScreenshotTest(fontScaleSensitive = false)
@PreviewComponent
@Composable
internal fun SumiBoardStrictConflictsPreview() {
    AppPreviewTheme {
        SumiBoard(state = previewBoard(), strictConflicts = true)
    }
}
