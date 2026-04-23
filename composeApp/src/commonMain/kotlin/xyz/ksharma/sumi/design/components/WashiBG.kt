package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import xyz.ksharma.sumi.theme.SumiTheme
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Paper-texture background. Every full-screen surface uses this as the base.
 *
 * @param intensity 0..1 — how strongly the fibre texture reads
 */
@Composable
fun WashiBG(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val colors = SumiTheme.colors
    val isDark = SumiTheme.isDark
    val baseColor = colors.paper
    val fiberColor = colors.ink
    val vignetteColor = if (isDark) Color(0x55000000) else Color(0x26644619)

    val fibers = remember(isDark, intensity) {
        val rng = Random(0x53756D69L) // "Sumi" seed
        List(1000) {
            FiberLine(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                length = rng.nextFloat() * 0.06f + 0.01f,
                angle = rng.nextFloat() * 0.4f - 0.2f,
                alpha = (rng.nextFloat() * 0.5f + 0.3f) * 0.09f * intensity,
                width = rng.nextFloat() * 0.6f + 0.3f,
            )
        }
    }

    Box(
        modifier = modifier.drawBehind {
            drawRect(color = baseColor)

            for (fiber in fibers) {
                val x1 = fiber.x * size.width
                val y1 = fiber.y * size.height
                val len = fiber.length * size.width
                drawLine(
                    color = fiberColor.copy(alpha = fiber.alpha),
                    start = Offset(x1, y1),
                    end = Offset(x1 + len * cos(fiber.angle), y1 + len * sin(fiber.angle)),
                    strokeWidth = fiber.width,
                )
            }

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, vignetteColor),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = maxOf(size.width, size.height) * 0.72f,
                ),
            )
        },
    ) {
        content()
    }
}

private data class FiberLine(
    val x: Float,
    val y: Float,
    val length: Float,
    val angle: Float,
    val alpha: Float,
    val width: Float,
)
