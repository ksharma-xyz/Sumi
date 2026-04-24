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

data class SumiColors(
    val paper: Color,
    val paperWarm: Color,
    val paperDeep: Color,
    val paperEdge: Color,
    val paperGlow: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val inkGhost: Color,
    val red: Color,
    val redDeep: Color,
    val teal: Color,
    val tealSoft: Color,
    val gold: Color,
    val goldLight: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val hint: Color,
)

private fun sumiLightColors(season: SumiSeason): SumiColors {
    val s = SumiTokens.Color.Season.forSeason(season)
    return SumiColors(
        paper = s.paper,
        paperWarm = s.paperWarm,
        paperDeep = s.paperDeep,
        paperEdge = s.paperEdge,
        paperGlow = s.paperGlow,
        ink = SumiTokens.Color.ink,
        inkSoft = SumiTokens.Color.inkSoft,
        inkFaint = SumiTokens.Color.inkFaint,
        inkGhost = SumiTokens.Color.inkGhost,
        red = s.accent,
        redDeep = s.accentDeep,
        teal = SumiTokens.Color.teal,
        tealSoft = SumiTokens.Color.tealSoft,
        gold = SumiTokens.Color.gold,
        goldLight = SumiTokens.Color.goldLight,
        success = SumiTokens.Color.success,
        warning = SumiTokens.Color.warning,
        error = SumiTokens.Color.error,
        hint = SumiTokens.Color.hint,
    )
}

private val SumiDarkColors = SumiColors(
    paper = SumiTokens.Color.Night.paper,
    paperWarm = SumiTokens.Color.Night.paperWarm,
    paperDeep = SumiTokens.Color.Night.paperDeep,
    paperEdge = SumiTokens.Color.Night.paperEdge,
    paperGlow = SumiTokens.Color.Night.paperDeep,
    ink = SumiTokens.Color.Night.ink,
    inkSoft = SumiTokens.Color.Night.inkSoft,
    inkFaint = SumiTokens.Color.Night.inkFaint,
    inkGhost = SumiTokens.Color.Night.inkGhost,
    red = SumiTokens.Color.Night.red,
    redDeep = SumiTokens.Color.Night.red,
    teal = SumiTokens.Color.Night.teal,
    tealSoft = SumiTokens.Color.Night.teal,
    gold = SumiTokens.Color.Night.gold,
    goldLight = SumiTokens.Color.Night.gold,
    success = SumiTokens.Color.success,
    warning = SumiTokens.Color.warning,
    error = SumiTokens.Color.Night.red,
    hint = SumiTokens.Color.Night.gold,
)

val LocalSumiColors = staticCompositionLocalOf<SumiColors> {
    error("SumiColors not provided — wrap your app in SumiTheme.")
}

val LocalSumiIsDark = staticCompositionLocalOf { false }

val LocalSumiSeason = staticCompositionLocalOf { SumiSeason.Spring }

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
    h1 = displayStyle(display, SumiTokens.Size.h1, (-0.02f).em),
    h2 = displayStyle(display, SumiTokens.Size.h2, (-0.015f).em),
    h3 = displayStyle(display, SumiTokens.Size.h3, (-0.01f).em),
    subhead = displayStyle(display, SumiTokens.Size.subhead, 0f.em),
    body = bodyStyle(body, SumiTokens.Size.body, (SumiTokens.Size.body.value * 1.6f).sp),
    bodySmall = bodyStyle(body, SumiTokens.Size.small),
    caption = bodyStyle(body, SumiTokens.Size.caption),
    uiButton = uiStyle(ui, 13.sp, SumiTokens.Track.WIDER.em, SumiTokens.Weight.SEMI),
    uiLabel = uiStyle(ui, 11.sp, SumiTokens.Track.WIDEST.em, SumiTokens.Weight.SEMI),
    uiMeta = uiStyle(ui, 10.sp, SumiTokens.Track.WIDER.em, SumiTokens.Weight.MEDIUM),
    quote = TextStyle(
        fontFamily = display,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(SumiTokens.Weight.MEDIUM),
        fontSize = 22.sp,
        lineHeight = 32.sp,
    ),
    numeral = TextStyle(
        fontFamily = display,
        fontWeight = FontWeight(SumiTokens.Weight.SEMI),
        fontSize = 22.sp,
        letterSpacing = (-0.02f).em,
    ),
    hand = TextStyle(fontFamily = hand, fontWeight = FontWeight(SumiTokens.Weight.MEDIUM), fontSize = 24.sp),
    cjk = TextStyle(fontFamily = cjk, fontWeight = FontWeight(SumiTokens.Weight.MEDIUM)),
)

private fun displayStyle(family: FontFamily, size: androidx.compose.ui.unit.TextUnit, tracking: androidx.compose.ui.unit.TextUnit) =
    TextStyle(
        fontFamily = family,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight(SumiTokens.Weight.MEDIUM),
        fontSize = size,
        letterSpacing = tracking,
    )

private fun bodyStyle(
    family: FontFamily,
    size: androidx.compose.ui.unit.TextUnit,
    leading: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight(SumiTokens.Weight.REGULAR),
    fontSize = size,
    lineHeight = leading,
)

private fun uiStyle(family: FontFamily, size: androidx.compose.ui.unit.TextUnit, tracking: androidx.compose.ui.unit.TextUnit, weight: Int) =
    TextStyle(fontFamily = family, fontWeight = FontWeight(weight), fontSize = size, letterSpacing = tracking)

@Composable
fun SumiTheme(
    typography: SumiTypeRoles,
    season: SumiSeason = SumiSeason.Spring,
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val sumiColors = if (dark) SumiDarkColors else sumiLightColors(season)
    val colorScheme = if (dark) {
        darkColorScheme(
            background = sumiColors.paper,
            surface = sumiColors.paperWarm,
            onBackground = sumiColors.ink,
            onSurface = sumiColors.ink,
            primary = sumiColors.red,
            onPrimary = sumiColors.paper,
            secondary = sumiColors.teal,
            tertiary = sumiColors.gold,
            error = sumiColors.red,
        )
    } else {
        lightColorScheme(
            background = sumiColors.paper,
            surface = sumiColors.paperWarm,
            onBackground = sumiColors.ink,
            onSurface = sumiColors.ink,
            primary = sumiColors.red,
            onPrimary = sumiColors.paper,
            secondary = sumiColors.teal,
            tertiary = sumiColors.gold,
            error = sumiColors.red,
        )
    }
    CompositionLocalProvider(
        LocalSumiColors provides sumiColors,
        LocalSumiIsDark provides dark,
        LocalSumiSeason provides season,
        LocalSumiTypography provides typography,
        LocalSumiDimensions provides sumiDimensions,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

object SumiTheme {
    val colors: SumiColors
        @Composable get() = LocalSumiColors.current
    val isDark: Boolean
        @Composable get() = LocalSumiIsDark.current
    val season: SumiSeason
        @Composable get() = LocalSumiSeason.current
    val typography: SumiTypeRoles
        @Composable get() = LocalSumiTypography.current
    val dimensions: SumiDimensions
        @Composable get() = LocalSumiDimensions.current
}
