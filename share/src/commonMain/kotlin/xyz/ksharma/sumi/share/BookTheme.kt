package xyz.ksharma.sumi.share

/**
 * Paper layer — the page background. Warm cream or deep ink-night.
 */
enum class BookPaper { Light, Dark }

/**
 * Ink layer — the accent colour used for grid lines, difficulty labels, and quote rules.
 * [Sumi] is the default ink (warm dark on light, soft cream on dark).
 * [Gold] is the Pro-only accent.
 */
enum class BookInk { Sumi, Winter, Autumn, Gold }

/**
 * A book theme is a [paper] background + [ink] accent — selected independently.
 */
data class BookTheme(
    val paper: BookPaper = BookPaper.Light,
    val ink: BookInk = BookInk.Sumi,
)
