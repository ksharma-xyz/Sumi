@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.design.components.LogoEnso
import xyz.ksharma.sumi.design.components.LogoWordmark
import xyz.ksharma.sumi.design.components.SumiEyebrow
import xyz.ksharma.sumi.design.components.SumiPetals
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
    val ensoProgress = remember { Animatable(0f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val eyebrowAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { ensoProgress.animateTo(1f, tween(900, easing = Sumi.Ease.brush)) }
            launch {
                delay(600)
                wordmarkAlpha.animateTo(1f, tween(500, easing = Sumi.Ease.paper))
            }
            launch {
                delay(900)
                eyebrowAlpha.animateTo(1f, tween(400, easing = Sumi.Ease.paper))
            }
            launch { delay(1400) }
        }
        uiState.navigationTarget?.let { currentOnNavigate(it) }
    }

    WashiBG(modifier = modifier.fillMaxSize()) {
        SumiPetals(modifier = Modifier.fillMaxSize(), count = 12)
        SplashCenter(ensoProgress.value, wordmarkAlpha.value, eyebrowAlpha.value)
        SplashVersion()
    }
}

@Composable
private fun SplashCenter(ensoProgress: Float, wordmarkAlpha: Float, eyebrowAlpha: Float) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LogoEnso(size = 120.dp, color = Sumi.Color.ink, progress = ensoProgress)
            Spacer(Modifier.height(Sumi.Space.s4))
            LogoWordmark(
                scale = 1.4f,
                color = Sumi.Color.ink,
                accent = Sumi.Color.red,
                modifier = Modifier.alpha(wordmarkAlpha),
            )
            Spacer(Modifier.height(Sumi.Space.s2))
            SumiEyebrow(
                text = "A Quiet Practice",
                color = Sumi.Color.inkSoft,
                modifier = Modifier.alpha(eyebrowAlpha),
            )
        }
    }
}

@Composable
private fun SplashVersion() {
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 70.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = "v 1.0",
            style = SumiTheme.typography.uiMeta.copy(fontSize = 10.sp, letterSpacing = 3.sp),
            color = Sumi.Color.inkFaint.copy(alpha = 0.6f),
        )
    }
}
