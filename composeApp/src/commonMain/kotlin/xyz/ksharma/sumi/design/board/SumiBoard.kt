@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Cell
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

// Grid stroke widths derived from cellSize so the board scales cleanly when
// cellSize changes (was hardcoded to 1.5dp / 0.5dp tied to the original 38dp
// cell). At cellSize = 41dp these resolve to ~1.4dp / ~0.5dp, matching the
// previous look; at larger cells they grow proportionally.
private const val BOX_STROKE_RATIO = 0.035f
private const val THIN_STROKE_RATIO = 0.012f
private val BOX_STROKE_MIN = 1.dp
private val THIN_STROKE_MIN = 0.5.dp

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
    // colors.red is the muted calligraphy red — used for selection wash + the
    // sumi chop seal aesthetic. colors.error is the bold red (identical in
    // light + dark) — used SPECIFICALLY for wrong-digit signalling so it
    // reads at a glance regardless of theme.
    val red = colors.red
    val errorRed = colors.error
    val tone = if (SumiTheme.isDark) AuroraTone.Night else AuroraTone.Paper
    // Mode-aware selection-wash alphas — dark paper needs hotter values to read.
    val selectionAlphas = if (SumiTheme.isDark) Sumi.Color.BoardSelection.Dark
    else Sumi.Color.BoardSelection.Light
    val selected = state.selected
    val errorCells = state.errorCells

    val activeSweeps = rememberHouseSweeps(state)
    val currentOnCellTap by rememberUpdatedState(onCellTap)

    Box(modifier = modifier.size(boardSize).semantics { isTraversalGroup = true }) {
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
            drawCellBackgrounds(
                state,
                selected,
                errorCells,
                px,
                CellColors(ink, teal, red, errorRed),
                selectionAlphas,
            )
            drawGridLines(px, ink)
        }

        CellContents(state = state, cellSize = cellSize, ink = ink, teal = teal, errorRed = errorRed)

        SweepLayer(
            activeSweeps = activeSweeps,
            externalSweep = sweep,
            cellSize = cellSize,
            boardSize = boardSize,
            tone = tone,
        )
        CellA11yGrid(
            state = state,
            cellSize = cellSize,
            onCellTap = onCellTap,
            modifier = Modifier.size(boardSize),
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
    errorRed: Color,
) {
    val errorCells = state.errorCells
    for (r in 0..8) {
        for (c in 0..8) {
            val cell = state.cells[r][c]
            if (cell.value == 0 && cell.notes.isEmpty()) continue
            val isError = (r to c) in errorCells
            val textColor = when {
                isError -> errorRed
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
    val scale = remember { Animatable(1f) }

    LaunchedEffect(cell.value) {
        if (cell.value != 0) {
            alpha.snapTo(0f)
            if (!cell.given) scale.snapTo(0.80f)
            launch { alpha.animateTo(1f, animationSpec = tween(160, easing = Sumi.Ease.paper)) }
            if (!cell.given) {
                launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    )
                }
            }
        }
    }

    val offsetX = cellSize * col
    val offsetY = cellSize * row

    if (cell.value != 0) {
        CellDigitLayout(
            cell = cell,
            offsetX = offsetX,
            offsetY = offsetY,
            cellSize = cellSize,
            textColor = textColor,
            alpha = alpha.value,
            scale = scale.value,
        )
    } else if (cell.notes.isNotEmpty()) {
        NoteGrid(notes = cell.notes, cellSize = cellSize, offsetX = offsetX, offsetY = offsetY)
    }
}

@Composable
private fun CellDigitLayout(
    cell: Cell,
    offsetX: Dp,
    offsetY: Dp,
    cellSize: Dp,
    textColor: Color,
    alpha: Float,
    scale: Float,
) {
    Layout(
        content = {
            Text(
                text = cell.value.toString(),
                // Both given and player digits use the same numeral typeface so 1 and 7
                // are unambiguous (the previous handwriting font for player entries had
                // near-identical 1/7 glyphs). The teal vs ink colour set by textColor is
                // the affordance that still tells the user which cells they entered.
                style = if (cell.given) {
                    SumiTheme.typography.numeral.copy(color = textColor.copy(alpha = alpha))
                } else {
                    SumiTheme.typography.numeral.copy(color = textColor)
                },
                modifier = if (!cell.given) {
                    Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                } else {
                    Modifier
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

private data class CellColors(val ink: Color, val teal: Color, val red: Color, val errorRed: Color)

@Suppress("LongParameterList")
private fun DrawScope.drawCellBackgrounds(
    state: BoardState,
    selected: Pair<Int, Int>?,
    errorCells: Set<Pair<Int, Int>>,
    px: Float,
    colors: CellColors,
    alphas: xyz.ksharma.sumi.theme.BoardSelectionAlphas,
) {
    val selectedValue = selected?.let { (r, c) -> state.cells[r][c].value }
    for (r in 0..8) {
        for (c in 0..8) {
            val bg = cellBg(state.cells[r][c], r, c, selected, errorCells, selectedValue, colors, alphas)
            if (bg != Color.Transparent) {
                drawRect(color = bg, topLeft = Offset(c * px, r * px), size = Size(px, px))
            }
        }
    }
}

@Suppress("LongParameterList")
private fun cellBg(
    cell: xyz.ksharma.sumi.game.model.Cell,
    r: Int,
    c: Int,
    selected: Pair<Int, Int>?,
    errorCells: Set<Pair<Int, Int>>,
    selectedValue: Int?,
    colors: CellColors,
    alphas: xyz.ksharma.sumi.theme.BoardSelectionAlphas,
): Color {
    val isSelected = selected?.first == r && selected.second == c
    val isError = (r to c) in errorCells
    val isInUnit = selected != null && !isSelected && sharesUnit(r, c, selected)
    val isSameDigit = selectedValue != null && selectedValue != 0 &&
        cell.value == selectedValue && !isSelected
    return when {
        isError -> colors.errorRed.copy(alpha = alphas.error)
        isSelected -> colors.red.copy(alpha = alphas.selected)
        isSameDigit -> colors.teal.copy(alpha = alphas.sameDigit)
        isInUnit -> colors.ink.copy(alpha = alphas.inUnit)
        else -> Color.Transparent
    }
}

private fun sharesUnit(r: Int, c: Int, sel: Pair<Int, Int>): Boolean =
    r == sel.first || c == sel.second || sameBox(r, c, sel.first, sel.second)

private fun DrawScope.drawGridLines(px: Float, ink: Color) {
    val boardPx = px * 9
    // Stroke widths scale with cell size so the grid stays visually balanced
    // when cellSize is bumped (was: hard-coded 1.5dp / 0.5dp tied to one
    // specific cellSize). Coerce to dp minimums so the lines never disappear
    // on tiny embed sizes.
    val cellDp = px.toDp().value
    val boxStroke = (cellDp * BOX_STROKE_RATIO).dp.toPx().coerceAtLeast(BOX_STROKE_MIN.toPx())
    val thinStroke = (cellDp * THIN_STROKE_RATIO).dp.toPx().coerceAtLeast(THIN_STROKE_MIN.toPx())

    // Full grid (i = 0..9): SumiBoard owns its own perimeter so it ships as a
    // complete bordered unit. Wrapping containers should NOT add their own
    // Modifier.border — the canvas's i=0 / i=9 perimeter strokes are the
    // outer ring, inset by half their thickness so the full stroke sits
    // inside the canvas (without inset, half a 1.4dp stroke gets clipped).
    for (i in 0..9) {
        val isBox = i % 3 == 0
        val strokeWidth = if (isBox) boxStroke else thinStroke
        val color = ink.copy(alpha = if (isBox) 0.85f else 0.25f)
        val pos = when (i) {
            0 -> strokeWidth / 2f
            9 -> boardPx - strokeWidth / 2f
            else -> i * px
        }
        drawLine(color, Offset(pos, 0f), Offset(pos, boardPx), strokeWidth)
        drawLine(color, Offset(0f, pos), Offset(boardPx, pos), strokeWidth)
    }
}

@Composable
private fun CellA11yGrid(
    state: BoardState,
    cellSize: Dp,
    onCellTap: ((Int, Int) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            for (r in 0..8) {
                for (c in 0..8) {
                    val cell = state.cells[r][c]
                    val isSelected = state.selected?.first == r && state.selected?.second == c
                    val isError = (r to c) in state.errorCells
                    val desc = buildCellDescription(r, c, cell, isSelected, isError)
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .semantics(mergeDescendants = false) {
                                contentDescription = desc
                                if (onCellTap != null) {
                                    onClick(label = "Select") {
                                        onCellTap(r, c)
                                        true
                                    }
                                }
                            },
                    )
                }
            }
        },
    ) { measurables, _ ->
        val sizePx = cellSize.roundToPx()
        val placeables = measurables.map { it.measure(Constraints.fixed(sizePx, sizePx)) }
        layout(sizePx * 9, sizePx * 9) {
            placeables.forEachIndexed { i, p ->
                val r = i / 9
                val c = i % 9
                p.placeRelative(c * sizePx, r * sizePx)
            }
        }
    }
}

private fun buildCellDescription(
    row: Int,
    col: Int,
    cell: Cell,
    isSelected: Boolean,
    isError: Boolean,
): String {
    val base = "Row ${row + 1}, column ${col + 1}"
    val value = when {
        cell.value != 0 && cell.given -> "Given: ${cell.value}"
        cell.value != 0 && isError -> "Error: ${cell.value}"
        cell.value != 0 -> "Entered: ${cell.value}"
        cell.notes.isNotEmpty() -> "Notes: ${cell.notes.sorted().joinToString()}"
        else -> "Empty"
    }
    val sel = if (isSelected) ", selected" else ""
    return "$base. $value$sel"
}

private fun sameBox(r1: Int, c1: Int, r2: Int, c2: Int): Boolean =
    r1 / 3 == r2 / 3 && c1 / 3 == c2 / 3
