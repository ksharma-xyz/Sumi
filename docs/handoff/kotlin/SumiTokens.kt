package com.sumi.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * SUMI — Design system tokens.
 * Ported from the canonical HTML design system (handoff/reference/sumi/tokens.jsx).
 * Single source of truth. Import from every component, every screen.
 *
 * NOTE: Compose Multiplatform. All members are platform-agnostic.
 */
object SumiTokens {

    // ── Color tokens ──────────────────────────────────────────
    object Color {
        // Paper (light mode — day)
        val paper       = androidx.compose.ui.graphics.Color(0xFFF4ECE0)  // primary surface
        val paperWarm   = androidx.compose.ui.graphics.Color(0xFFEBE0CC)  // secondary / panels
        val paperDeep   = androidx.compose.ui.graphics.Color(0xFFDFD1B6)  // tertiary (washi-darkened)
        val paperEdge   = androidx.compose.ui.graphics.Color(0xFFC9B48A)  // seam / divider
        val paperGlow   = androidx.compose.ui.graphics.Color(0xFFFBF6ED)  // lifted surfaces

        // Ink (primary foreground)
        val ink         = androidx.compose.ui.graphics.Color(0xFF1A1410)
        val inkSoft     = androidx.compose.ui.graphics.Color(0xFF5A4838)
        val inkFaint    = androidx.compose.ui.graphics.Color(0xFF8A7560)
        val inkGhost    = androidx.compose.ui.graphics.Color(0xFFB3A088)

        // Accents (use sparingly)
        val red         = androidx.compose.ui.graphics.Color(0xFFA8342A)  // chop / completion / today
        val redDeep     = androidx.compose.ui.graphics.Color(0xFF7E2820)
        val teal        = androidx.compose.ui.graphics.Color(0xFF2A5A6E)  // user-entered digits
        val tealSoft    = androidx.compose.ui.graphics.Color(0xFF4A7A8E)
        val gold        = androidx.compose.ui.graphics.Color(0xFF8A6B2A)  // Pro / premium
        val goldLight   = androidx.compose.ui.graphics.Color(0xFFC49A4A)

        // States
        val success     = androidx.compose.ui.graphics.Color(0xFF5A7A3A)
        val warning     = androidx.compose.ui.graphics.Color(0xFFA8632A)
        val error       = androidx.compose.ui.graphics.Color(0xFFA8342A)
        val hint        = androidx.compose.ui.graphics.Color(0xFF8A6B2A)

        // Night mode
        object Night {
            val paper       = androidx.compose.ui.graphics.Color(0xFF1A1410)
            val paperWarm   = androidx.compose.ui.graphics.Color(0xFF241C14)
            val paperDeep   = androidx.compose.ui.graphics.Color(0xFF2D241A)
            val paperEdge   = androidx.compose.ui.graphics.Color(0xFF3D3022)
            val ink         = androidx.compose.ui.graphics.Color(0xFFF4ECE0)
            val inkSoft     = androidx.compose.ui.graphics.Color(0xFFB3A088)
            val inkFaint    = androidx.compose.ui.graphics.Color(0xFF8A7560)
            val inkGhost    = androidx.compose.ui.graphics.Color(0xFF5A4838)
            val red         = androidx.compose.ui.graphics.Color(0xFFE84A3E)
            val teal        = androidx.compose.ui.graphics.Color(0xFF6FA8BC)
            val gold        = androidx.compose.ui.graphics.Color(0xFFD9A855)
        }
    }

    // ── Type scale ────────────────────────────────────────────
    // Load fonts as Compose Resources — see FONTS.md for registration.
    object TypeFamily {
        const val DISPLAY = "CormorantGaramond"  // italic serif — headlines, quotes
        const val BODY    = "SourceSerif4"       // body serif — long-form
        const val UI      = "Inter"              // UI sans — buttons, chrome
        const val NUMERAL = "CormorantGaramond"  // board clues
        const val HAND    = "Caveat"             // user-entered digits
        const val CJK     = "ShipporiMincho"     // kanji chops, 墨 休 完
    }

    // Legibility-first scale. See ADAPTIVE.md §3. Nothing below 12sp.
    object Size {
        val caption = 12.sp      // was 11 — min legible
        val small   = 14.sp      // was 12 — min body-s
        val body    = 16.sp      // was 15 — WCAG comfortable
        val bodyLg  = 18.sp      // was 17
        val subhead = 22.sp      // was 20
        val h3      = 28.sp      // was 26
        val h2      = 36.sp      // was 34
        val h1      = 48.sp      // was 46
        val display = 64.sp
        val hero    = 92.sp
        // UI roles
        val uiMeta    = 12.sp
        val uiLabel   = 12.sp
        val uiButton  = 13.sp
    }

    // Italic display at 600 (not 500) — holds weight on small screens.
    // Buttons at 700 — reads as a control, not body.
    object Weight {
        const val LIGHT   = 300
        const val REGULAR = 400
        const val MEDIUM  = 500
        const val SEMI    = 600   // display italic default
        const val BOLD    = 700   // buttons, UI labels
    }

    object Track {  // letter-spacing as em
        const val TIGHT  = -0.03f
        const val SNUG   = -0.01f
        const val NORMAL = 0f
        const val WIDE   = 0.1f
        const val WIDER  = 0.2f
        const val WIDEST = 0.28f
    }

    // ── Space / radius ────────────────────────────────────────
    object Space {
        val s0 = 0.dp;   val s1 = 4.dp;   val s2 = 8.dp;   val s3 = 12.dp
        val s4 = 16.dp;  val s5 = 20.dp;  val s6 = 24.dp;  val s7 = 32.dp
        val s8 = 40.dp;  val s9 = 48.dp;  val s10 = 64.dp; val s11 = 80.dp
    }

    object Radius {
        val none = 0.dp
        val xs   = 2.dp     // default — washi paper edge
        val sm   = 4.dp     // cells, chips
        val md   = 8.dp     // buttons
        val lg   = 12.dp    // sheets
        val pill = 9999.dp
    }

    // ── Motion tokens ─────────────────────────────────────────
    // Everything moves like wet ink settling on paper.
    object Duration {
        const val BRIEF    = 160     // small taps
        const val SHORT    = 260     // button press
        const val BASE     = 380     // screen chrome
        const val BREATH   = 560     // ink bleed settle
        const val SLOW     = 900     // celebrations
        const val CEREMONY = 1600    // aurora sweep, win bloom
    }

    object Ease {
        val paper: Easing = CubicBezierEasing(0.22f, 0.75f, 0.28f, 1f)  // default
        val brush: Easing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
        val bleed: Easing = CubicBezierEasing(0.16f, 0.84f, 0.24f, 1f)
        val snap:  Easing = CubicBezierEasing(0.5f, -0.2f, 0.3f, 1.4f)
    }

    // ── Layout ────────────────────────────────────────────────
    object Layout {
        val screenPadX = 24.dp
        val screenPadY = 28.dp
        val cellSize   = 38.dp   // sudoku cell on phone
        val minTap     = 44.dp   // minimum tap target
    }
}

// Import-friendly top-level alias so you can write `Sumi.Color.ink` in components.
typealias Sumi = SumiTokens
