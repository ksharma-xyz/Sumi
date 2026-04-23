@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.resources.enso_ink
import xyz.ksharma.sumi.theme.SumiTheme

// Enso path mirrors enso_ink.xml (1024×1024 viewBox scaled to 120×120):
// M694,356 A218.4,218.4 121.4,1 0,626.4 678.4 Q574.4,688.8 512,678.4
// Arc: center≈(60.92,57.14) r=25.59, startAngle=-37.1°, sweep=-262° (CCW large arc)
// Tail: quadratic bezier to (60,79.5) with control (67.26,80.72)
private fun ensoFullPath(s: Float): Path = Path().apply {
    moveTo(81.30f * s, 41.72f * s)
    arcTo(
        rect = Rect(
            left = 35.33f * s,
            top = 31.55f * s,
            right = 86.51f * s,
            bottom = 82.73f * s,
        ),
        startAngleDegrees = -37.1f,
        sweepAngleDegrees = -262f,
        forceMoveTo = false,
    )
    quadraticTo(67.26f * s, 80.72f * s, 60f * s, 79.5f * s)
}

@Composable
fun LogoEnso(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = Color.Unspecified,
    progress: Float = 1f,
) {
    val resolvedColor = if (color == Color.Unspecified) SumiTheme.colors.ink else color
    if (progress >= 1f) {
        Image(
            painter = painterResource(Res.drawable.enso_ink),
            contentDescription = null,
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(resolvedColor),
        )
    } else {
        Canvas(modifier = modifier.size(size)) {
            val s = this.size.width / 120f
            val strokeW = this.size.width * 0.092f
            val fullPath = ensoFullPath(s)
            val pm = PathMeasure().also { it.setPath(fullPath, false) }
            val drawPath = Path().also { seg -> pm.getSegment(0f, progress * pm.length, seg, true) }

            drawPath(path = drawPath, color = resolvedColor, style = Stroke(strokeW, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun LogoGrid(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = Color.Unspecified,
    accent: Color = Color.Unspecified,
) {
    val resolvedColor = if (color == Color.Unspecified) SumiTheme.colors.ink else color
    val resolvedAccent = if (accent == Color.Unspecified) SumiTheme.colors.red else accent
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val pad = w * 0.15f
        val cellW = (w - pad * 2f) / 3f
        val strokePx = w * 0.025f
        val accentStroke = w * 0.04f

        for (i in 0..3) {
            val x = pad + i * cellW
            val y = pad + i * cellW
            drawLine(resolvedColor, Offset(x, pad), Offset(x, w - pad), strokePx)
            drawLine(resolvedColor, Offset(pad, y), Offset(w - pad, y), strokePx)
        }

        val cx = pad + cellW
        val cy = pad + cellW
        drawRect(
            color = resolvedAccent.copy(alpha = 0.15f),
            topLeft = Offset(cx, cy),
            size = androidx.compose.ui.geometry.Size(cellW, cellW),
        )
        drawRect(
            color = resolvedAccent,
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
    color: Color = Color.Unspecified,
) {
    val resolvedColor = if (color == Color.Unspecified) SumiTheme.colors.ink else color
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
                color = if (i == 4) resolvedColor else resolvedColor.copy(alpha = 0.4f),
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
    color: Color = Color.Unspecified,
    accent: Color = Color.Unspecified,
) {
    val resolvedColor = if (color == Color.Unspecified) SumiTheme.colors.ink else color
    val resolvedAccent = if (accent == Color.Unspecified) SumiTheme.colors.red else accent
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
                color = if (isAccent) resolvedAccent else resolvedColor,
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
    color: Color = Color.Unspecified,
    accent: Color = Color.Unspecified,
) {
    val resolvedColor = if (color == Color.Unspecified) SumiTheme.colors.ink else color
    val resolvedAccent = if (accent == Color.Unspecified) SumiTheme.colors.red else accent
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Sumi",
            style = SumiTheme.typography.h3.copy(
                fontSize = (26 * scale).sp,
                color = resolvedColor,
            ),
        )
        Spacer(Modifier.width((4 * scale).dp))
        Text(
            text = "墨",
            style = SumiTheme.typography.cjk.copy(
                fontSize = (22 * scale).sp,
                color = resolvedAccent,
            ),
        )
    }
}
