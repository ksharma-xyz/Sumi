@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.sin

// ── Internal model ────────────────────────────────────────────────────────────

private data class PetalData(
    val startXFraction: Float,
    val startYFraction: Float,
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
    val windCarried: Boolean,
    val driftRight: Boolean,
    val windPushX: Float = 0f,
)

private val PETAL_COLORS = listOf(
    Color(0xFFF5CED6),
    Color(0xFFE8A3B3),
    Color(0xFFC97A8E),
    Color(0xFFFBF6ED),
)

// ── Ambient generation (splash — infinite loop) ───────────────────────────────

private fun generateAmbientPetals(count: Int): List<PetalData> {
    val windCount = maxOf(1, count / 4)
    return List(count) { i ->
        val windCarried = i < windCount
        val a = i * 37 + 17
        val b = i * 53 + 29
        val c = i * 71 + 13
        val d = i * 83 + 41
        val e = i * 97 + 7

        if (windCarried) {
            PetalData(
                startXFraction = 0f,
                startYFraction = 0.08f + (a % 55) / 100f,
                delayFraction = (b % 100) / 100f,
                durationMs = 22000f + (c % 14000).toFloat(),
                swayAmp1 = 28f + (d % 28).toFloat(),
                swayFreq1 = 2f + (b % 18) / 10f,
                swayAmp2 = 10f + (c % 12).toFloat(),
                swayFreq2 = 4.5f + (a % 20) / 10f,
                swayPhase2 = (e % 628) / 100f,
                sizePx = 18f + (a % 14).toFloat(),
                initialRotation = (b % 360).toFloat(),
                totalSpin = (if (i % 2 == 0) 1f else -1f) * (18f + (c % 28).toFloat()),
                color = PETAL_COLORS[i % PETAL_COLORS.size],
                alpha = 0.30f + (d % 22) / 100f,
                windCarried = true,
                driftRight = i % 2 == 0,
            )
        } else {
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

// ── Burst generation (game events — one-shot) ─────────────────────────────────

private fun generateBurstPetals(
    count: Int,
    seed: Int,
    sizeMultiplier: Float,
    swayScale: Float,
    windPushScale: Float,
): List<PetalData> {
    val rng = kotlin.random.Random(seed)
    return List(count) { i ->
        // All burst petals: gravity-fall with rightward wind push.
        // No wind-carried type — that would make them all travel the same left→right path.
        val sizePx = (26f + rng.nextFloat() * 22f) * sizeMultiplier
        PetalData(
            // Fully random X across entire screen — no clustering
            startXFraction = rng.nextFloat(),
            startYFraction = 0f,
            // Stagger entry up to 40% of burst; no normalisation so late petals
            // simply don't complete their full fall (looks natural).
            delayFraction = rng.nextFloat() * 0.40f,
            durationMs = 1f,
            // Wide sway so each petal visibly oscillates left/right
            swayAmp1 = (60f + rng.nextFloat() * 80f) * swayScale,
            swayFreq1 = 1.0f + rng.nextFloat() * 1.2f,
            swayAmp2 = (20f + rng.nextFloat() * 30f) * swayScale,
            swayFreq2 = 3.0f + rng.nextFloat() * 2.0f,
            swayPhase2 = rng.nextFloat() * 6.28f,
            sizePx = sizePx,
            initialRotation = rng.nextFloat() * 360f,
            totalSpin = (if (rng.nextBoolean()) 1f else -1f) * (60f + rng.nextFloat() * 100f),
            color = PETAL_COLORS[i % PETAL_COLORS.size],
            alpha = 0.55f + rng.nextFloat() * 0.35f,
            windCarried = false,
            driftRight = false,
            windPushX = (0.10f + rng.nextFloat() * 0.25f) * windPushScale,
        )
    }
}

// ── Shared path ───────────────────────────────────────────────────────────────

private fun sakuraPath(scale: Float): Path = Path().apply {
    moveTo(12f * scale, 4f * scale)
    cubicTo(14f * scale, 8f * scale, 18f * scale, 8f * scale, 18f * scale, 12f * scale)
    cubicTo(18f * scale, 16f * scale, 14f * scale, 16f * scale, 12f * scale, 20f * scale)
    cubicTo(10f * scale, 16f * scale, 6f * scale, 16f * scale, 6f * scale, 12f * scale)
    cubicTo(6f * scale, 8f * scale, 10f * scale, 8f * scale, 12f * scale, 4f * scale)
    close()
}

// ── Shared draw engine (ambient) ──────────────────────────────────────────────

private fun DrawScope.renderPetalAtProgress(
    petal: PetalData,
    cycleProgress: Float,
    fadeInWindow: Float,
    fadeOutWindow: Float,
) {
    val twoPi = (2f * PI).toFloat()
    val alpha = when {
        cycleProgress < fadeInWindow -> cycleProgress / fadeInWindow * petal.alpha
        cycleProgress > 1f - fadeOutWindow -> (1f - cycleProgress) / fadeOutWindow * petal.alpha
        else -> petal.alpha
    }
    if (alpha <= 0f) return

    val w = size.width
    val h = size.height
    val sway1 = petal.swayAmp1 * sin(cycleProgress * petal.swayFreq1 * twoPi)
    val sway2 = petal.swayAmp2 * sin(cycleProgress * petal.swayFreq2 * twoPi + petal.swayPhase2)

    val x: Float
    val y: Float

    if (petal.windCarried) {
        x = if (petal.driftRight) {
            -petal.sizePx + cycleProgress * (w + petal.sizePx * 2f)
        } else {
            w + petal.sizePx - cycleProgress * (w + petal.sizePx * 2f)
        }
        y = petal.startYFraction * h + sway1 + sway2
    } else {
        x = petal.startXFraction * w + sway1 + sway2 + petal.windPushX * cycleProgress * w
        y = -petal.sizePx + cycleProgress * (h + petal.sizePx * 2f)
    }

    val rotation = petal.initialRotation + cycleProgress * petal.totalSpin
    val scale = petal.sizePx / 24f

    withTransform({
        translate(x, y)
        rotate(degrees = rotation, pivot = Offset(petal.sizePx / 2f, petal.sizePx / 2f))
    }) {
        drawPath(path = sakuraPath(scale), color = petal.color.copy(alpha = alpha))
    }
}

// ── Public composables ────────────────────────────────────────────────────────

/**
 * Ambient infinite petal shower — used on the splash screen background.
 */
@Composable
fun SumiPetals(
    modifier: Modifier = Modifier,
    count: Int = 8,
) {
    val petals = remember(count) { generateAmbientPetals(count) }
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
        petals.forEach { petal ->
            val cycleProgress = ((timeMs / petal.durationMs) + petal.delayFraction) % 1f
            renderPetalAtProgress(petal, cycleProgress, fadeInWindow = 0.08f, fadeOutWindow = 0.08f)
        }
    }
}

/**
 * One-shot petal burst triggered by a game completion event.
 *
 * Each new [trigger] value > 0 starts a fresh 3-second shower with a unique
 * random layout. Wind always blows left → right. Use [config] to control
 * petal count, size, duration, sway strength, and wind intensity.
 */
@Suppress("LongMethod")
@Composable
fun SumiPetalBurst(
    trigger: Int,
    modifier: Modifier = Modifier,
    config: PetalBurstConfig = PetalBurstConfig(),
) {
    var petals by remember { mutableStateOf(emptyList<PetalData>()) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        petals = generateBurstPetals(
            count = config.count,
            seed = trigger,
            sizeMultiplier = config.sizeMultiplier,
            swayScale = config.swayScale,
            windPushScale = config.windPushScale,
        )
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = config.durationMs, easing = LinearEasing))
        petals = emptyList()
    }

    val currentPetals = petals
    val currentProgress = progress.value

    if (currentPetals.isNotEmpty()) {
        Canvas(modifier = modifier) {
            val twoPi = (2f * PI).toFloat()
            currentPetals.forEach { petal ->
                // No normalisation: delayed petals simply don't complete their full fall,
                // which looks natural. Previously they were sped up so they all converged
                // at the bottom simultaneously — that's what caused the "straight line" look.
                val effectiveProgress = (currentProgress - petal.delayFraction).coerceAtLeast(0f)
                if (effectiveProgress <= 0f) return@forEach

                // Fade in quickly per-petal; all petals fade out together in the last 20%.
                val fadeIn = (effectiveProgress / 0.08f).coerceAtMost(1f)
                val fadeOut = if (currentProgress > 0.80f) {
                    ((1f - currentProgress) / 0.20f).coerceIn(0f, 1f)
                } else {
                    1f
                }
                val alpha = petal.alpha * fadeIn * fadeOut
                if (alpha <= 0f) return@forEach

                val sway1 = petal.swayAmp1 * sin(effectiveProgress * petal.swayFreq1 * twoPi)
                val sway2 = petal.swayAmp2 * sin(effectiveProgress * petal.swayFreq2 * twoPi + petal.swayPhase2)
                val x = petal.startXFraction * size.width + sway1 + sway2 +
                    petal.windPushX * effectiveProgress * size.width
                val y = -petal.sizePx + effectiveProgress * (size.height + petal.sizePx * 2f)
                val rotation = petal.initialRotation + effectiveProgress * petal.totalSpin
                val scale = petal.sizePx / 24f

                withTransform({
                    translate(x, y)
                    rotate(degrees = rotation, pivot = Offset(petal.sizePx / 2f, petal.sizePx / 2f))
                }) {
                    drawPath(path = sakuraPath(scale), color = petal.color.copy(alpha = alpha))
                }
            }
        }
    }
}
