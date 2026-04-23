package xyz.ksharma.sumi.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.design.components.LogoEnso
import xyz.ksharma.sumi.design.components.LogoWordmark
import xyz.ksharma.sumi.design.components.SumiEyebrow
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnNavigate by rememberUpdatedState(onNavigate)
    val ensoAlpha = remember { Animatable(0f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val eyebrowAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        ensoAlpha.animateTo(1f, animationSpec = tween(durationMillis = 900))
        wordmarkAlpha.animateTo(1f, animationSpec = tween(durationMillis = 500, delayMillis = 0))
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
                LogoEnso(
                    size = 120.dp,
                    color = Sumi.Color.ink,
                    modifier = Modifier.alpha(ensoAlpha.value),
                )
                Spacer(Modifier.height(Sumi.Space.s4))
                LogoWordmark(
                    scale = 1f,
                    color = Sumi.Color.ink,
                    accent = Sumi.Color.red,
                    modifier = Modifier.alpha(wordmarkAlpha.value),
                )
                Spacer(Modifier.height(Sumi.Space.s2))
                SumiEyebrow(
                    text = "A daily sudoku",
                    color = Sumi.Color.inkFaint,
                    modifier = Modifier.alpha(eyebrowAlpha.value),
                )
            }
        }
    }
}
