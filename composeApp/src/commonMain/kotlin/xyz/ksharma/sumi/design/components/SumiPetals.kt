@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.sin

private data class PetalData(
    val startXFraction: Float,
    val delayMs: Float,
    val durationMs: Float,
    val swayPx: Float,
    val sizePx: Float,
    val initialRotation: Float,
    val totalSpin: Float,
    val color: Color,
    val alpha: Float,
)

private val SPRING_COLORS = listOf(
    Color(0xFFF5CED6),
    Color(0xFFE8A3B3),
    Color(0xFFC97A8E),
    Color(0xFFFBF6ED),
)

private fun generatePetals(count: Int): List<PetalData> = List(count) { i ->
    PetalData(
        startXFraction = ((i * 37 + 17) % 100) / 100f,
        delayMs = (i * 2300f) % 12000f,
        durationMs = 14000f + (i * 1700f) % 10000f,
        swayPx = 20f + (i * 7) % 30f,
        sizePx = 14f + (i * 3) % 18f,
        initialRotation = (i * 43) % 360f,
        totalSpin = (if (i % 2 == 0) 1f else -1f) * (60f + (i * 11) % 120f),
        color = SPRING_COLORS[i % SPRING_COLORS.size],
        alpha = 0.5f + ((i * 13) % 40) / 100f,
    )
}

// Sakura petal path in 24×24 viewBox
private fun sakuraPath(scale: Float): Path = Path().apply {
    moveTo(12f * scale, 4f * scale)
    cubicTo(14f * scale, 8f * scale, 18f * scale, 8f * scale, 18f * scale, 12f * scale)
    cubicTo(18f * scale, 16f * scale, 14f * scale, 16f * scale, 12f * scale, 20f * scale)
    cubicTo(10f * scale, 16f * scale, 6f * scale, 16f * scale, 6f * scale, 12f * scale)
    cubicTo(6f * scale, 8f * scale, 10f * scale, 8f * scale, 12f * scale, 4f * scale)
    close()
}

@Composable
fun SumiPetals(
    modifier: Modifier = Modifier,
    count: Int = 12,
) {
    val petals = remember { generatePetals(count) }

    val transition = rememberInfiniteTransition(label = "petals")
    val timeMs by transition.animateFloat(
        initialValue = 0f,
        targetValue = 24_000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "petal_time",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        petals.forEach { petal ->
            val cycleDuration = petal.durationMs
            val cycleProgress = ((timeMs + petal.delayMs) % cycleDuration) / cycleDuration

            val alpha = when {
                cycleProgress < 0.1f -> cycleProgress / 0.1f * petal.alpha
                cycleProgress > 0.9f -> (1f - cycleProgress) / 0.1f * petal.alpha
                else -> petal.alpha
            }
            if (alpha <= 0f) return@forEach

            val x = petal.startXFraction * w + petal.swayPx * sin(cycleProgress * 2f * PI.toFloat())
            val y = -petal.sizePx + cycleProgress * (h + petal.sizePx * 2f)
            val rotation = petal.initialRotation + cycleProgress * petal.totalSpin
            val scale = petal.sizePx / 24f

            withTransform({
                translate(x, y)
                rotate(
                    degrees = rotation,
                    pivot = androidx.compose.ui.geometry.Offset(petal.sizePx / 2f, petal.sizePx / 2f),
                )
            }) {
                drawPath(
                    path = sakuraPath(scale),
                    color = petal.color.copy(alpha = alpha),
                )
            }
        }
    }
}
