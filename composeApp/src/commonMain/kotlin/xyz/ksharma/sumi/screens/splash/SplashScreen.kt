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
    val kanjiAlpha = remember { Animatable(0f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    var dotsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coroutineScope {
            // t=0 — Ensō starts revealing (700ms)
            launch { ensoProgress.animateTo(1f, tween(700, easing = Sumi.Ease.brush)) }
            // t=800 — kanji 墨 fades in (240ms)
            launch {
                delay(800)
                kanjiAlpha.animateTo(1f, tween(240, easing = Sumi.Ease.paper))
            }
            // t=1100 — wordmark fades in (400ms) with subtle lift
            launch {
                delay(1100)
                wordmarkAlpha.animateTo(1f, tween(400, easing = Sumi.Ease.paper))
            }
            // t=1500 — loading dots begin
            launch {
                delay(1500)
                dotsVisible = true
            }
        }
    }

    LaunchedEffect(uiState.navigationTarget) {
        uiState.navigationTarget?.let { currentOnNavigate(it) }
    }

    WashiBG(modifier = modifier.fillMaxSize()) {
        // z=1: ink bleed behind the Ensō
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ink_bleed_01),
                contentDescription = null,
                modifier = Modifier
                    .size(360.dp)
                    .offset(y = (-40).dp),  // nudge to match 40% from top
                contentScale = ContentScale.Fit,
                alpha = 0.12f,
            )
        }

        // z=2–3: Ensō + kanji stack, centered at ~40% from top
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 240.dp),  // ~40% on 600dp tall screen
                contentAlignment = Alignment.TopCenter,
            ) {
                // z=2: Ensō logo (140dp, asset-based)
                LogoEnso(size = 140.dp, color = SumiTheme.colors.ink, progress = ensoProgress.value)

                // z=3: kanji 墨 centered inside the Ensō
                Text(
                    text = "墨",
                    style = SumiTheme.typography.cjk.copy(fontSize = 52.sp),
                    color = SumiTheme.colors.ink,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 38.dp)  // nudge to center within 140dp Ensō
                        .alpha(kanjiAlpha.value),
                )
            }
        }

        // z=4: wordmark "Sumi" below the Ensō
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
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
                    .alpha(wordmarkAlpha.value)
                    .graphicsLayer {
                        translationY = (1f - wordmarkAlpha.value) * 6.dp.toPx()
                    },
            )
        }

        // z=5: loading dots near bottom
        if (dotsVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 48.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                LoadingDots()
            }
        }
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(7) { i ->
            if (i > 0) Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .background(
                        color = SumiTheme.colors.ink.copy(
                            alpha = if (i == activeIndex) 0.60f else 0.20f,
                        ),
                        shape = CircleShape,
                    ),
            )
        }
    }
}
