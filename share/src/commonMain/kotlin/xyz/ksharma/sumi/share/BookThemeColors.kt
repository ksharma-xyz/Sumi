@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.share

import androidx.compose.ui.graphics.Color

/** Paper background swatch colour for in-app preview UI. */
fun BookPaper.swatchColor(): Color = when (this) {
    BookPaper.Light -> Color(0xFFFBF7F1)
    BookPaper.Dark -> Color(0xFF1A1210)
}

/**
 * Ink colour for display on the given paper — picks a contrast-correct variant.
 * Used by the theme picker swatches and the in-app PDF preview mock.
 */
fun BookInk.color(paper: BookPaper): Color {
    val isDark = paper == BookPaper.Dark
    return when (this) {
        BookInk.Sumi -> if (isDark) Color(0xFFE8E0D5) else Color(0xFF1A1210)
        BookInk.Winter -> if (isDark) Color(0xFF7AAAD4) else Color(0xFF4A6E8A)
        BookInk.Autumn -> if (isDark) Color(0xFFE69960) else Color(0xFFC45810)
        BookInk.Gold -> if (isDark) Color(0xFFD4A017) else Color(0xFF9C7B0E)
    }
}
