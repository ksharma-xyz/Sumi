@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import xyz.ksharma.sumi.design.components.LogoEnso
import xyz.ksharma.sumi.design.components.SumiPetals
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.resources.ink_bleed_01
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
    var dotsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { runSplashSequence(ensoProgress, wordmarkAlpha) { dotsVisible = true } }
    LaunchedEffect(uiState.navigationTarget) { uiState.navigationTarget?.let { currentOnNavigate(it) } }

    WashiBG(modifier = modifier.fillMaxSize()) {
        SumiPetals(modifier = Modifier.fillMaxSize(), count = 28, sizeMultiplier = 1.8f, speedFactor = 8f)
        SplashInkBleed()
        SplashEnso(ensoProgress.value)
        SplashWordmark(wordmarkAlpha.value)
        if (dotsVisible) SplashDots()
    }
}

private suspend fun runSplashSequence(
    ensoProgress: Animatable<Float, *>,
    wordmarkAlpha: Animatable<Float, *>,
    onDotsReady: () -> Unit,
) = coroutineScope {
    launch { ensoProgress.animateTo(1f, tween(1100, easing = Sumi.Ease.brush)) }
    launch {
        delay(1300)
        wordmarkAlpha.animateTo(1f, tween(800, easing = Sumi.Ease.paper))
    }
    launch {
        delay(2300)
        onDotsReady()
    }
}

@Composable
private fun SplashInkBleed() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(Res.drawable.ink_bleed_01),
            contentDescription = null,
            modifier = Modifier.size(360.dp).offset(y = (-40).dp),
            contentScale = ContentScale.Fit,
            alpha = 0.12f,
        )
    }
}

@Composable
private fun SplashEnso(ensoProgress: Float) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 240.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            LogoEnso(size = 140.dp, color = SumiTheme.colors.ink, progress = ensoProgress)
        }
    }
}

@Composable
private fun SplashWordmark(wordmarkAlpha: Float) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Text(
            text = "Sumi",
            style = SumiTheme.typography.h1.copy(
                fontSize = 46.sp,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.03f).em,
            ),
            color = SumiTheme.colors.ink,
            modifier = Modifier
                .padding(top = 420.dp)
                .alpha(wordmarkAlpha)
                .graphicsLayer { translationY = (1f - wordmarkAlpha) * 14.dp.toPx() },
        )
    }
}

@Composable
private fun SplashDots() {
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 48.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        LoadingDots()
    }
}

@Composable
private fun LoadingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    val tick by transition.animateFloat(
        initialValue = 0f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dotTick",
    )
    val activeIndex = tick.toInt() % 7
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(7) { i ->
            if (i > 0) Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .background(
                        color = SumiTheme.colors.ink.copy(alpha = if (i == activeIndex) 0.60f else 0.20f),
                        shape = CircleShape,
                    ),
            )
        }
    }
}
