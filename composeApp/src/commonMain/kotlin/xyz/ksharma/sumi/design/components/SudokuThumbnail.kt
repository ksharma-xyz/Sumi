@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.theme.SumiTheme

/**
 * Tiny 9×9 read-only Sudoku grid for the Win share card. Renders 81 digits from a
 * row-major string of `'1'`–`'9'` characters, with thicker box dividers at rows/cols 3 and 6.
 *
 * Used inside `WinShareCard` so the shared image shows the actual completed puzzle —
 * not just the time / marks / quote.
 */
@Composable
fun SudokuThumbnail(
    cells: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 220.dp,
) {
    if (cells.length < 81) return
    val ink = SumiTheme.colors.ink
    val thinLine = SumiTheme.colors.ink.copy(alpha = 0.18f)
    val measurer = rememberTextMeasurer()
    val numStyle = TextStyle(
        color = ink,
        fontSize = (sizeDp.value * 0.052f).sp,
        fontStyle = FontStyle.Italic,
    )

    Box(modifier = modifier.size(sizeDp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellSize = size.minDimension / 9f
            val gridSize = cellSize * 9f
            val thinPx = 0.5.dp.toPx()
            val boxPx = 1.5.dp.toPx()

            for (i in 0..9) {
                val isBox = i % 3 == 0
                val color = if (isBox) ink else thinLine
                val width = if (isBox) boxPx else thinPx
                drawLine(
                    color = color,
                    start = Offset(i * cellSize, 0f),
                    end = Offset(i * cellSize, gridSize),
                    strokeWidth = width,
                )
                drawLine(
                    color = color,
                    start = Offset(0f, i * cellSize),
                    end = Offset(gridSize, i * cellSize),
                    strokeWidth = width,
                )
            }

            for (idx in 0 until 81) {
                val digit = cells[idx].digitToIntOrNull()
                if (digit != null && digit in 1..9) {
                    val layout = measurer.measure(
                        text = AnnotatedString(digit.toString()),
                        style = numStyle,
                    )
                    val row = idx / 9
                    val col = idx % 9
                    val x = col * cellSize + (cellSize - layout.size.width) / 2f
                    val y = row * cellSize + (cellSize - layout.size.height) / 2f
                    drawText(textLayoutResult = layout, topLeft = Offset(x, y))
                }
            }
        }
    }
}
