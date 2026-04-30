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
 * Decorative ink-bleed accent — an organic ink stain on washi paper.
 *
 * The web design system renders this as concentric circles distorted by a
 * `feTurbulence` + `feDisplacementMap` SVG filter, which gives the blot its
 * irregular organic boundary. Compose has no direct equivalent (and a plain
 * `Modifier.blur` over geometric circles reads as a soft glow, not as ink),
 * so we use a pre-rendered ink_bleed PNG — already an organic ink texture —
 * and tint it via `BlendMode.SrcIn` to whatever ink color the caller (or
 * the season) calls for.
 *
 * **Theme-aware**: [color] defaults to [SumiTheme.colors.red], which already
 * resolves to the **current season's accent** (Spring rose / Autumn ochre /
 * Winter indigo) and is automatically lightened toward cream on dark paper.
 * Alpha is bumped on dark mode so the bleed reads at the same visual density.
 */
@Composable
fun InkBleed(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 240.dp,
    color: Color = SumiTheme.colors.red,
    alpha: Float = if (SumiTheme.isDark) 0.30f else 0.22f,
) {
    Image(
        painter = painterResource(Res.drawable.ink_bleed_01),
        contentDescription = null,
        modifier = modifier.size(sizeDp),
        contentScale = ContentScale.Fit,
        // SrcIn replaces the PNG's opaque ink pixels with [color] while
        // preserving the per-pixel alpha that gives the stain its irregular
        // boundary. Without SrcIn the tint would just multiply on top.
        colorFilter = ColorFilter.tint(color, BlendMode.SrcIn),
        alpha = alpha,
    )
}
