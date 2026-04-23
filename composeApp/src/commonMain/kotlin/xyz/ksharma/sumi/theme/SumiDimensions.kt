package xyz.ksharma.sumi.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import xyz.ksharma.sumi.theme.SumiTokens as Tokens

data class SumiDimensions(
    // ── Spacing ───────────────────────────────────────────────
    val s0: Dp = Tokens.Space.s0,
    val s1: Dp = Tokens.Space.s1,
    val s2: Dp = Tokens.Space.s2,
    val s3: Dp = Tokens.Space.s3,
    val s4: Dp = Tokens.Space.s4,
    val s5: Dp = Tokens.Space.s5,
    val s6: Dp = Tokens.Space.s6,
    val s7: Dp = Tokens.Space.s7,
    val s8: Dp = Tokens.Space.s8,
    val s9: Dp = Tokens.Space.s9,
    val s10: Dp = Tokens.Space.s10,
    val s11: Dp = Tokens.Space.s11,

    // ── Radius ────────────────────────────────────────────────
    val radiusNone: Dp = Tokens.Radius.none,
    val radiusXs: Dp = Tokens.Radius.xs,
    val radiusSm: Dp = Tokens.Radius.sm,
    val radiusMd: Dp = Tokens.Radius.md,
    val radiusLg: Dp = Tokens.Radius.lg,
    val radiusPill: Dp = Tokens.Radius.pill,

    // ── Layout ────────────────────────────────────────────────
    val screenPadX: Dp = Tokens.Layout.screenPadX,
    val screenPadY: Dp = Tokens.Layout.screenPadY,
    val cellSize: Dp = Tokens.Layout.cellSize,
    val minTap: Dp = Tokens.Layout.minTap,
)

val LocalSumiDimensions = staticCompositionLocalOf { SumiDimensions() }

val sumiDimensions = SumiDimensions()
