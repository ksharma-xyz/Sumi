package xyz.ksharma.sumi.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SeasonPalette(
    val paper: androidx.compose.ui.graphics.Color,
    val paperWarm: androidx.compose.ui.graphics.Color,
    val paperDeep: androidx.compose.ui.graphics.Color,
    val paperEdge: androidx.compose.ui.graphics.Color,
    val paperGlow: androidx.compose.ui.graphics.Color,
    val accent: androidx.compose.ui.graphics.Color,
    val accentDeep: androidx.compose.ui.graphics.Color,
)

/**
 * Per-mode opacity ramp for the ink-bleed bullseye — three concentric circles, outer to inner.
 * Used by [xyz.ksharma.sumi.design.components.InkBleed] so the same component reads as
 * "bleed at the same visual density" on both cream paper and ink-night paper.
 */
data class InkBleedAlphas(val outer: Float, val middle: Float, val inner: Float)

object SumiTokens {

    object Color {
        // Paper (day)
        val paper = androidx.compose.ui.graphics.Color(0xFFF4ECE0)
        val paperWarm = androidx.compose.ui.graphics.Color(0xFFEBE0CC)
        val paperDeep = androidx.compose.ui.graphics.Color(0xFFDFD1B6)
        val paperEdge = androidx.compose.ui.graphics.Color(0xFFC9B48A)
        val paperGlow = androidx.compose.ui.graphics.Color(0xFFFBF6ED)

        // Ink
        val ink = androidx.compose.ui.graphics.Color(0xFF1A1410)
        val inkSoft = androidx.compose.ui.graphics.Color(0xFF5A4838)
        val inkFaint = androidx.compose.ui.graphics.Color(0xFF8A7560)
        val inkGhost = androidx.compose.ui.graphics.Color(0xFFB3A088)

        // Accents
        val red = androidx.compose.ui.graphics.Color(0xFFA8342A)
        val redDeep = androidx.compose.ui.graphics.Color(0xFF7E2820)
        val teal = androidx.compose.ui.graphics.Color(0xFF2A5A6E)
        val tealSoft = androidx.compose.ui.graphics.Color(0xFF4A7A8E)
        val gold = androidx.compose.ui.graphics.Color(0xFF8A6B2A)
        val goldLight = androidx.compose.ui.graphics.Color(0xFFC49A4A)

        // States
        val success = androidx.compose.ui.graphics.Color(0xFF5A7A3A)
        val warning = androidx.compose.ui.graphics.Color(0xFFA8632A)
        val error = androidx.compose.ui.graphics.Color(0xFFA8342A)
        val hint = androidx.compose.ui.graphics.Color(0xFF8A6B2A)

        // Sakura petal palette — used in ambient and burst petal animations
        object Sakura {
            val blush = androidx.compose.ui.graphics.Color(0xFFF5CED6)
            val mid = androidx.compose.ui.graphics.Color(0xFFE8A3B3)
            val deep = androidx.compose.ui.graphics.Color(0xFFC97A8E)
            val glow = androidx.compose.ui.graphics.Color(0xFFFBF6ED)
        }

        // Ink-bleed tokens — opacity ramps for the [InkBleed] bullseye primitive.
        // Dark-mode ramp is brighter so the bleed reads at the same visual density on
        // ink-night paper (which absorbs more of the alpha).
        object InkBleed {
            val Light = InkBleedAlphas(outer = 0.12f, middle = 0.20f, inner = 0.28f)
            val Dark = InkBleedAlphas(outer = 0.18f, middle = 0.28f, inner = 0.38f)
            val SoftnessLight = 8.dp     // blur radius — subtle bloom on cream paper
            val SoftnessDark = 12.dp     // slightly more diffuse on dark paper
        }

        // Seasonal palettes — paper tints and accent colours per season
        object Season {
            val Spring = SeasonPalette(
                paper = androidx.compose.ui.graphics.Color(0xFFF5ECEA),
                paperWarm = androidx.compose.ui.graphics.Color(0xFFEDE1DC),
                paperDeep = androidx.compose.ui.graphics.Color(0xFFE1D2CC),
                paperEdge = androidx.compose.ui.graphics.Color(0xFFCCAA9E),
                paperGlow = androidx.compose.ui.graphics.Color(0xFFFCF5F3),
                accent = androidx.compose.ui.graphics.Color(0xFFB84568),
                accentDeep = androidx.compose.ui.graphics.Color(0xFF8C2E4C),
            )
            val Autumn = SeasonPalette(
                paper = androidx.compose.ui.graphics.Color(0xFFF4E9D8),
                paperWarm = androidx.compose.ui.graphics.Color(0xFFEBDBC4),
                paperDeep = androidx.compose.ui.graphics.Color(0xFFDFCCB0),
                paperEdge = androidx.compose.ui.graphics.Color(0xFFC8A272),
                paperGlow = androidx.compose.ui.graphics.Color(0xFFFBF3E8),
                accent = androidx.compose.ui.graphics.Color(0xFFB85A28),
                accentDeep = androidx.compose.ui.graphics.Color(0xFF8C3C14),
            )
            val Winter = SeasonPalette(
                paper = androidx.compose.ui.graphics.Color(0xFFEEEEF8),
                paperWarm = androidx.compose.ui.graphics.Color(0xFFE4E6F0),
                paperDeep = androidx.compose.ui.graphics.Color(0xFFD8DAE8),
                paperEdge = androidx.compose.ui.graphics.Color(0xFFA8B0CC),
                paperGlow = androidx.compose.ui.graphics.Color(0xFFF6F6FC),
                accent = androidx.compose.ui.graphics.Color(0xFF4A5CB0),
                accentDeep = androidx.compose.ui.graphics.Color(0xFF333E88),
            )

            fun forSeason(season: SumiSeason): SeasonPalette = when (season) {
                SumiSeason.Spring -> Spring
                SumiSeason.Autumn -> Autumn
                SumiSeason.Winter -> Winter
            }
        }

        object Night {
            val paper = androidx.compose.ui.graphics.Color(0xFF1A1410)
            val paperWarm = androidx.compose.ui.graphics.Color(0xFF241C14)
            val paperDeep = androidx.compose.ui.graphics.Color(0xFF2D241A)
            val paperEdge = androidx.compose.ui.graphics.Color(0xFF3D3022)
            val ink = androidx.compose.ui.graphics.Color(0xFFF4ECE0)
            val inkSoft = androidx.compose.ui.graphics.Color(0xFFB3A088)
            val inkFaint = androidx.compose.ui.graphics.Color(0xFF8A7560)
            val inkGhost = androidx.compose.ui.graphics.Color(0xFF5A4838)
            val red = androidx.compose.ui.graphics.Color(0xFFE84A3E)
            val teal = androidx.compose.ui.graphics.Color(0xFF6FA8BC)
            val gold = androidx.compose.ui.graphics.Color(0xFFD9A855)
        }
    }

    object TypeFamily {
        const val DISPLAY = "CormorantGaramond"
        const val BODY = "SourceSerif4"
        const val UI = "Inter"
        const val NUMERAL = "CormorantGaramond"
        const val HAND = "Caveat"
        const val CJK = "ShipporiMincho"
    }

    object Size {
        val caption = 11.sp
        val small = 12.sp
        val body = 15.sp
        val bodyLg = 17.sp
        val subhead = 20.sp
        val h3 = 26.sp
        val h2 = 34.sp
        val h1 = 46.sp
        val display = 62.sp
        val hero = 88.sp
    }

    object Weight {
        const val LIGHT = 300
        const val REGULAR = 400
        const val MEDIUM = 500
        const val SEMI = 600
        const val BOLD = 700
    }

    object Track {
        const val TIGHT = -0.03f
        const val SNUG = -0.01f
        const val NORMAL = 0f
        const val WIDE = 0.1f
        const val WIDER = 0.2f
        const val WIDEST = 0.28f
    }

    object Space {
        val s0 = 0.dp
        val s1 = 4.dp
        val s2 = 8.dp
        val s3 = 12.dp
        val s4 = 16.dp
        val s5 = 20.dp
        val s6 = 24.dp
        val s7 = 32.dp
        val s8 = 40.dp
        val s9 = 48.dp
        val s10 = 64.dp
        val s11 = 80.dp
    }

    object Radius {
        val none = 0.dp
        val xs = 2.dp
        val sm = 4.dp
        val md = 8.dp
        val lg = 12.dp
        val pill = 9999.dp
    }

    object Duration {
        const val BRIEF = 160
        const val SHORT = 260
        const val BASE = 380
        const val BREATH = 560
        const val SLOW = 900
        const val CEREMONY = 1600
    }

    object Ease {
        val paper: Easing = CubicBezierEasing(0.22f, 0.75f, 0.28f, 1f)
        val brush: Easing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
        val bleed: Easing = CubicBezierEasing(0.16f, 0.84f, 0.24f, 1f)
        val snap: Easing = CubicBezierEasing(0.5f, -0.2f, 0.3f, 1.4f)
    }

    object Layout {
        val screenPadX = 24.dp
        val screenPadY = 28.dp
        val cellSize = 38.dp
        val minTap = 44.dp
    }
}

typealias Sumi = SumiTokens
