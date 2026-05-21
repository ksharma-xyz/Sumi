package xyz.ksharma.sumi.share

/** One page of a printable puzzle book. */
data class PuzzleBookPage(
    val clues: List<Int>, // 81 ints: 0 = empty cell, 1–9 = given digit
    val solution: List<Int> = emptyList(), // 81 ints: full solved grid (all non-zero)
    val pageNumber: Int,
    val difficulty: String,
    val quote: String = "",
    val quoteAuthor: String = "",
)

/**
 * Full specification for a puzzle book export.
 *
 * [paperPng] and [inkBleedPng] are optional PNG byte arrays — when provided, the platform
 * exporter will draw the washi paper texture as the page background and the ink-bleed
 * graphic as a corner accent on the cover. This keeps the PDF's "paper + ink" feel.
 */
class PuzzleBookSpec(
    val pages: List<PuzzleBookPage>,
    val theme: BookTheme,
    val appName: String = "Sumi",
    val includeAnswers: Boolean = false,
    val paperPng: ByteArray? = null,
    val inkBleedPng: ByteArray? = null,
)
