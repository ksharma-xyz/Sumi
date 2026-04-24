@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import xyz.ksharma.sumi.design.components.WashiVariant.Faint
import xyz.ksharma.sumi.design.components.WashiVariant.Full
import xyz.ksharma.sumi.design.components.WashiVariant.Quiet
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.resources.washi_ink_dark
import xyz.ksharma.sumi.resources.washi_paper_dark
import xyz.ksharma.sumi.resources.washi_paper_light
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

/**
 * Three-layer paper background: base color → PNG texture → radial vignette.
 * Never use procedural noise — always ship the PNG/JPEG assets.
 */
@Composable
fun WashiBG(
    modifier: Modifier = Modifier,
    variant: WashiVariant = Full,
    inkMode: Boolean = false,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val dark = SumiTheme.isDark
    val resource = when {
        inkMode -> Res.drawable.washi_ink_dark
        dark -> Res.drawable.washi_paper_dark
        else -> Res.drawable.washi_paper_light
    }
    val baseColor = if (dark) Sumi.Color.Night.paper else Sumi.Color.paper

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1 — base color (visible even if PNG fails to load)
        Spacer(Modifier.fillMaxSize().background(baseColor))

        // Layer 2 — washi texture, cropped to fill
        Image(
            painter = painterResource(resource),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = when (variant) {
                Full -> 1f
                Quiet -> 0.7f
                Faint -> 0.35f
            },
        )

        // Layer 3 — inner vignette ring (warm shadow at edges)
        Box(
            Modifier.fillMaxSize().drawBehind {
                val vigColor = if (dark) Color(0x660A0704) else Color(0x33645820)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Transparent, vigColor),
                        center = center,
                        radius = size.maxDimension * 0.8f,
                    ),
                )
            },
        )

        content()
    }
}
