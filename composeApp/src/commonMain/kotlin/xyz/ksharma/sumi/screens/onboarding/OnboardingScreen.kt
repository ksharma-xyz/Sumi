@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xyz.ksharma.sumi.design.components.LogoEnso
import xyz.ksharma.sumi.design.components.SumiButton
import xyz.ksharma.sumi.design.components.SumiButtonSize
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.resources.ink_bleed_01
import xyz.ksharma.sumi.resources.ink_bleed_02
import xyz.ksharma.sumi.resources.ink_bleed_03
import xyz.ksharma.sumi.theme.SumiSeason
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens

private const val SLIDE_COUNT = 4

private data class BleedConfig(
    val res: DrawableResource,
    val size: Dp,
    val anchor: Alignment,
    val offsetX: Dp,
    val offsetY: Dp,
    val rotation: Float,
    val alpha: Float,
)

private val BLEED_CONFIGS = listOf(
    BleedConfig(Res.drawable.ink_bleed_02, 320.dp, Alignment.TopEnd, 40.dp, 40.dp, 12f, 0.10f),
    BleedConfig(Res.drawable.ink_bleed_03, 300.dp, Alignment.BottomStart, (-60).dp, (-60).dp, -8f, 0.12f),
    BleedConfig(Res.drawable.ink_bleed_01, 360.dp, Alignment.Center, 0.dp, 0.dp, 0f, 0.08f),
    BleedConfig(Res.drawable.ink_bleed_02, 280.dp, Alignment.TopStart, (-20).dp, 20.dp, -6f, 0.08f),
)

@Composable
fun OnboardingScreen(
    onComplete: (SumiSeason) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { SLIDE_COUNT })
    val isLast = pagerState.currentPage == SLIDE_COUNT - 1
    val skipSrc = remember { MutableInteractionSource() }
    var selectedSeason by rememberSaveable { mutableStateOf(SumiSeason.Spring) }

    Box(modifier = modifier.fillMaxSize()) {
        WashiBG(modifier = Modifier.fillMaxSize())
        SlideInkBleed(page = pagerState.currentPage)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp),
        ) {
            OnboardingTopBar(skipSrc = skipSrc, onSkip = { onComplete(selectedSeason) })
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                if (page == SLIDE_COUNT - 1) {
                    SeasonPickerSlide(selected = selectedSeason, onSelect = { selectedSeason = it })
                } else {
                    SlidePage(page = page)
                }
            }
            OnboardingFooter(
                currentPage = pagerState.currentPage,
                isLast = isLast,
                onComplete = { onComplete(selectedSeason) },
            )
        }
    }
}

@Composable
private fun OnboardingTopBar(skipSrc: MutableInteractionSource, onSkip: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = "Skip →",
            style = SumiTheme.typography.body.copy(fontSize = 15.sp),
            color = SumiTheme.colors.inkSoft,
            modifier = Modifier.clickable(interactionSource = skipSrc, indication = null, onClick = onSkip),
        )
    }
}

@Composable
private fun OnboardingFooter(currentPage: Int, isLast: Boolean, onComplete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(SLIDE_COUNT) { i ->
                if (i > 0) Spacer(Modifier.size(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = SumiTheme.colors.ink.copy(alpha = if (i == currentPage) 1f else 0.24f),
                            shape = CircleShape,
                        ),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        SumiButton(onClick = onComplete, modifier = Modifier.fillMaxWidth(), size = SumiButtonSize.Lg) {
            Text(text = if (isLast) "Begin" else "Continue", style = SumiTheme.typography.uiButton)
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun SlideInkBleed(page: Int) {
    val cfg = BLEED_CONFIGS[page]
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = cfg.anchor) {
        Image(
            painter = painterResource(cfg.res),
            contentDescription = null,
            modifier = Modifier.size(cfg.size).offset(x = cfg.offsetX, y = cfg.offsetY).rotate(cfg.rotation),
            contentScale = ContentScale.Fit,
            alpha = cfg.alpha,
        )
    }
}

@Composable
private fun SlidePage(page: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            when (page) {
                0 -> LogoEnso(size = 200.dp, color = SumiTheme.colors.ink)
                1 -> HandDigits()
                2 -> RestKanji()
            }
        }
        Spacer(Modifier.height(40.dp))
        Text(
            text = slideHeadline(page),
            style = SumiTheme.typography.h2.copy(
                fontSize = 34.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = (34 * 1.15f).sp,
                letterSpacing = (-0.02f).em,
            ),
            color = SumiTheme.colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = slideBody(page),
            style = SumiTheme.typography.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
            color = SumiTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SeasonPickerSlide(selected: SumiSeason, onSelect: (SumiSeason) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Choose your season.",
            style = SumiTheme.typography.h2.copy(
                fontSize = 34.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = (34 * 1.15f).sp,
                letterSpacing = (-0.02f).em,
            ),
            color = SumiTheme.colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Sets the accent colour. You can change it in Settings.",
            style = SumiTheme.typography.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = SumiTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(48.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SeasonTile(season = SumiSeason.Spring, selected = selected == SumiSeason.Spring, onSelect = onSelect)
            SeasonTile(season = SumiSeason.Autumn, selected = selected == SumiSeason.Autumn, onSelect = onSelect)
            SeasonTile(season = SumiSeason.Winter, selected = selected == SumiSeason.Winter, onSelect = onSelect)
        }
    }
}

@Composable
private fun SeasonTile(season: SumiSeason, selected: Boolean, onSelect: (SumiSeason) -> Unit) {
    val palette = SumiTokens.Color.Season.forSeason(season)
    val accentColor = palette.accent
    val paperColor = palette.paper
    val kanji = when (season) { SumiSeason.Spring -> "春"
        SumiSeason.Autumn -> "秋"
        SumiSeason.Winter -> "冬" }
    val label = when (season) { SumiSeason.Spring -> "Spring"
        SumiSeason.Autumn -> "Autumn"
        SumiSeason.Winter -> "Winter" }
    val src = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(interactionSource = src, indication = null) { onSelect(season) },
    ) {
        val borderMod: Modifier = if (selected) {
            Modifier.border(2.dp, accentColor, RoundedCornerShape(8.dp))
        } else {
            Modifier.border(1.dp, SumiTokens.Color.paperEdge, RoundedCornerShape(8.dp))
        }
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(paperColor)
                .then(borderMod),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = kanji,
                style = SumiTheme.typography.cjk.copy(fontSize = 36.sp),
                color = if (selected) accentColor else SumiTokens.Color.inkSoft,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = SumiTheme.typography.uiLabel,
            color = if (selected) SumiTheme.colors.ink else SumiTheme.colors.inkSoft,
        )
        if (selected) {
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.size(4.dp).background(accentColor, CircleShape))
        }
    }
}

@Composable
private fun HandDigits() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for ((digit, rotation) in listOf("3" to -6f, "5" to 0f, "9" to 8f)) {
            Text(
                text = digit,
                style = SumiTheme.typography.hand.copy(fontSize = 96.sp),
                color = SumiTheme.colors.ink,
                modifier = Modifier.rotate(rotation),
            )
        }
    }
}

@Composable
private fun RestKanji() {
    Text(
        text = "休",
        style = SumiTheme.typography.cjk.copy(fontSize = 120.sp),
        color = SumiTheme.colors.red,
    )
}

private fun slideHeadline(page: Int) = when (page) {
    0 -> "Sudoku as quiet practice."
    1 -> "Every mark is yours."
    else -> "Rest is part of the practice."
}

private fun slideBody(page: Int) = when (page) {
    0 -> "Ink on paper. No chrome. No streaks.\nNine by nine, every day."
    1 -> "Tap to enter. Hold to pencil.\nYour hand, your pace, your pause."
    else -> "Pause without penalty. Return without cost.\nThe grid waits."
}
