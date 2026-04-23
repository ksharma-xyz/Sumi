@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Cell
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun SumiBoard(
    state: BoardState,
    modifier: Modifier = Modifier,
    cellSize: Dp = Sumi.Layout.cellSize,
    sweep: BoardSweep? = null,
    onCellTap: ((r: Int, c: Int) -> Unit)? = null,
) {
    val boardSize = cellSize * 9
    val colors = SumiTheme.colors
    val ink = colors.ink
    val teal = colors.teal
    val red = colors.red
    val tone = if (SumiTheme.isDark) AuroraTone.Night else AuroraTone.Paper
    val selected = state.selected
    val errorCells = state.errorCells

    val activeSweeps = rememberHouseSweeps(state)
    val currentOnCellTap by rememberUpdatedState(onCellTap)

    Box(modifier = modifier.size(boardSize)) {
        Canvas(
            modifier = Modifier
                .size(boardSize)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val px = cellSize.toPx()
                        val r = (offset.y / px).toInt().coerceIn(0, 8)
                        val c = (offset.x / px).toInt().coerceIn(0, 8)
                        currentOnCellTap?.invoke(r, c)
                    }
                },
        ) {
            val px = cellSize.toPx()
            drawCellBackgrounds(state, selected, errorCells, px, CellColors(ink, teal, red))
            drawGridLines(px, ink)
        }

        CellContents(state = state, cellSize = cellSize, ink = ink, teal = teal, red = red)

        SweepLayer(
            activeSweeps = activeSweeps,
            externalSweep = sweep,
            cellSize = cellSize,
            boardSize = boardSize,
            tone = tone,
        )
    }
}

@Composable
private fun rememberHouseSweeps(state: BoardState): List<BoardSweep> {
    var activeSweeps by remember { mutableStateOf(emptyList<BoardSweep>()) }
    var prevRows by remember { mutableStateOf(emptySet<Int>()) }
    var prevCols by remember { mutableStateOf(emptySet<Int>()) }
    var prevBoxes by remember { mutableStateOf(emptySet<Int>()) }

    LaunchedEffect(state.completedRows, state.completedCols, state.completedBoxes) {
        (state.completedRows - prevRows).forEach { r ->
            launch {
                val s = BoardSweep.Row(r)
                activeSweeps = activeSweeps + s
                delay(1200L)
                activeSweeps = activeSweeps - s
            }
        }
        (state.completedCols - prevCols).forEach { c ->
            launch {
                val s = BoardSweep.Col(c)
                activeSweeps = activeSweeps + s
                delay(1200L)
                activeSweeps = activeSweeps - s
            }
        }
        (state.completedBoxes - prevBoxes).forEach { b ->
            launch {
                val s = BoardSweep.Box(b)
                activeSweeps = activeSweeps + s
                delay(1400L)
                activeSweeps = activeSweeps - s
            }
        }
        prevRows = state.completedRows
        prevCols = state.completedCols
        prevBoxes = state.completedBoxes
    }

    return activeSweeps
}

@Composable
private fun SweepLayer(
    activeSweeps: List<BoardSweep>,
    externalSweep: BoardSweep?,
    cellSize: Dp,
    boardSize: Dp,
    tone: AuroraTone,
) {
    activeSweeps.forEach { kind ->
        key(kind) {
            AuroraSweep(kind = kind, cellSize = cellSize, modifier = Modifier.size(boardSize), tone = tone)
        }
    }
    if (externalSweep != null) {
        AuroraSweep(kind = externalSweep, cellSize = cellSize, modifier = Modifier.size(boardSize), tone = tone)
    }
}

@Composable
private fun CellContents(
    state: BoardState,
    cellSize: Dp,
    ink: Color,
    teal: Color,
    red: Color,
) {
    val errorCells = state.errorCells
    for (r in 0..8) {
        for (c in 0..8) {
            val cell = state.cells[r][c]
            if (cell.value == 0 && cell.notes.isEmpty()) continue
            val isError = (r to c) in errorCells
            val textColor = when {
                isError -> red
                cell.given -> ink
                else -> teal
            }
            key(r, c) {
                CellText(cell = cell, row = r, col = c, cellSize = cellSize, textColor = textColor)
            }
        }
    }
}

@Composable
private fun CellText(
    cell: Cell,
    row: Int,
    col: Int,
    cellSize: Dp,
    textColor: Color,
) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(cell.value) {
        if (cell.value != 0) {
            alpha.snapTo(0f)
            alpha.animateTo(1f, animationSpec = tween(120, easing = Sumi.Ease.paper))
        }
    }

    val offsetX = cellSize * col
    val offsetY = cellSize * row

    if (cell.value != 0) {
        Layout(
            content = {
                Text(
                    text = cell.value.toString(),
                    style = if (cell.given) {
                        SumiTheme.typography.numeral.copy(color = textColor.copy(alpha = alpha.value))
                    } else {
                        SumiTheme.typography.hand.copy(
                            color = textColor.copy(alpha = alpha.value),
                            fontSize = 24.sp,
                        )
                    },
                )
            },
            modifier = Modifier,
        ) { measurables, constraints ->
            val placeable = measurables[0].measure(constraints)
            layout(cellSize.roundToPx(), cellSize.roundToPx()) {
                val x = offsetX.roundToPx() + (cellSize.roundToPx() - placeable.width) / 2
                val y = offsetY.roundToPx() + (cellSize.roundToPx() - placeable.height) / 2
                placeable.placeRelative(x, y)
            }
        }
    } else if (cell.notes.isNotEmpty()) {
        NoteGrid(notes = cell.notes, cellSize = cellSize, offsetX = offsetX, offsetY = offsetY)
    }
}

@Composable
private fun NoteGrid(
    notes: Set<Int>,
    cellSize: Dp,
    offsetX: Dp,
    offsetY: Dp,
) {
    val noteColor = SumiTheme.colors.ink.copy(alpha = 0.45f)
    Layout(
        content = {
            for (d in 1..9) {
                if (d in notes) {
                    Text(
                        text = d.toString(),
                        style = SumiTheme.typography.hand.copy(
                            fontSize = (cellSize.value * 0.28f).sp,
                            color = noteColor,
                        ),
                    )
                } else {
                    Box(modifier = Modifier)
                }
            }
        },
        modifier = Modifier,
    ) { measurables, _ ->
        val noteSize = (cellSize.roundToPx() / 3)
        val placeables = measurables.map {
            it.measure(androidx.compose.ui.unit.Constraints.fixed(noteSize, noteSize))
        }
        layout(cellSize.roundToPx(), cellSize.roundToPx()) {
            placeables.forEachIndexed { i, p ->
                val nr = i / 3
                val nc = i % 3
                val x = offsetX.roundToPx() + nc * noteSize
                val y = offsetY.roundToPx() + nr * noteSize
                p.placeRelative(x, y)
            }
        }
    }
}

private data class CellColors(val ink: Color, val teal: Color, val red: Color)

private fun DrawScope.drawCellBackgrounds(
    state: BoardState,
    selected: Pair<Int, Int>?,
    errorCells: Set<Pair<Int, Int>>,
    px: Float,
    colors: CellColors,
) {
    val selectedValue = selected?.let { (r, c) -> state.cells[r][c].value }
    for (r in 0..8) {
        for (c in 0..8) {
            val bg = cellBg(state.cells[r][c], r, c, selected, errorCells, selectedValue, colors)
            if (bg != Color.Transparent) {
                drawRect(color = bg, topLeft = Offset(c * px, r * px), size = Size(px, px))
            }
        }
    }
}

private fun cellBg(
    cell: xyz.ksharma.sumi.game.model.Cell,
    r: Int,
    c: Int,
    selected: Pair<Int, Int>?,
    errorCells: Set<Pair<Int, Int>>,
    selectedValue: Int?,
    colors: CellColors,
): Color {
    val isSelected = selected?.first == r && selected.second == c
    val isError = (r to c) in errorCells
    val isInUnit = selected != null && !isSelected && sharesUnit(r, c, selected)
    val isSameDigit = selectedValue != null && selectedValue != 0 &&
        cell.value == selectedValue && !isSelected
    return when {
        isError -> colors.red.copy(alpha = 0.10f)
        isSelected -> colors.red.copy(alpha = 0.08f)
        isSameDigit -> colors.teal.copy(alpha = 0.08f)
        isInUnit -> colors.ink.copy(alpha = 0.03f)
        else -> Color.Transparent
    }
}

private fun sharesUnit(r: Int, c: Int, sel: Pair<Int, Int>): Boolean =
    r == sel.first || c == sel.second || sameBox(r, c, sel.first, sel.second)

private fun DrawScope.drawGridLines(px: Float, ink: Color) {
    val boardPx = px * 9
    for (i in 0..9) {
        val pos = i * px
        val isBox = i % 3 == 0
        val strokeWidth = if (isBox) 1.5f else 0.5f
        val alpha = if (isBox) 0.85f else 0.25f
        val color = ink.copy(alpha = alpha)
        drawLine(color, Offset(pos, 0f), Offset(pos, boardPx), strokeWidth)
        drawLine(color, Offset(0f, pos), Offset(boardPx, pos), strokeWidth)
    }
    drawRect(
        color = ink,
        topLeft = Offset.Zero,
        size = Size(boardPx, boardPx),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
    )
}

private fun sameBox(r1: Int, c1: Int, r2: Int, c2: Int): Boolean =
    r1 / 3 == r2 / 3 && c1 / 3 == c2 / 3
