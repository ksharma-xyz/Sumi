package xyz.ksharma.sumi.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

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
    error("SumiTypography not provided — wrap your app in SumiTheme.")
}

@Composable
fun sumiTypography(
    display: FontFamily,
    body: FontFamily,
    ui: FontFamily,
    hand: FontFamily,
    cjk: FontFamily,
): SumiTypeRoles = SumiTypeRoles(
    h1 = TextStyle(
        fontFamily = display, fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(SumiTokens.Weight.MEDIUM),
        fontSize = SumiTokens.Size.h1, letterSpacing = (-0.02f).em,
    ),
    h2 = TextStyle(
        fontFamily = display, fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(SumiTokens.Weight.MEDIUM),
        fontSize = SumiTokens.Size.h2, letterSpacing = (-0.015f).em,
    ),
    h3 = TextStyle(
        fontFamily = display, fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(SumiTokens.Weight.MEDIUM),
        fontSize = SumiTokens.Size.h3, letterSpacing = (-0.01f).em,
    ),
    subhead = TextStyle(
        fontFamily = display, fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(SumiTokens.Weight.MEDIUM),
        fontSize = SumiTokens.Size.subhead,
    ),
    body = TextStyle(
        fontFamily = body, fontWeight = FontWeight(SumiTokens.Weight.REGULAR),
        fontSize = SumiTokens.Size.body,
        lineHeight = (SumiTokens.Size.body.value * 1.6f).sp,
    ),
    bodySmall = TextStyle(
        fontFamily = body, fontWeight = FontWeight(SumiTokens.Weight.REGULAR),
        fontSize = SumiTokens.Size.small,
    ),
    caption = TextStyle(
        fontFamily = body, fontWeight = FontWeight(SumiTokens.Weight.REGULAR),
        fontSize = SumiTokens.Size.caption,
    ),
    uiButton = TextStyle(
        fontFamily = ui, fontWeight = FontWeight(SumiTokens.Weight.SEMI),
        fontSize = 13.sp, letterSpacing = SumiTokens.Track.WIDER.em,
    ),
    uiLabel = TextStyle(
        fontFamily = ui, fontWeight = FontWeight(SumiTokens.Weight.SEMI),
        fontSize = 11.sp, letterSpacing = SumiTokens.Track.WIDEST.em,
    ),
    uiMeta = TextStyle(
        fontFamily = ui, fontWeight = FontWeight(SumiTokens.Weight.MEDIUM),
        fontSize = 10.sp, letterSpacing = SumiTokens.Track.WIDER.em,
    ),
    quote = TextStyle(
        fontFamily = display, fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(SumiTokens.Weight.MEDIUM),
        fontSize = 22.sp, lineHeight = 32.sp,
    ),
    numeral = TextStyle(
        fontFamily = display, fontWeight = FontWeight(SumiTokens.Weight.SEMI),
        fontSize = 22.sp, letterSpacing = (-0.02f).em,
    ),
    hand = TextStyle(
        fontFamily = hand, fontWeight = FontWeight(SumiTokens.Weight.MEDIUM),
        fontSize = 24.sp,
    ),
    cjk = TextStyle(
        fontFamily = cjk, fontWeight = FontWeight(SumiTokens.Weight.MEDIUM),
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
            background   = SumiTokens.Color.Night.paper,
            surface      = SumiTokens.Color.Night.paperWarm,
            onBackground = SumiTokens.Color.Night.ink,
            onSurface    = SumiTokens.Color.Night.ink,
            primary      = SumiTokens.Color.Night.red,
            onPrimary    = SumiTokens.Color.Night.paper,
            secondary    = SumiTokens.Color.Night.teal,
            tertiary     = SumiTokens.Color.Night.gold,
            error        = SumiTokens.Color.Night.red,
        )
    } else {
        lightColorScheme(
            background   = SumiTokens.Color.paper,
            surface      = SumiTokens.Color.paperWarm,
            onBackground = SumiTokens.Color.ink,
            onSurface    = SumiTokens.Color.ink,
            primary      = SumiTokens.Color.red,
            onPrimary    = SumiTokens.Color.paper,
            secondary    = SumiTokens.Color.teal,
            tertiary     = SumiTokens.Color.gold,
            error        = SumiTokens.Color.red,
        )
    }
    CompositionLocalProvider(LocalSumiTypography provides typography) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

object SumiTheme {
    val typography: SumiTypeRoles
        @Composable get() = LocalSumiTypography.current
}
