@file:Suppress("MagicNumber") // sample puzzle data + layout maths only

package xyz.ksharma.sumi.design.board

import androidx.compose.runtime.Composable
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Cell
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.ui.preview.AppPreviewTheme
import xyz.ksharma.sumi.ui.preview.PreviewComponent

// A solved grid drives the preview so error/teal states can be derived from it.
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

// Representative half-filled board exercising every cell state the board can render:
// given digits (ink), a correct user entry (teal), a wrong entry (error red),
// empty cells, pencil-mark notes, and a selected cell.
private fun previewBoard(): BoardState {
    val sol = PREVIEW_SOLUTION
    val cells = List(9) { r ->
        List(9) { c ->
            when {
                r == 0 && c == 1 -> Cell(value = 9, given = false) // wrong (solution is 3) -> error red
                r == 0 && c == 3 -> Cell(value = sol[r][c], given = false) // correct user entry -> teal
                r == 2 && c == 5 -> Cell(value = sol[r][c], given = false) // correct user entry -> teal
                r == 1 && c == 2 -> Cell(value = 0, given = false, notes = setOf(2, 5, 8)) // pencil marks
                r == 4 && c == 4 -> Cell(value = 0, given = false, notes = setOf(1, 3, 5, 7, 9)) // pencil marks
                (r + c) % 2 == 0 -> Cell(value = sol[r][c], given = true) // given clue
                else -> Cell(value = 0, given = false) // empty
            }
        }
    }
    return BoardState(
        cells = cells,
        selected = 4 to 4,
        difficulty = Difficulty.entries.first(),
        solution = sol,
    )
}

@PreviewComponent
@Composable
internal fun SumiBoardPreview() {
    AppPreviewTheme {
        SumiBoard(state = previewBoard())
    }
}
