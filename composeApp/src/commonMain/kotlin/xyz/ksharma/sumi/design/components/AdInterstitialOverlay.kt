package xyz.ksharma.sumi.design.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private const val DISPLAY_MS = 4000L
private const val FADE_IN_MS = 300
private const val FADE_OUT_MS = 400
private const val FADE_OUT_DELAY_MS = DISPLAY_MS - FADE_OUT_MS

// Full-screen interstitial placeholder shown at natural pause points (every 3rd solve, free users).
// Replace the content of this composable with real AdMob interstitial loading when ad unit IDs
// are available. The dismiss callback and timing contract stay the same.
@Composable
fun AdInterstitialOverlay(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(FADE_IN_MS))
        delay(FADE_OUT_DELAY_MS)
        alpha.animateTo(0f, tween(FADE_OUT_MS))
        currentOnDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha.value }
            .background(SumiTheme.colors.paper),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "広告",
                style = SumiTheme.typography.cjk,
                color = SumiTheme.colors.inkFaint,
            )
            Spacer(Modifier.height(Sumi.Space.s3))
            Text(
                text = "ADVERTISEMENT",
                style = SumiTheme.typography.uiMeta,
                color = SumiTheme.colors.inkFaint,
            )
        }
    }
}
