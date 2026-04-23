package xyz.ksharma.sumi.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.alpha
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnNavigate by rememberUpdatedState(onNavigate)
    val wordmarkAlpha = remember { Animatable(0f) }
    val eyebrowAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        wordmarkAlpha.animateTo(1f, animationSpec = tween(durationMillis = 600, delayMillis = 600))
        eyebrowAlpha.animateTo(1f, animationSpec = tween(durationMillis = 400, delayMillis = 0))
    }

    LaunchedEffect(uiState.navigationTarget) {
        uiState.navigationTarget?.let { currentOnNavigate(it) }
    }

    WashiBG(modifier = modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
            ) {
                Text(
                    text = "墨",
                    style = SumiTheme.typography.cjk.copy(fontSize = Sumi.Size.h1),
                    color = Sumi.Color.ink,
                    modifier = Modifier.alpha(wordmarkAlpha.value),
                )
                Spacer(Modifier.height(Sumi.Space.s2))
                Text(
                    text = "Sumi",
                    style = SumiTheme.typography.h2,
                    color = Sumi.Color.ink,
                    modifier = Modifier.alpha(wordmarkAlpha.value),
                )
                Text(
                    text = "A daily practice.",
                    style = SumiTheme.typography.uiMeta,
                    color = Sumi.Color.inkFaint,
                    modifier = Modifier.alpha(eyebrowAlpha.value),
                )
            }
        }
    }
}
