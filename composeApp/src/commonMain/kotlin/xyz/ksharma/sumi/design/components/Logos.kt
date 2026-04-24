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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.resources.enso_ink
import xyz.ksharma.sumi.theme.SumiTheme

/**
 * Splash Ensō logo — always renders the shipped asset (never a plain geometric arc).
 * [progress] drives a fade + subtle scale reveal (Option B from BACKGROUNDS.md).
 */
@Composable
fun LogoEnso(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = Color.Unspecified,
    progress: Float = 1f,
) {
    val resolvedColor = if (color == Color.Unspecified) SumiTheme.colors.ink else color
    Image(
        painter = painterResource(Res.drawable.enso_ink),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                alpha = progress
                val scale = 0.96f + 0.04f * progress
                scaleX = scale
                scaleY = scale
            },
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(resolvedColor),
    )
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
