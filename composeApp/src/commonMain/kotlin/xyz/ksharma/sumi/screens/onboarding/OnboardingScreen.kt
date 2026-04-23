@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.design.components.LogoEnso
import xyz.ksharma.sumi.design.components.LogoGrid
import xyz.ksharma.sumi.design.components.LogoNine
import xyz.ksharma.sumi.design.components.LogoStrokes
import xyz.ksharma.sumi.design.components.SumiButton
import xyz.ksharma.sumi.design.components.SumiButtonSize
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private data class OnboardingSlide(
    val headline: String,
    val body: String,
    val visual: @Composable () -> Unit,
)

private val SLIDES: List<OnboardingSlide> = listOf(
    OnboardingSlide(
        headline = "Every row,\nevery column,\nevery house.",
        body = "The numbers 一 through 九, each placed once — no more, no less. The rest is patience.",
        visual = { LogoGrid(size = 100.dp) },
    ),
    OnboardingSlide(
        headline = "One digit in\neach house.\nNine times.",
        body = "No guessing. No luck. Every move follows from what you already know.",
        visual = { LogoStrokes(size = 100.dp) },
    ),
    OnboardingSlide(
        headline = "Hint, erase,\nreflect.",
        body = "Take your time. The board waits. Three marks are all you get — use them wisely.",
        visual = { LogoNine(size = 100.dp) },
    ),
    OnboardingSlide(
        headline = "Begin with\nstillness.",
        body = "A daily practice. A moment of quiet. The grid has always been here.",
        visual = { LogoEnso(size = 100.dp) },
    ),
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var page by remember { mutableIntStateOf(0) }
    val slide = SLIDES[page]
    val isLast = page == SLIDES.lastIndex

    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s6)
                .padding(top = Sumi.Space.s8, bottom = Sumi.Space.s7),
        ) {
            ProgressBars(total = SLIDES.size, filled = page + 1)
            Spacer(Modifier.height(Sumi.Space.s8))
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) { slide.visual() }
            Spacer(Modifier.height(Sumi.Space.s7))
            SlideContent(slide = slide)
            Spacer(Modifier.weight(1f))
            OnboardingActions(
                isLast = isLast,
                onContinue = { if (isLast) onComplete() else page++ },
                onSkip = onComplete,
            )
        }
    }
}

@Composable
private fun SlideContent(slide: OnboardingSlide) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = slide.headline,
            style = SumiTheme.typography.h2.copy(
                fontSize = 40.sp,
                lineHeight = (40 * 1.05f).sp,
                letterSpacing = (-0.03f).em,
            ),
            color = SumiTheme.colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Sumi.Space.s4))
        Text(
            text = slide.body,
            style = SumiTheme.typography.body.copy(lineHeight = (15 * 1.6f).sp),
            color = SumiTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OnboardingActions(isLast: Boolean, onContinue: () -> Unit, onSkip: () -> Unit) {
    val skipSrc = remember { MutableInteractionSource() }
    Column(modifier = Modifier.fillMaxWidth()) {
        SumiButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            size = SumiButtonSize.Lg,
        ) {
            Text(
                text = if (isLast) "Begin →" else "Continue",
                style = SumiTheme.typography.uiButton,
            )
        }
        Spacer(Modifier.height(Sumi.Space.s3))
        Text(
            text = "SKIP",
            style = SumiTheme.typography.uiLabel.copy(letterSpacing = 2.sp),
            color = SumiTheme.colors.inkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = skipSrc, indication = null, onClick = onSkip)
                .padding(vertical = Sumi.Space.s2),
        )
    }
}

@Composable
private fun ProgressBars(total: Int, filled: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(if (i < filled) SumiTheme.colors.ink else SumiTheme.colors.paperEdge),
            )
        }
    }
}
