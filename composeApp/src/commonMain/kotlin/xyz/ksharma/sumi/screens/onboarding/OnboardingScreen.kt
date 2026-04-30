@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import xyz.ksharma.sumi.design.components.PetalBurstConfig
import xyz.ksharma.sumi.design.components.SumiButton
import xyz.ksharma.sumi.design.components.SumiButtonSize
import xyz.ksharma.sumi.design.components.SumiPetalBurst
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

private fun seasonPetalColors(season: SumiSeason): List<Color> {
    val p = SumiTokens.Color.Season.forSeason(season)
    return listOf(p.accent, p.accentDeep, p.paperEdge, p.paper)
}

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
            OnboardingTopBar(skipSrc = skipSrc, accent = accent, onSkip = { onComplete(selectedSeason) })
            // Selected accent threads through the rest of onboarding so the
            // user's first decision visibly carries forward.
            val accent = SumiTokens.Color.Season.forSeason(selectedSeason).accent
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                // Season picker is page 0 — picking the accent first means the
                // remaining onboarding renders in the user's chosen palette.
                // Content slides shift down one position; pass (page - 1) so
                // their headline/body/visual lookups still index 0..n.
                if (page == 0) {
                    SeasonPickerSlide(selected = selectedSeason, onSelect = { selectedSeason = it })
                } else {
                    SlidePage(page = page - 1, accent = accent)
                }
            }
            OnboardingFooter(
                currentPage = pagerState.currentPage,
                isLast = isLast,
                accent = accent,
                onComplete = { onComplete(selectedSeason) },
            )
        }
    }
}

@Composable
private fun OnboardingTopBar(skipSrc: MutableInteractionSource, accent: Color, onSkip: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = "Skip →",
            style = SumiTheme.typography.body.copy(fontSize = 15.sp),
            color = accent,
            modifier = Modifier.clickable(interactionSource = skipSrc, indication = null, onClick = onSkip),
        )
    }
}

@Composable
private fun OnboardingFooter(currentPage: Int, isLast: Boolean, accent: Color, onComplete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(SLIDE_COUNT) { i ->
                if (i > 0) Spacer(Modifier.size(8.dp))
                // Active dot picks up the season accent; inactive dots stay
                // ink-faint so the active one reads as a clear pointer.
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (i == currentPage) accent
                            else SumiTheme.colors.ink.copy(alpha = 0.24f),
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
private fun SlidePage(page: Int, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            when (page) {
                // Enso stays ink — it IS the sumi-ink mark, recolouring it would
                // lose the meaning. The accent shows up on the next two slides.
                0 -> LogoEnso(size = 200.dp, color = SumiTheme.colors.ink)
                1 -> LogoGridDots(size = 180.dp, accent = accent)
                2 -> RestKanji(color = accent)
            }
        }
        Spacer(Modifier.height(28.dp))
        // Ceremonial-numeral eyebrow in the season accent — small, tracked
        // wide. Reinforces the user's choice without competing with the
        // headline for hierarchy.
        Text(
            text = slideEyebrow(page),
            style = SumiTheme.typography.cjk.copy(fontSize = 20.sp, letterSpacing = 6.sp),
            color = accent,
        )
        Spacer(Modifier.height(10.dp))
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
    var burstTrigger by remember { mutableIntStateOf(0) }
    var burstColors by remember { mutableStateOf(seasonPetalColors(SumiSeason.Spring)) }

    Box(modifier = Modifier.fillMaxSize()) {
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
            // 4 tiles × 80dp + 3 × 24dp gap = 392dp, plus side padding. ≥ 420dp
            // wide → all four fit in a single row; narrower (iPhone 14 / SE /
            // mini territory) → 2×2 grid via FlowRow's wrap. maxItemsInEachRow
            // caps it so we never end up with an awkward 3+1 split on
            // borderline widths.
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                val perRow = if (maxWidth >= 420.dp) SumiSeason.entries.size else 2
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    maxItemsInEachRow = perRow,
                ) {
                    SumiSeason.entries.forEach { season ->
                        SeasonTile(
                            season = season,
                            selected = selected == season,
                            onSelect = { picked ->
                                burstColors = seasonPetalColors(picked)
                                burstTrigger++
                                onSelect(picked)
                            },
                        )
                    }
                }
            }
        }
        SumiPetalBurst(
            trigger = burstTrigger,
            modifier = Modifier.fillMaxSize(),
            config = PetalBurstConfig(count = 24, durationMs = 2_500, colors = burstColors),
        )
    }
}

private data class SeasonTileAnim(
    val scale: Float,
    val borderWidth: Dp,
    val borderColor: Color,
    val kanjiColor: Color,
    val labelColor: Color,
)

@Composable
private fun rememberSeasonTileAnim(selected: Boolean, accentColor: Color): SeasonTileAnim {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 0.92f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "tile_scale",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.dp else 1.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "tile_border",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) accentColor else SumiTokens.Color.paperEdge,
        animationSpec = tween(durationMillis = 350),
        label = "tile_border_color",
    )
    val kanjiColor by animateColorAsState(
        targetValue = if (selected) accentColor else SumiTokens.Color.inkSoft,
        animationSpec = tween(durationMillis = 350),
        label = "tile_kanji_color",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) SumiTheme.colors.ink else SumiTheme.colors.inkSoft,
        animationSpec = tween(durationMillis = 350),
        label = "tile_label_color",
    )
    return SeasonTileAnim(scale, borderWidth, borderColor, kanjiColor, labelColor)
}

@Composable
private fun SeasonTile(season: SumiSeason, selected: Boolean, onSelect: (SumiSeason) -> Unit) {
    val palette = SumiTokens.Color.Season.forSeason(season)
    val accentColor = palette.accent
    val src = remember { MutableInteractionSource() }
    val anim = rememberSeasonTileAnim(selected, accentColor)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = anim.scale
                scaleY = anim.scale
            }
            .clip(RoundedCornerShape(8.dp))
            .clickable(interactionSource = src, indication = null) { onSelect(season) },
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(palette.paper)
                .border(anim.borderWidth, anim.borderColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = seasonKanji(season),
                style = SumiTheme.typography.cjk.copy(fontSize = 36.sp),
                color = anim.kanjiColor,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(text = seasonLabel(season), style = SumiTheme.typography.uiLabel, color = anim.labelColor)
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.4f),
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.4f),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.size(4.dp).background(accentColor, CircleShape))
            }
        }
    }
}

private fun seasonKanji(season: SumiSeason) = when (season) {
    SumiSeason.Spring -> "春"
    SumiSeason.Summer -> "夏"
    SumiSeason.Autumn -> "秋"
    SumiSeason.Winter -> "冬"
}

private fun seasonLabel(season: SumiSeason) = when (season) {
    SumiSeason.Spring -> "Spring"
    SumiSeason.Summer -> "Summer"
    SumiSeason.Autumn -> "Autumn"
    SumiSeason.Winter -> "Winter"
}

@Composable
private fun RestKanji(color: Color) {
    Text(
        text = "休",
        style = SumiTheme.typography.cjk.copy(fontSize = 120.sp),
        color = color,
    )
}

/**
 * 3×3 grid of ink dots used on the "Every mark is yours" onboarding slide.
 *
 * Drawn with Compose Canvas using [SumiTheme.colors.ink] so the dots flip cream-coloured
 * on dark paper — the previous static vector drawable hardcoded a dark-ink fill and
 * vanished against the night-paper background in dark mode.
 */
@Composable
private fun LogoGridDots(size: androidx.compose.ui.unit.Dp, accent: Color) {
    val ink = SumiTheme.colors.ink
    // Bottom-centre dot uses the selected season's accent so the user's
    // first choice traces visibly through the rest of onboarding.
    androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
        val unit = this.size.minDimension / 120f
        for (row in 0..2) {
            for (col in 0..2) {
                val isAccentDot = row == 2 && col == 1
                val cx = (30f + col * 30f) * unit
                val cy = (30f + row * 30f) * unit
                drawCircle(
                    color = if (isAccentDot) accent else ink,
                    radius = 9f * unit,
                    center = androidx.compose.ui.geometry.Offset(cx, cy),
                )
            }
        }
    }
}

// Slides 2/3/4 of the four-slide flow (the season picker is slide 1).
private fun slideEyebrow(page: Int) = when (page) {
    0 -> "弐"
    1 -> "参"
    else -> "肆"
}

private fun slideHeadline(page: Int) = when (page) {
    0 -> "Sudoku as quiet practice."
    1 -> "Every mark is yours."
    else -> "Rest is part of the practice."
}

private fun slideBody(page: Int) = when (page) {
    0 -> "Ink on paper. No chrome. No noise.\nNine by nine, every day."
    1 -> "Tap to enter. Hold to pencil.\nYour hand, your pace, your pause."
    // Non-breaking space ( ) glues "without penalty" and "without cost"
    // together so the trailing word never gets stranded on its own line at
    // narrow widths.
    else -> "Pause without penalty. Return without cost.\nThe grid waits."
}
