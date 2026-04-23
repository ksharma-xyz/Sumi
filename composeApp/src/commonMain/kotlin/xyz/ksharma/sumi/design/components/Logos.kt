@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.theme.SumiTheme
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun LogoEnso(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = Sumi.Color.ink,
    progress: Float = 1f,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val cx = w / 2f
        val cy = w / 2f
        val rx = w * 0.435f
        val ry = w * 0.400f
        val startAngleRad = (148.0 * kotlin.math.PI / 180.0).toFloat()
        val sweepRad = (297.0 * kotlin.math.PI / 180.0).toFloat()
        val steps = 120
        val limitedSteps = (steps * progress.coerceIn(0f, 1f)).toInt().coerceAtLeast(2)
        val maxStroke = w * 0.092f
        val minStroke = w * 0.022f

        val outerPts = ArrayList<Offset>(limitedSteps + 1)
        val innerPts = ArrayList<Offset>(limitedSteps + 1)

        for (i in 0..limitedSteps) {
            val t = i.toFloat() / steps
            val angle = startAngleRad + sweepRad * t
            val wobble = w * 0.006f * sin(angle * 3.7f)
            val x = cx + (rx + wobble) * cos(angle)
            val y = cy + (ry - wobble * 0.4f) * sin(angle)

            val halfW = ensoHalfWidth(t, maxStroke, minStroke) / 2f

            val tx = -ry * sin(angle)
            val ty = rx * cos(angle)
            val tLen = sqrt(tx * tx + ty * ty).coerceAtLeast(0.001f)
            val px = -ty / tLen
            val py = tx / tLen

            outerPts.add(Offset(x + px * halfW, y + py * halfW))
            innerPts.add(Offset(x - px * halfW, y - py * halfW))
        }

        val path = Path().apply {
            moveTo(outerPts[0].x, outerPts[0].y)
            for (i in 1..limitedSteps) lineTo(outerPts[i].x, outerPts[i].y)
            lineTo(innerPts[limitedSteps].x, innerPts[limitedSteps].y)
            for (i in limitedSteps downTo 0) lineTo(innerPts[i].x, innerPts[i].y)
            close()
        }
        drawPath(path = path, color = color)

        val startPt = outerPts[0].run { Offset((x + innerPts[0].x) / 2f, (y + innerPts[0].y) / 2f) }
        drawCircle(color = color, radius = maxStroke * 0.48f, center = startPt)
    }
}

private fun ensoHalfWidth(t: Float, max: Float, min: Float): Float = when {
    t < 0.06f -> min + (max * 0.55f - min) * (t / 0.06f)
    t < 0.22f -> max * 0.55f + (max - max * 0.55f) * ((t - 0.06f) / 0.16f)
    t < 0.72f -> max * (0.88f + 0.12f * sin(t * 7.3f + 1f))
    else -> max * 0.9f + (min - max * 0.9f) * ((t - 0.72f) / 0.28f)
}

@Composable
fun LogoGrid(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = Sumi.Color.ink,
    accent: Color = Sumi.Color.red,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val pad = w * 0.15f
        val cellW = (w - pad * 2f) / 3f
        val strokePx = w * 0.025f
        val accentStroke = w * 0.04f

        // 4 horizontal + 4 vertical lines forming 3×3 grid
        for (i in 0..3) {
            val x = pad + i * cellW
            val y = pad + i * cellW
            drawLine(color, Offset(x, pad), Offset(x, w - pad), strokePx)
            drawLine(color, Offset(pad, y), Offset(w - pad, y), strokePx)
        }

        // Red center cell highlight
        val cx = pad + cellW
        val cy = pad + cellW
        drawRect(
            color = accent.copy(alpha = 0.15f),
            topLeft = Offset(cx, cy),
            size = androidx.compose.ui.geometry.Size(cellW, cellW),
        )
        drawRect(
            color = accent,
            topLeft = Offset(cx, cy),
            size = androidx.compose.ui.geometry.Size(cellW, cellW),
            style = Stroke(width = accentStroke),
        )
    }
}

@Composable
fun LogoChop(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
) {
    SealInk(modifier = modifier, size = size)
}

@Composable
fun LogoNine(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = Sumi.Color.ink,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val r = w * 0.12f
        val strokePx = w * 0.045f
        val positions = listOf(
            Offset(w * 0.25f, w * 0.25f), Offset(w * 0.5f, w * 0.25f), Offset(w * 0.75f, w * 0.25f),
            Offset(w * 0.25f, w * 0.5f), Offset(w * 0.5f, w * 0.5f), Offset(w * 0.75f, w * 0.5f),
            Offset(w * 0.25f, w * 0.75f), Offset(w * 0.5f, w * 0.75f), Offset(w * 0.75f, w * 0.75f),
        )
        positions.forEachIndexed { i, pos ->
            drawCircle(
                color = if (i == 4) color else color.copy(alpha = 0.4f),
                radius = if (i == 4) r * 1.2f else r,
                center = pos,
                style = Stroke(width = strokePx),
            )
        }
    }
}

@Composable
fun LogoStrokes(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = Sumi.Color.ink,
    accent: Color = Sumi.Color.red,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val pad = w * 0.2f
        val gap = (w - pad * 2f) / 4f
        val strokePx = w * 0.035f

        // 5 vertical strokes, middle one in accent
        for (i in 0..4) {
            val x = pad + i * gap
            val isAccent = i == 2
            val topOff = if (isAccent) 0f else w * 0.05f * (i % 2)
            drawLine(
                color = if (isAccent) accent else color,
                start = Offset(x, pad + topOff),
                end = Offset(x, w - pad - topOff),
                strokeWidth = if (isAccent) strokePx * 1.4f else strokePx,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun LogoWordmark(
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    color: Color = Sumi.Color.ink,
    accent: Color = Sumi.Color.red,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Sumi",
            style = SumiTheme.typography.h3.copy(
                fontSize = (26 * scale).sp,
                color = color,
            ),
        )
        Spacer(Modifier.width((4 * scale).dp))
        Text(
            text = "墨",
            style = SumiTheme.typography.cjk.copy(
                fontSize = (22 * scale).sp,
                color = accent,
            ),
        )
    }
}
