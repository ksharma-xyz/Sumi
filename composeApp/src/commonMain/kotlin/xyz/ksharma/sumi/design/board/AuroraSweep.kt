@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.board

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import xyz.ksharma.sumi.a11y.rememberReducedMotion
import xyz.ksharma.sumi.theme.SumiTokens

private val AURORA_HUES_PAPER = listOf(
    Color(0xFFE8B4A8),
    Color(0xFFB8D4C8),
    Color(0xFFC8B4D8),
    Color(0xFFD4C88A),
    Color(0xFFB4C4D8),
)

private val AURORA_HUES_NIGHT = listOf(
    Color(0xFF6A4E42),
    Color(0xFF425C52),
    Color(0xFF524660),
    Color(0xFF5C5638),
    Color(0xFF3A4658),
)

@Composable
fun AuroraSweep(
    kind: BoardSweep,
    cellSize: Dp,
    modifier: Modifier = Modifier,
    tone: AuroraTone = AuroraTone.Paper,
) {
    if (rememberReducedMotion()) return
    val duration = when (kind) {
        is BoardSweep.Win -> SumiTokens.Duration.CEREMONY
        is BoardSweep.Box -> 1200
        else -> 1000
    }

    val t by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = duration, easing = SumiTokens.Ease.bleed),
        label = "aurora_sweep",
    )

    val hues = if (tone == AuroraTone.Paper) AURORA_HUES_PAPER else AURORA_HUES_NIGHT
    val boardPx = cellSize.value * 9

    Canvas(modifier = modifier) {
        drawAuroraSweep(kind = kind, t = t, hues = hues, boardPx = boardPx, cellPx = cellSize.value)
    }
}

private fun DrawScope.drawAuroraSweep(
    kind: BoardSweep,
    t: Float,
    hues: List<Color>,
    boardPx: Float,
    cellPx: Float,
) {
    val sweepWidth = boardPx * 0.35f
    val travelDist = boardPx + sweepWidth
    val gradColors = buildGradientColors(hues)

    when (kind) {
        is BoardSweep.Row -> drawRowSweep(kind, t, travelDist, sweepWidth, boardPx, cellPx, gradColors)
        is BoardSweep.Col -> drawColSweep(kind, t, travelDist, sweepWidth, boardPx, cellPx, gradColors)
        is BoardSweep.Box -> drawBoxSweep(kind, t, travelDist, sweepWidth, cellPx, gradColors)
        is BoardSweep.Win -> drawWinSweep(t, travelDist, sweepWidth, hues)
    }
}

private fun buildGradientColors(hues: List<Color>): List<Color> {
    val take = hues.take(4)
    val mid = take.size / 2
    return take.mapIndexed { i, c -> c.copy(alpha = if (i == mid) 0.55f else 0.15f) }
}

private fun DrawScope.drawRowSweep(
    kind: BoardSweep.Row,
    t: Float,
    travelDist: Float,
    sweepWidth: Float,
    boardPx: Float,
    cellPx: Float,
    gradColors: List<Color>,
) {
    val top = kind.index * cellPx
    val x = t * travelDist - sweepWidth
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent) + gradColors + listOf(Color.Transparent),
            startX = x,
            endX = x + sweepWidth,
        ),
        topLeft = Offset(0f, top),
        size = Size(boardPx, cellPx),
    )
}

private fun DrawScope.drawColSweep(
    kind: BoardSweep.Col,
    t: Float,
    travelDist: Float,
    sweepWidth: Float,
    boardPx: Float,
    cellPx: Float,
    gradColors: List<Color>,
) {
    val left = kind.index * cellPx
    val y = t * travelDist - sweepWidth
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent) + gradColors + listOf(Color.Transparent),
            startY = y,
            endY = y + sweepWidth,
        ),
        topLeft = Offset(left, 0f),
        size = Size(cellPx, boardPx),
    )
}

private fun DrawScope.drawBoxSweep(
    kind: BoardSweep.Box,
    t: Float,
    travelDist: Float,
    sweepWidth: Float,
    cellPx: Float,
    gradColors: List<Color>,
) {
    val left = (kind.index % 3) * 3 * cellPx
    val top = (kind.index / 3) * 3 * cellPx
    val x = t * travelDist - sweepWidth
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent) + gradColors + listOf(Color.Transparent),
            startX = left + x,
            endX = left + x + sweepWidth,
        ),
        topLeft = Offset(left, top),
        size = Size(cellPx * 3, cellPx * 3),
    )
}

private fun DrawScope.drawWinSweep(
    t: Float,
    travelDist: Float,
    sweepWidth: Float,
    hues: List<Color>,
) {
    val x = t * travelDist - sweepWidth
    val winColors = hues.flatMap { c -> listOf(c.copy(alpha = 0.45f), c.copy(alpha = 0.1f)) }
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent) + winColors + listOf(Color.Transparent),
            startX = x,
            endX = x + sweepWidth,
        ),
        size = this.size,
    )
}
