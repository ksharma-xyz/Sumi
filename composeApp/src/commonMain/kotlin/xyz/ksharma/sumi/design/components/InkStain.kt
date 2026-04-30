@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.resources.ink_bleed_01
import xyz.ksharma.sumi.theme.SumiTheme

/**
 * Organic ink stain — a real ink texture with irregular boundaries, tinted to
 * the seasonal accent. Distinct from [InkBleed], which is a precise three-circle
 * bullseye softened by [Modifier.blur] and reads as a clean geometric accent.
 *
 * Use this where you want the look of ink seeping into washi paper (splash hero,
 * marketing surfaces). Use [InkBleed] where you want a defined bullseye accent
 * (stat panels, daily streak emphasis).
 *
 * Implementation note: the web design system gets organic boundaries from an
 * SVG `feTurbulence` + `feDisplacementMap` filter pipeline. Compose Multiplatform
 * has no direct equivalent, so we ship the result as a pre-rendered PNG and
 * tint it at draw time via `BlendMode.SrcIn`.
 */
@Composable
fun InkStain(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 240.dp,
    color: Color = SumiTheme.colors.red,
    // Bumped to match the marketing site's pink blot — 0.22 was barely
    // visible against the cream paper. The PNG already has soft semi-
    // transparent edges, so high alpha here doesn't read as a hard cut-off.
    alpha: Float = if (SumiTheme.isDark) 0.95f else 0.85f,
) {
    Image(
        painter = painterResource(Res.drawable.ink_bleed_01),
        contentDescription = null,
        modifier = modifier.size(sizeDp),
        contentScale = ContentScale.Fit,
        // SrcIn replaces the PNG's opaque pixels with [color] while preserving
        // the per-pixel alpha that gives the stain its irregular boundary.
        colorFilter = ColorFilter.tint(color, BlendMode.SrcIn),
        alpha = alpha,
    )
}
