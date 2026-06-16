@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
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
private const val BOX_STROKE_RATIO = 0.05f
private const val THIN_STROKE_RATIO = 0.014f
private val BOX_STROKE_MIN = 1.5.dp
private val THIN_STROKE_MIN = 0.5.dp

// Grid line alphas (over the ink colour). The 3x3 box separators are near-opaque
// and the inner cell lines lighter, so the nine boxes read clearly at a glance.
private const val BOX_LINE_ALPHA = 0.92f
private const val THIN_LINE_ALPHA = 0.32f

// Board digit + pencil-mark sizes are a fraction of cellSize and converted
// dp -> sp via LocalDensity so they IGNORE the system font-scale setting. The
// board therefore keeps one fixed, legible glyph size no matter how large the
// user's OS text size is. cellSize is capped at MAX_BOARD_WIDTH/9 upstream, so
// these ratios also act as the maximum on tablets.
private const val DIGIT_SIZE_RATIO = 0.60f
private const val NOTE_SIZE_RATIO = 0.30f

// Inset (per side, as a fraction of cellSize) reserved as a margin around the
// 3x3 pencil-mark grid so the outer notes are centred inside a slightly smaller
// area and never touch the cell border, even at the larger NOTE_SIZE_RATIO.
private const val NOTE_AREA_INSET_RATIO = 0.07f

// Ink-bloom entrance tuning. The reveal sweeps along the top-left -> bottom-right
// diagonal (cells share a wave position by row+col, which runs 0..16). SPREAD is
// the fraction of the timeline the wave front travels across; RAMP is how long a
// single cell takes to bloom. SPREAD + RAMP == 1 so the last diagonal reaches
// full bloom exactly as the animation ends.
private const val ENTRANCE_MS = 820
private const val ENTRANCE_SPREAD = 0.62f // fraction of timeline the wave front travels
private const val ENTRANCE_RAMP = 0.38f // how long one cell takes to bloom (SPREAD + RAMP == 1)
private const val ENTRANCE_BLOOM_SCALE = 0.2f // each cell grows from this to 1.0 (with overshoot)
private const val MAX_DIAGONAL = 16f // (row 8 + col 8)

// easeOutBack overshoot constants — the cell scales past 1.0 then settles, giving
// each digit an ink-stamp "pop" as it lands. OVERSHOOT controls how far past 1.0.
private const val BACK_OVERSHOOT = 1.9f

/**
 * Per-cell linear reveal in [0,1] for the ink-bloom entrance: 0 = not yet revealed,
 * 1 = fully revealed. Derived from the board-wide [progress] and the cell's diagonal
 * position so the reveal washes across top-left -> bottom-right. Easing (alpha vs the
 * overshoot scale pop) is applied by the caller. Returns 1 (no-op) past the wave.
 */
private fun entranceReveal(progress: Float, row: Int, col: Int): Float {
    val diagNorm = (row + col) / MAX_DIAGONAL
    return ((progress - diagNorm * ENTRANCE_SPREAD) / ENTRANCE_RAMP).coerceIn(0f, 1f)
}

/** easeOutBack — overshoots past 1.0 then settles back, for an ink-stamp pop. */
private fun easeOutBack(x: Float): Float {
    val c1 = BACK_OVERSHOOT
    val c3 = c1 + 1f
    val t = x - 1f
    return 1f + c3 * t * t * t + c1 * t * t
}

/**
 * Drives the ink-bloom reveal, returning the board-wide progress in [0,1]. Starts
 * (and stays) at 1 for the no-op entrance so non-blooming boards skip the animation.
 * SumiBoard is composed fresh each time the board becomes visible (it lives inside
 * GameBody's boardReady Crossfade), so the reveal replays on every board open.
 */
@Composable
private fun rememberEntranceProgress(entrance: BoardEntrance): Float {
    val progress = remember { Animatable(if (entrance == BoardEntrance.InkBloom) 0f else 1f) }
    LaunchedEffect(entrance) {
        if (entrance == BoardEntrance.InkBloom) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(ENTRANCE_MS, easing = LinearEasing))
        } else {
            progress.snapTo(1f)
        }
    }
    return progress.value
}

// Resolved board colours + selection alphas + aurora tone for the current theme.
private data class BoardPaint(
    val cellColors: CellColors,
    val ink: Color,
    val selectionAlphas: xyz.ksharma.sumi.theme.BoardSelectionAlphas,
    val tone: AuroraTone,
)

@Composable
private fun rememberBoardPaint(): BoardPaint {
    val colors = SumiTheme.colors
    // colors.red is the muted calligraphy red (selection wash + sumi chop seal).
    // colors.error is the bold red used SPECIFICALLY for wrong-digit signalling so
    // it reads at a glance regardless of theme. Mode-aware selection-wash alphas —
    // dark paper needs hotter values to read.
    return BoardPaint(
        cellColors = CellColors(colors.ink, colors.teal, colors.red, colors.error),
        ink = colors.ink,
        selectionAlphas = if (SumiTheme.isDark) Sumi.Color.BoardSelection.Dark
        else Sumi.Color.BoardSelection.Light,
        tone = if (SumiTheme.isDark) AuroraTone.Night else AuroraTone.Paper,
    )
}

@Composable
fun SumiBoard(
    state: BoardState,
    modifier: Modifier = Modifier,
    cellSize: Dp = Sumi.Layout.cellSize,
    sweep: BoardSweep? = null,
    highLegibility: Boolean = false,
    strictConflicts: Boolean = false,
    entrance: BoardEntrance = BoardEntrance.None,
    onCellTap: ((r: Int, c: Int) -> Unit)? = null,
) {
    val boardSize = cellSize * 9
    val entranceProgress = rememberEntranceProgress(entrance)
    val paint = rememberBoardPaint()
    val selected = state.selected
    // Strict mode highlights only rule conflicts (no solution spoilers); default highlights
    // every cell that differs from the solution.
    val errorCells = if (strictConflicts) state.conflictCells else state.errorCells

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
            drawCellBackgrounds(state, selected, errorCells, px, paint.cellColors, paint.selectionAlphas)
            drawGridLines(px, paint.ink)
        }

        CellContents(
            state,
            errorCells,
            cellSize,
            paint.cellColors,
            highLegibility,
            entrance,
            entranceProgress,
        )

        SweepLayer(
            activeSweeps = activeSweeps,
            externalSweep = sweep,
            cellSize = cellSize,
            boardSize = boardSize,
            tone = paint.tone,
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
    errorCells: Set<Pair<Int, Int>>,
    cellSize: Dp,
    colors: CellColors,
    highLegibility: Boolean,
    entrance: BoardEntrance,
    entranceProgress: Float,
) {
    for (r in 0..8) {
        for (c in 0..8) {
            val cell = state.cells[r][c]
            if (cell.value == 0 && cell.notes.isEmpty()) continue
            val isError = (r to c) in errorCells
            val textColor = when {
                isError -> colors.errorRed
                cell.given -> colors.ink
                else -> colors.teal
            }
            // 1f for the no-op entrance — the reveal multiplies straight through without
            // changing the existing per-cell fade.
            val reveal = if (entrance == BoardEntrance.InkBloom) {
                entranceReveal(entranceProgress, r, c)
            } else {
                1f
            }
            key(r, c) {
                CellText(
                    cell = cell,
                    row = r,
                    col = c,
                    cellSize = cellSize,
                    textColor = textColor,
                    highLegibility = highLegibility,
                    entranceReveal = reveal,
                )
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
    highLegibility: Boolean,
    entranceReveal: Float,
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

    // Fold the ink-bloom reveal into the per-cell fade/scale. entranceReveal is 1 for
    // the no-op entrance, so both reduce to the previous behaviour. Alpha uses the paper
    // ease (soft ink soak); scale uses easeOutBack so the digit overshoots then settles
    // like an ink stamp. Givens have no per-placement scale (scale == 1), so the bloom
    // is their only scale source.
    val effAlpha = alpha.value * Sumi.Ease.paper.transform(entranceReveal)
    val effScale = scale.value * lerp(ENTRANCE_BLOOM_SCALE, 1f, easeOutBack(entranceReveal))

    if (cell.value != 0) {
        CellDigitLayout(
            cell = cell,
            offset = DpOffset(offsetX, offsetY),
            cellSize = cellSize,
            textColor = textColor,
            alpha = effAlpha,
            scale = effScale,
            highLegibility = highLegibility,
        )
    } else if (cell.notes.isNotEmpty()) {
        NoteGrid(
            notes = cell.notes,
            cellSize = cellSize,
            offsetX = offsetX,
            offsetY = offsetY,
            entranceReveal = entranceReveal,
        )
    }
}

@Composable
private fun CellDigitLayout(
    cell: Cell,
    offset: DpOffset,
    cellSize: Dp,
    textColor: Color,
    alpha: Float,
    scale: Float,
    highLegibility: Boolean,
) {
    val digitSize = with(LocalDensity.current) { (cellSize * DIGIT_SIZE_RATIO).toSp() }
    // High-legibility mode swaps the Cormorant serif numerals for the UI sans
    // family (clearer 1/4/7); otherwise the board keeps its calligraphic numerals.
    val baseStyle = if (highLegibility) {
        SumiTheme.typography.numeral.copy(fontFamily = SumiTheme.typography.uiLabel.fontFamily)
    } else {
        SumiTheme.typography.numeral
    }
    Layout(
        content = {
            Text(
                text = cell.value.toString(),
                // Both given and player digits use the same numeral typeface so 1 and 7
                // are unambiguous (the previous handwriting font for player entries had
                // near-identical 1/7 glyphs). The teal vs ink colour set by textColor is
                // the affordance that still tells the user which cells they entered.
                // fontSize/lineHeight overridden so the digit is sized from the cell and
                // never scaled by the system font setting (see DIGIT_SIZE_RATIO).
                // Alpha + scale are driven entirely through graphicsLayer (for both given
                // and player digits) so the bloom scale applies uniformly.
                style = baseStyle.copy(
                    color = textColor,
                    fontSize = digitSize,
                    lineHeight = digitSize,
                ),
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                },
            )
        },
        modifier = Modifier,
    ) { measurables, constraints ->
        val placeable = measurables[0].measure(constraints)
        layout(cellSize.roundToPx(), cellSize.roundToPx()) {
            val x = offset.x.roundToPx() + (cellSize.roundToPx() - placeable.width) / 2
            val y = offset.y.roundToPx() + (cellSize.roundToPx() - placeable.height) / 2
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
    entranceReveal: Float,
) {
    val noteColor = SumiTheme.colors.ink.copy(alpha = 0.5f)
    val noteSizeSp = with(LocalDensity.current) { (cellSize * NOTE_SIZE_RATIO).toSp() }
    // Restored notes bloom in with the rest of the board on the ink-bloom entrance
    // (reveal is 1 otherwise, so this is a no-op during normal play).
    val noteScale = lerp(ENTRANCE_BLOOM_SCALE, 1f, easeOutBack(entranceReveal))
    val noteAlpha = Sumi.Ease.paper.transform(entranceReveal)
    Layout(
        content = {
            for (d in 1..9) {
                if (d in notes) {
                    Text(
                        text = d.toString(),
                        // Fixed (non-scaling) size from cellSize — bigger than before so
                        // pencil marks stay legible, and no longer driven by the system
                        // font setting that previously let them grow and clip.
                        style = SumiTheme.typography.hand.copy(
                            fontSize = noteSizeSp,
                            lineHeight = noteSizeSp,
                            color = noteColor,
                        ),
                        modifier = Modifier.graphicsLayer {
                            scaleX = noteScale
                            scaleY = noteScale
                            this.alpha = noteAlpha
                        },
                    )
                } else {
                    Box(modifier = Modifier)
                }
            }
        },
        modifier = Modifier,
    ) { measurables, _ ->
        val cellPx = cellSize.roundToPx()
        // Reserve a small margin on every side so the 3x3 note grid sits inside the
        // cell and the outer notes never touch the border (see NOTE_AREA_INSET_RATIO).
        val inset = (cellSize * NOTE_AREA_INSET_RATIO).roundToPx()
        val areaPx = cellPx - inset * 2
        val subCell = areaPx / 3
        // Measure each glyph at its natural size (unbounded) instead of forcing it
        // into the sub-cell, then centre it in its 3x3 slot — so a larger note can
        // never be clipped at the edges.
        val placeables = measurables.map { it.measure(Constraints()) }
        layout(cellPx, cellPx) {
            placeables.forEachIndexed { i, p ->
                val nr = i / 3
                val nc = i % 3
                val x = offsetX.roundToPx() + inset + nc * subCell + (subCell - p.width) / 2
                val y = offsetY.roundToPx() + inset + nr * subCell + (subCell - p.height) / 2
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
        val color = ink.copy(alpha = if (isBox) BOX_LINE_ALPHA else THIN_LINE_ALPHA)
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
