package xyz.ksharma.sumi.design.components

/**
 * Controls the appearance and timing of a [SumiPetalBurst].
 *
 * @param count Total petals per burst.
 * @param sizeMultiplier Scales base petal sizes. 1f = splash size, 2f = double.
 * @param durationMs How long the burst lasts in milliseconds.
 * @param swayScale Scales the horizontal sway amplitude. 1f = default 60–140px range.
 * @param windPushScale Scales rightward drift per petal. 1f = 10–35% of screen width.
 */
data class PetalBurstConfig(
    val count: Int = 16,
    val sizeMultiplier: Float = 2.0f,
    val durationMs: Int = 3_000,
    val swayScale: Float = 1.0f,
    val windPushScale: Float = 1.0f,
)
