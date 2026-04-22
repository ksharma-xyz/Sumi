package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

/**
 * Paper-texture background. Every full-screen surface uses this as the base.
 *
 * @param intensity 0..1 — how strongly the fibre texture reads
 */
@Composable
fun WashiBG(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    intensity: Float = 1f,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val baseColor = if (dark) Sumi.Color.Night.paper else Sumi.Color.paper
    // Fiber color: dark fibers on light paper, faint light fibers on dark paper
    val fiberR = if (dark) 1f else 0.24f
    val fiberG = if (dark) 0.97f else 0.17f
    val fiberB = if (dark) 0.94f else 0.04f

    // Cache noise data so it doesn't regenerate every frame
    val fibers = remember(dark, intensity) {
        val rng = Random(0x53756D69L) // "Sumi" seed
        List(1000) {
            FiberLine(
                x       = rng.nextFloat(),
                y       = rng.nextFloat(),
                length  = rng.nextFloat() * 0.06f + 0.01f,
                angle   = rng.nextFloat() * 0.4f - 0.2f,
                alpha   = (rng.nextFloat() * 0.5f + 0.3f) * 0.09f * intensity,
                width   = rng.nextFloat() * 0.6f + 0.3f,
            )
        }
    }

    Box(
        modifier = modifier.drawBehind {
            // Base paper fill
            drawRect(color = baseColor)

            // Paper fiber texture — short near-horizontal strokes
            for (fiber in fibers) {
                val x1 = fiber.x * size.width
                val y1 = fiber.y * size.height
                val len = fiber.length * size.width
                drawLine(
                    color = Color(fiberR, fiberG, fiberB, fiber.alpha),
                    start = Offset(x1, y1),
                    end   = Offset(x1 + len * cos(fiber.angle), y1 + len * sin(fiber.angle)),
                    strokeWidth = fiber.width,
                )
            }

            // Vignette — soft warm shadow at edges
            val vignetteColor = if (dark) Color(0x55000000) else Color(0x26644619)
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
