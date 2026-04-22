package com.sumi.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * SumiTheme — wraps MaterialTheme so existing Compose APIs (Surface, Text, etc.)
 * use Sumi's palette and type by default, but DOES NOT lean on Material's visual
 * language. We override everything. Most of the app should pull from SumiTokens
 * directly rather than MaterialTheme.
 */

data class SumiTypeRoles(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val subhead: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val caption: TextStyle,
    val uiButton: TextStyle,
    val uiLabel: TextStyle,
    val uiMeta: TextStyle,
    val quote: TextStyle,
    val numeral: TextStyle,
    val hand: TextStyle,
    val cjk: TextStyle,
)

val LocalSumiTypography = staticCompositionLocalOf<SumiTypeRoles> {
    error("SumiTypography not provided. Wrap your app in SumiTheme.")
}

@Composable
fun sumiTypography(
    display: FontFamily,
    body: FontFamily,
    ui: FontFamily,
    hand: FontFamily,
    cjk: FontFamily,
): SumiTypeRoles = SumiTypeRoles(
    // Display serif, italic, medium weight — for emotion and headlines.
    h1 = TextStyle(
        fontFamily = display, fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(Sumi.Weight.MEDIUM),
        fontSize = Sumi.Size.h1, letterSpacing = (-0.02f).em,
    ),
    h2 = TextStyle(
        fontFamily = display, fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(Sumi.Weight.MEDIUM),
        fontSize = Sumi.Size.h2, letterSpacing = (-0.015f).em,
    ),
    h3 = TextStyle(
        fontFamily = display, fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(Sumi.Weight.MEDIUM),
        fontSize = Sumi.Size.h3, letterSpacing = (-0.01f).em,
    ),
    subhead = TextStyle(
        fontFamily = display, fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(Sumi.Weight.MEDIUM),
        fontSize = Sumi.Size.subhead,
    ),
    // Body — a readable serif. Regular, no italic.
    body = TextStyle(
        fontFamily = body, fontWeight = FontWeight(Sumi.Weight.REGULAR),
        fontSize = Sumi.Size.body, lineHeight = (Sumi.Size.body.value * 1.6f).sp,
    ),
    bodySmall = TextStyle(
        fontFamily = body, fontWeight = FontWeight(Sumi.Weight.REGULAR),
        fontSize = Sumi.Size.small,
    ),
    caption = TextStyle(
        fontFamily = body, fontWeight = FontWeight(Sumi.Weight.REGULAR),
        fontSize = Sumi.Size.caption,
    ),
    // UI sans — buttons, labels, meta. ALWAYS UPPERCASE + tracked.
    // Compose doesn't auto-uppercase — apply `.uppercase()` at the call site.
    uiButton = TextStyle(
        fontFamily = ui, fontWeight = FontWeight(Sumi.Weight.SEMI),
        fontSize = 13.sp, letterSpacing = Sumi.Track.WIDER.em,
    ),
    uiLabel = TextStyle(
        fontFamily = ui, fontWeight = FontWeight(Sumi.Weight.SEMI),
        fontSize = 11.sp, letterSpacing = Sumi.Track.WIDEST.em,
    ),
    uiMeta = TextStyle(
        fontFamily = ui, fontWeight = FontWeight(Sumi.Weight.MEDIUM),
        fontSize = 10.sp, letterSpacing = Sumi.Track.WIDER.em,
    ),
    // Editorial quote style — italic, generous leading.
    quote = TextStyle(
        fontFamily = display, fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(Sumi.Weight.MEDIUM),
        fontSize = 22.sp, lineHeight = 32.sp,
    ),
    // Board numerals.
    numeral = TextStyle(
        fontFamily = display, fontWeight = FontWeight(Sumi.Weight.SEMI),
        fontSize = 22.sp, letterSpacing = (-0.02f).em,
    ),
    hand = TextStyle(
        fontFamily = hand, fontWeight = FontWeight(Sumi.Weight.MEDIUM),
        fontSize = 24.sp,
    ),
    // Kanji chops — 墨 休 完
    cjk = TextStyle(
        fontFamily = cjk, fontWeight = FontWeight(Sumi.Weight.MEDIUM),
    ),
)

@Composable
fun SumiTheme(
    dark: Boolean = isSystemInDarkTheme(),
    typography: SumiTypeRoles,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (dark) {
        darkColorScheme(
            background = Sumi.Color.Night.paper,
            surface    = Sumi.Color.Night.paperWarm,
            onBackground = Sumi.Color.Night.ink,
            onSurface    = Sumi.Color.Night.ink,
            primary      = Sumi.Color.Night.red,
            onPrimary    = Sumi.Color.Night.paper,
            secondary    = Sumi.Color.Night.teal,
            tertiary     = Sumi.Color.Night.gold,
            error        = Sumi.Color.Night.red,
        )
    } else {
        lightColorScheme(
            background = Sumi.Color.paper,
            surface    = Sumi.Color.paperWarm,
            onBackground = Sumi.Color.ink,
            onSurface    = Sumi.Color.ink,
            primary      = Sumi.Color.red,
            onPrimary    = Sumi.Color.paper,
            secondary    = Sumi.Color.teal,
            tertiary     = Sumi.Color.gold,
            error        = Sumi.Color.red,
        )
    }
    CompositionLocalProvider(LocalSumiTypography provides typography) {
        MaterialTheme(
            colorScheme = colorScheme,
            // We intentionally leave Typography default; never read MaterialTheme.typography.
            // Always go through LocalSumiTypography.current or SumiTypeRoles directly.
            content = content,
        )
    }
}

// Convenience: SumiTheme.typography.body, etc.
object SumiTheme {
    val typography: SumiTypeRoles
        @Composable get() = LocalSumiTypography.current
}

// Missing sp/em extensions placeholder (Compose provides these; listed for clarity).
// import androidx.compose.ui.unit.sp
// import androidx.compose.ui.unit.em
