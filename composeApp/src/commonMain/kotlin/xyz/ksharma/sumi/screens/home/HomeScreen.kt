@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.Quote
import xyz.ksharma.sumi.design.components.LogoWordmark
import xyz.ksharma.sumi.design.components.QuoteRule
import xyz.ksharma.sumi.design.components.SumiEyebrow
import xyz.ksharma.sumi.design.components.SumiIcon
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private val DIFFICULTY_TILES = listOf(
    Triple("Easy", "一", "3 min"),
    Triple("Medium", "二", "6 min"),
    Triple("Hard", "三", "12 min"),
    Triple("Master", "四", "25 min"),
    Triple("Edo", "五", "45 min"),
)

@Composable
fun HomeScreen(
    streakDays: Int,
    quote: Quote,
    onStartGame: (difficulty: String) -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    lockedDifficulties: Set<String> = emptySet(),
    bottomBanner: (@Composable () -> Unit)? = null,
) {

    WashiBG(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = Sumi.Space.s6)
                    // Reserve room at the bottom so banner-anchored content doesn't overlap
                    // the last difficulty tile when scrolled to the end.
                    .padding(top = Sumi.Space.s4, bottom = if (bottomBanner != null) 64.dp else Sumi.Space.s5),
            ) {
                HomeTopChrome(onSettings = onSettings)
                Spacer(Modifier.height(Sumi.Space.s5))
                DailyQuoteBlock(quote = quote)
                Spacer(Modifier.height(Sumi.Space.s5))
                QuoteRule()
                Spacer(Modifier.height(Sumi.Space.s5))
                StreakCard(streakDays = streakDays)
                Spacer(Modifier.height(Sumi.Space.s4))
                NewPracticeGrid(onStartGame = onStartGame, lockedDifficulties = lockedDifficulties)
            }
            if (bottomBanner != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter,
                ) { bottomBanner() }
            }
        }
    }
}

@Composable
private fun HomeTopChrome(onSettings: () -> Unit) {
    val settingsSrc = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogoWordmark(scale = 0.56f, color = SumiTheme.colors.ink, accent = SumiTheme.colors.red)
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(Sumi.Layout.minTap)
                .clickable(interactionSource = settingsSrc, indication = null, onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) {
            SumiIcon(
                icon = SumiIcons.Settings,
                contentDescription = "Settings",
                tint = SumiTheme.colors.inkSoft,
                size = 22.dp,
            )
        }
    }
}

@Composable
private fun DailyQuoteBlock(quote: Quote) {
    Column {
        SumiEyebrow(text = "Today / Practice", color = SumiTheme.colors.red)
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = "\u201C${quote.text}\u201D",
            style = SumiTheme.typography.quote.copy(lineHeight = 36.sp),
            color = SumiTheme.colors.ink,
        )
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = "— ${quote.attribution}".uppercase(),
            style = SumiTheme.typography.uiMeta.copy(letterSpacing = 2.sp),
            color = SumiTheme.colors.inkSoft,
        )
    }
}

@Composable
private fun StreakCard(streakDays: Int) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = if (streakDays == 1) "1 day streak" else "$streakDays day streak"
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(width = 1.5.dp, color = SumiTheme.colors.red, shape = CircleShape)
                .background(SumiTheme.colors.red.copy(alpha = 0.04f)),
            contentAlignment = Alignment.Center,
        ) {
            SumiIcon(icon = SumiIcons.Flame, contentDescription = null, tint = SumiTheme.colors.red, size = 28.dp)
        }
        Column {
            Text(
                text = "STREAK",
                style = SumiTheme.typography.uiMeta.copy(letterSpacing = 2.sp),
                color = SumiTheme.colors.inkFaint,
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$streakDays",
                    style = SumiTheme.typography.h2.copy(
                        fontSize = 32.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight(500),
                    ),
                    color = SumiTheme.colors.ink,
                )
                Text(
                    text = "days",
                    style = SumiTheme.typography.h3.copy(
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Normal,
                    ),
                    color = SumiTheme.colors.inkSoft,
                )
            }
            Spacer(Modifier.height(Sumi.Space.s1))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(minOf(streakDays, 14)) { i ->
                    Box(
                        modifier = Modifier
                            .size(width = 8.dp, height = 4.dp)
                            .background(SumiTheme.colors.red.copy(alpha = 0.5f + i * 0.035f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun NewPracticeGrid(onStartGame: (String) -> Unit, lockedDifficulties: Set<String>) {
    Column {
        SumiEyebrow(text = "New Practice", color = SumiTheme.colors.inkFaint)
        Spacer(Modifier.height(Sumi.Space.s3))
        val rows = DIFFICULTY_TILES.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(Sumi.Space.s2)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s2)) {
                    row.forEach { (name, kanji, time) ->
                        DifficultyTile(
                            name = name,
                            kanji = kanji,
                            avgTime = time,
                            isLocked = name in lockedDifficulties,
                            onClick = { onStartGame(name) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DifficultyTile(
    name: String,
    kanji: String,
    avgTime: String,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val src = remember { MutableInteractionSource() }
    val borderColor = if (isLocked) SumiTheme.colors.gold else SumiTheme.colors.paperEdge
    val kanjiColor = if (isLocked) SumiTheme.colors.gold else SumiTheme.colors.red
    val tileDescription = if (isLocked) "Start $name game, requires Sumi Pro" else "Start $name game, about $avgTime"
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor)
                .semantics(mergeDescendants = true) { contentDescription = tileDescription }
                .clickable(interactionSource = src, indication = null, onClick = onClick)
                .padding(horizontal = Sumi.Space.s3, vertical = Sumi.Space.s3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
        ) {
            Text(
                text = kanji,
                style = SumiTheme.typography.cjk.copy(fontSize = 30.sp),
                color = kanjiColor,
                modifier = Modifier.semantics { hideFromAccessibility() },
            )
            Column {
                Text(
                    text = name,
                    style = SumiTheme.typography.h3.copy(
                        fontSize = 20.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight(500),
                    ),
                    color = SumiTheme.colors.ink,
                )
                Text(
                    text = if (isLocked) "Sumi Pro" else avgTime,
                    style = SumiTheme.typography.uiMeta.copy(letterSpacing = 1.sp, fontSize = 12.sp),
                    color = if (isLocked) SumiTheme.colors.gold else SumiTheme.colors.inkFaint,
                )
            }
        }
        if (isLocked) {
            Text(
                text = "PRO",
                style = SumiTheme.typography.uiMeta.copy(letterSpacing = 1.sp, fontSize = 9.sp),
                color = SumiTheme.colors.gold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 6.dp),
            )
        }
    }
}
