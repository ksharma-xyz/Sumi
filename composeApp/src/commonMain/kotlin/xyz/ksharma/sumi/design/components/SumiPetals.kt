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
import xyz.ksharma.sumi.a11y.rememberReducedMotion
import xyz.ksharma.sumi.theme.SumiTokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Internal model ────────────────────────────────────────────────────────────

private data class PetalData(
    // Entry position as fraction of canvas size; may be negative (off-screen entry).
    val startXFraction: Float,
    val startYFraction: Float,
    // Normalised wind direction (cos/sin of angle); drives the main travel vector.
    val windDx: Float,
    val windDy: Float,
    // How many screen-widths the petal travels along the wind vector in one cycle.
    val travelScale: Float,
    // Turbulence: dual-frequency sway perpendicular to the wind direction.
    val swayAmp1: Float,
    val swayFreq1: Float,
    val swayAmp2: Float,
    val swayFreq2: Float,
    val swayPhase2: Float,
    val delayFraction: Float,
    val durationMs: Float,
    val sizePx: Float,
    val initialRotation: Float,
    val totalSpin: Float,
    val color: Color,
    val alpha: Float,
)

// Default sakura palette sourced from SumiTokens.Color.Sakura design tokens.
private val SAKURA_COLORS = listOf(
    SumiTokens.Color.Sakura.blush,
    SumiTokens.Color.Sakura.mid,
    SumiTokens.Color.Sakura.deep,
    SumiTokens.Color.Sakura.glow,
)

// ── Generation helpers ────────────────────────────────────────────────────────

/**
 * Generates ambient petals for the infinite splash shower.
 *
 * All petals are wind-blown with a diagonal trajectory (angle 25–65° from horizontal).
 * 40% enter from the left edge; the rest enter from the top. This creates a natural
 * "blizzard" where the wind carries petals across the full screen in varied paths.
 */
private fun generateAmbientPetals(count: Int, colors: List<Color>): List<PetalData> {
    val twoPi = (2f * PI).toFloat()
    return List(count) { i ->
        val a = i * 37 + 17
        val b = i * 53 + 29
        val c = i * 71 + 13
        val d = i * 83 + 41
        val e = i * 97 + 7

        // 40% side-entry (from left), 60% top-entry
        val fromLeft = (i % 10) < 4

        // Wind angle: 25° (nearly horizontal gust) to 65° (steeper fall).
        // Expressed as radians from horizontal-right.
        val angleDeg = 25f + (a % 40).toFloat()
        val angleRad = angleDeg * PI.toFloat() / 180f
        val windDx = cos(angleRad)
        val windDy = sin(angleRad)

        val startXFraction = if (fromLeft) -0.12f else (a % 100) / 100f
        val startYFraction = if (fromLeft) (b % 90) / 100f else -(c % 15) / 100f

        PetalData(
            startXFraction = startXFraction,
            startYFraction = startYFraction,
            windDx = windDx,
            windDy = windDy,
            travelScale = 1.5f + (b % 60) / 100f, // 1.5–2.1× screen width
            swayAmp1 = 45f + (d % 65).toFloat(), // 45–110 px turbulence
            swayFreq1 = 1.5f + (b % 28) / 10f, // 1.5–4.3
            swayAmp2 = 16f + (c % 28).toFloat(), // 16–44 px
            swayFreq2 = 3.5f + (a % 22) / 10f, // 3.5–5.7
            swayPhase2 = (e % 628) / 100f,
            delayFraction = (b % 100) / 100f,
            durationMs = 14000f + (c % 13000).toFloat(), // 14–27 s
            sizePx = 15f + (a % 23).toFloat(), // 15–38 px; varied density
            initialRotation = (b % 360).toFloat(),
            totalSpin = (if (i % 2 == 0) 1f else -1f) * (50f + (c % 150).toFloat()),
            color = colors[i % colors.size],
            alpha = 0.38f + (d % 37) / 100f, // 0.38–0.75
        )
    }
}

/**
 * Generates petals for a one-shot burst (game completion events).
 *
 * Same wind-blizzard physics as ambient petals; each burst is seeded for uniqueness.
 * All petals have a rightward wind push; some enter from the left or mid-screen.
 */
private fun generateBurstPetals(
    count: Int,
    seed: Int,
    sizeMultiplier: Float,
    swayScale: Float,
    colors: List<Color>,
): List<PetalData> {
    val twoPi = (2f * PI).toFloat()
    val rng = kotlin.random.Random(seed)
    return List(count) { i ->
        // Wind angle 30–70° for bursts — slightly steeper to fill the screen faster.
        val angleDeg = 30f + rng.nextFloat() * 40f
        val angleRad = angleDeg * PI.toFloat() / 180f
        val windDx = cos(angleRad)
        val windDy = sin(angleRad)

        // 35% enter from left, rest from top; spread start positions generously.
        val fromLeft = rng.nextFloat() < 0.35f
        val startX = if (fromLeft) -0.15f else rng.nextFloat() * 1.1f - 0.05f
        val startY = if (fromLeft) rng.nextFloat() * 0.9f else -(rng.nextFloat() * 0.15f)

        PetalData(
            startXFraction = startX,
            startYFraction = startY,
            windDx = windDx,
            windDy = windDy,
            travelScale = 1.4f + rng.nextFloat() * 0.8f,
            swayAmp1 = (55f + rng.nextFloat() * 85f) * swayScale,
            swayFreq1 = 1.0f + rng.nextFloat() * 1.5f,
            swayAmp2 = (18f + rng.nextFloat() * 32f) * swayScale,
            swayFreq2 = 3.0f + rng.nextFloat() * 2.5f,
            swayPhase2 = rng.nextFloat() * twoPi,
            delayFraction = rng.nextFloat() * 0.40f,
            durationMs = 1f,
            sizePx = (24f + rng.nextFloat() * 24f) * sizeMultiplier,
            initialRotation = rng.nextFloat() * 360f,
            totalSpin = (if (rng.nextBoolean()) 1f else -1f) * (70f + rng.nextFloat() * 130f),
            color = colors[i % colors.size],
            alpha = 0.55f + rng.nextFloat() * 0.35f,
        )
    }
}

// ── Shared path ───────────────────────────────────────────────────────────────

/**
 * Returns a single sakura-petal outline scaled to [scale].
 * Base shape fits inside a 24×24 unit square.
 */
private fun sakuraPath(scale: Float): Path = Path().apply {
    moveTo(12f * scale, 4f * scale)
    cubicTo(14f * scale, 8f * scale, 18f * scale, 8f * scale, 18f * scale, 12f * scale)
    cubicTo(18f * scale, 16f * scale, 14f * scale, 16f * scale, 12f * scale, 20f * scale)
    cubicTo(10f * scale, 16f * scale, 6f * scale, 16f * scale, 6f * scale, 12f * scale)
    cubicTo(6f * scale, 8f * scale, 10f * scale, 8f * scale, 12f * scale, 4f * scale)
    close()
}

// ── Shared draw engine ────────────────────────────────────────────────────────

/**
 * Draws one petal at [cycleProgress] ∈ [0, 1).
 *
 * Position uses a wind-vector model:
 *   main travel = (windDx, windDy) × travelScale × screenWidth × progress
 *   lateral sway (perpendicular to wind): dual sine oscillators
 *
 * Alpha fades in over [fadeInWindow] and out over [fadeOutWindow] at the tail.
 */
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
    val dist = cycleProgress * petal.travelScale * w

    // Main trajectory along wind direction.
    val baseX = petal.startXFraction * w + petal.windDx * dist
    val baseY = petal.startYFraction * size.height + petal.windDy * dist

    // Turbulence perpendicular to the wind vector (cross-product in 2D = (-windDy, windDx)).
    val sway = petal.swayAmp1 * sin(cycleProgress * petal.swayFreq1 * twoPi) +
        petal.swayAmp2 * sin(cycleProgress * petal.swayFreq2 * twoPi + petal.swayPhase2)
    val x = baseX + (-petal.windDy) * sway
    val y = baseY + petal.windDx * sway

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
 * Default count is 20 (doubled from original 8) for the blizzard feel.
 */
@Composable
fun SumiPetals(
    modifier: Modifier = Modifier,
    count: Int = 20,
    colors: List<Color> = SAKURA_COLORS,
) {
    if (rememberReducedMotion()) return
    val petals = remember(count, colors) { generateAmbientPetals(count, colors) }
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
            renderPetalAtProgress(petal, cycleProgress, fadeInWindow = 0.07f, fadeOutWindow = 0.07f)
        }
    }
}

/**
 * One-shot petal burst triggered by a game completion event.
 *
 * Each new [trigger] > 0 starts a fresh shower with a unique random layout.
 * Use [config] to control count, size, duration, sway, and colour palette.
 */
@Composable
fun SumiPetalBurst(
    trigger: Int,
    modifier: Modifier = Modifier,
    config: PetalBurstConfig = PetalBurstConfig(),
) {
    if (rememberReducedMotion()) return
    val colors = config.colors ?: SAKURA_COLORS
    var petals by remember { mutableStateOf(emptyList<PetalData>()) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        petals = generateBurstPetals(
            count = config.count,
            seed = trigger,
            sizeMultiplier = config.sizeMultiplier,
            swayScale = config.swayScale,
            colors = colors,
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
                val effectiveProgress = (currentProgress - petal.delayFraction).coerceAtLeast(0f)
                if (effectiveProgress <= 0f) return@forEach

                val fadeIn = (effectiveProgress / 0.08f).coerceAtMost(1f)
                val fadeOut = if (currentProgress > 0.80f) {
                    ((1f - currentProgress) / 0.20f).coerceIn(0f, 1f)
                } else {
                    1f
                }
                val alpha = petal.alpha * fadeIn * fadeOut
                if (alpha <= 0f) return@forEach

                val w = size.width
                val dist = effectiveProgress * petal.travelScale * w
                val baseX = petal.startXFraction * w + petal.windDx * dist
                val baseY = petal.startYFraction * size.height + petal.windDy * dist

                val sway = petal.swayAmp1 * sin(effectiveProgress * petal.swayFreq1 * twoPi) +
                    petal.swayAmp2 * sin(effectiveProgress * petal.swayFreq2 * twoPi + petal.swayPhase2)
                val x = baseX + (-petal.windDy) * sway
                val y = baseY + petal.windDx * sway

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
