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
    val startXFraction: Float, // falling: X anchor across width
    val startYFraction: Float, // wind: Y anchor (fraction of screen height, upper area)
    val delayFraction: Float,
    val durationMs: Float,
    val swayAmp1: Float,
    val swayFreq1: Float,
    val swayAmp2: Float,
    val swayFreq2: Float,
    val swayPhase2: Float,
    val sizePx: Float,
    val initialRotation: Float,
    val totalSpin: Float,
    val color: Color,
    val alpha: Float,
    val windCarried: Boolean, // horizontal feather drift vs vertical fall
    val driftRight: Boolean, // wind direction
)

private val PETAL_COLORS = listOf(
    Color(0xFFF5CED6),
    Color(0xFFE8A3B3),
    Color(0xFFC97A8E),
    Color(0xFFFBF6ED),
)

private fun generatePetals(count: Int): List<PetalData> {
    val windCount = maxOf(1, count / 4)
    return List(count) { i ->
        val windCarried = i < windCount
        val a = i * 37 + 17
        val b = i * 53 + 29
        val c = i * 71 + 13
        val d = i * 83 + 41
        val e = i * 97 + 7

        if (windCarried) {
            // Feather-in-wind: drifts horizontally, gentle up/down flutter, upper half of screen
            PetalData(
                startXFraction = 0f,
                startYFraction = 0.08f + (a % 55) / 100f, // 0.08–0.63, upper portion
                delayFraction = (b % 100) / 100f,
                durationMs = 22000f + (c % 14000).toFloat(), // 22–36s, very slow
                swayAmp1 = 28f + (d % 28).toFloat(), // vertical flutter 28–56px
                swayFreq1 = 2f + (b % 18) / 10f, // 2–3.8 waves across screen
                swayAmp2 = 10f + (c % 12).toFloat(),
                swayFreq2 = 4.5f + (a % 20) / 10f,
                swayPhase2 = (e % 628) / 100f,
                sizePx = 18f + (a % 14).toFloat(), // 18–32px, delicate
                initialRotation = (b % 360).toFloat(),
                totalSpin = (if (i % 2 == 0) 1f else -1f) * (18f + (c % 28).toFloat()), // gentle spin
                color = PETAL_COLORS[i % PETAL_COLORS.size],
                alpha = 0.30f + (d % 22) / 100f,
                windCarried = true,
                driftRight = i % 2 == 0,
            )
        } else {
            // Gravity fall: top-to-bottom, dual-frequency lateral sway
            PetalData(
                startXFraction = (a % 100) / 100f,
                startYFraction = 0f,
                delayFraction = (b % 100) / 100f,
                durationMs = 18000f + (c % 12000).toFloat(),
                swayAmp1 = 22f + (d % 38).toFloat(),
                swayFreq1 = 1.2f + (b % 16) / 10f,
                swayAmp2 = 7f + (c % 11).toFloat(),
                swayFreq2 = 3.8f + (a % 22) / 10f,
                swayPhase2 = (e % 628) / 100f,
                sizePx = 26f + (a % 22).toFloat(),
                initialRotation = (b % 360).toFloat(),
                totalSpin = (if (i % 2 == 0) 1f else -1f) * (80f + (c % 100).toFloat()),
                color = PETAL_COLORS[i % PETAL_COLORS.size],
                alpha = 0.45f + (d % 35) / 100f,
                windCarried = false,
                driftRight = false,
            )
        }
    }
}

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
    count: Int = 8,
) {
    val petals = remember(count) { generatePetals(count) }
    val twoPi = (2f * PI).toFloat()

    val transition = rememberInfiniteTransition(label = "petals")
    val timeMs by transition.animateFloat(
        initialValue = 0f,
        targetValue = 60_000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "petal_time",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        petals.forEach { petal ->
            val cycleProgress = ((timeMs / petal.durationMs) + petal.delayFraction) % 1f

            val fadeWindow = 0.08f
            val alpha = when {
                cycleProgress < fadeWindow -> cycleProgress / fadeWindow * petal.alpha
                cycleProgress > 1f - fadeWindow -> (1f - cycleProgress) / fadeWindow * petal.alpha
                else -> petal.alpha
            }
            if (alpha <= 0f) return@forEach

            val x: Float
            val y: Float

            if (petal.windCarried) {
                // Horizontal feather drift — Y stays near anchor with gentle up/down flutter
                x = if (petal.driftRight) {
                    -petal.sizePx + cycleProgress * (w + petal.sizePx * 2f)
                } else {
                    w + petal.sizePx - cycleProgress * (w + petal.sizePx * 2f)
                }
                y = petal.startYFraction * h +
                    petal.swayAmp1 * sin(cycleProgress * petal.swayFreq1 * twoPi) +
                    petal.swayAmp2 * sin(cycleProgress * petal.swayFreq2 * twoPi + petal.swayPhase2)
            } else {
                // Vertical fall — X oscillates around anchor
                val sway = petal.swayAmp1 * sin(cycleProgress * petal.swayFreq1 * twoPi) +
                    petal.swayAmp2 * sin(cycleProgress * petal.swayFreq2 * twoPi + petal.swayPhase2)
                x = petal.startXFraction * w + sway
                y = -petal.sizePx + cycleProgress * (h + petal.sizePx * 2f)
            }

            val rotation = petal.initialRotation + cycleProgress * petal.totalSpin
            val scale = petal.sizePx / 24f

            withTransform({
                translate(x, y)
                rotate(
                    degrees = rotation,
                    pivot = androidx.compose.ui.geometry.Offset(petal.sizePx / 2f, petal.sizePx / 2f),
                )
            }) {
                drawPath(path = sakuraPath(scale), color = petal.color.copy(alpha = alpha))
            }
        }
    }
}
