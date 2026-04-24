package xyz.ksharma.sumi.design.components

import androidx.compose.ui.graphics.Color

/**
 * Controls the appearance and timing of a [SumiPetalBurst].
 *
 * @param count Total petals per burst.
 * @param sizeMultiplier Scales base petal sizes. 1f = splash size, 2f = double.
 * @param durationMs How long the burst lasts in milliseconds.
 * @param swayScale Scales the horizontal sway amplitude. 1f = default 60–140px range.
 * @param colors Optional override palette; null falls back to the default sakura palette.
 */
data class PetalBurstConfig(
    val count: Int = 32,
    val sizeMultiplier: Float = 2.0f,
    val durationMs: Int = 3_000,
    val swayScale: Float = 1.0f,
    val colors: List<Color>? = null,
)
