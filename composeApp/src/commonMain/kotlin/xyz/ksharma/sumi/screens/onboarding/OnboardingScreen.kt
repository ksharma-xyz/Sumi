package xyz.ksharma.sumi.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private data class OnboardingSlide(val title: String, val body: String)

private val SLIDES = listOf(
    OnboardingSlide("Sumi 墨", "The daily practice."),
    OnboardingSlide("Nine lines.\nNine columns.", "One patience."),
    OnboardingSlide("Begin gently.", "Go as far as you like."),
    OnboardingSlide("No ads during play.\nNo noise.", "No streaks to keep."),
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var page by remember { mutableIntStateOf(0) }
    val isLast = page == SLIDES.lastIndex

    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s7),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!isLast) {
                    SumiTextButton(
                        text = "Skip",
                        onClick = onComplete,
                        variant = SumiButtonVariant.Ghost,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            AnimatedContent(targetState = page) { idx ->
                val slide = SLIDES[idx]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = slide.title,
                        style = SumiTheme.typography.h1,
                        color = Sumi.Color.ink,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Sumi.Space.s4))
                    Text(
                        text = slide.body,
                        style = SumiTheme.typography.body,
                        color = Sumi.Color.inkSoft,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                DotIndicator(count = SLIDES.size, current = page)
            }

            Spacer(Modifier.height(Sumi.Space.s5))

            SumiTextButton(
                text = if (isLast) "Begin" else "Continue",
                onClick = { if (isLast) onComplete() else page++ },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DotIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s2),
        modifier = modifier,
    ) {
        repeat(count) { idx ->
            val color = if (idx == current) Sumi.Color.ink else Sumi.Color.inkGhost
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .drawBehind { drawRect(color) },
            )
        }
    }
}
