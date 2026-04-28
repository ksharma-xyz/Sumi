@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.theme.InkBleedAlphas
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens

/**
 * Ink-bleed bullseye — three concentric red circles with progressive opacity, optionally
 * blurred. Mirrors the SVG primitive used in the Sumi web design system:
 *
 *   r = 18, alpha 0.12  ─┐
 *   r = 12, alpha 0.20  ─┤  all centred on a 60×60 viewBox
 *   r =  6, alpha 0.28  ─┘
 *
 * **Weather + theme aware by default**:
 * - [color] defaults to [SumiTheme.colors.red], which already resolves to the **current
 *   season's accent** (Spring rose / Autumn ochre / Winter indigo) and is automatically
 *   lightened toward cream when dark mode is active.
 * - [alphas] defaults to a denser ramp on dark paper so the bleed reads at the same
 *   visual density on either paper variant.
 * - [softness] also bumps slightly in dark mode for a more diffuse bloom.
 *
 * Pair with one of the `ink_bleed_*.png` resources at low alpha underneath if you want
 * the paper-grain "splatter" character on top of the precise concentric geometry.
 *
 * The web version uses an SVG `feGaussianBlur` + `feTurbulence` filter for the soft "ink"
 * texture; on Compose we approximate the bloom with [Modifier.blur] and let the layered
 * opacities do the rest.
 */
@Composable
fun InkBleed(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 240.dp,
    color: Color = SumiTheme.colors.red,
    alphas: InkBleedAlphas = if (SumiTheme.isDark) SumiTokens.Color.InkBleed.Dark
    else SumiTokens.Color.InkBleed.Light,
    softness: Dp = if (SumiTheme.isDark) SumiTokens.Color.InkBleed.SoftnessDark
    else SumiTokens.Color.InkBleed.SoftnessLight,
) {
    Box(modifier = modifier.size(sizeDp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(softness, BlurredEdgeTreatment.Unbounded),
        ) {
            // Web SVG viewBox is 60×60 with circles at r=18/12/6.
            // Scale that ratio to whatever sizeDp the caller picked so proportions stay identical.
            val unit = size.minDimension / 60f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(color = color.copy(alpha = alphas.outer), radius = 18f * unit, center = center)
            drawCircle(color = color.copy(alpha = alphas.middle), radius = 12f * unit, center = center)
            drawCircle(color = color.copy(alpha = alphas.inner), radius = 6f * unit, center = center)
        }
    }
}
