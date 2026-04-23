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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.FREE_QUOTES
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
)

@Composable
fun HomeScreen(
    onStartGame: (difficulty: String) -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    streakDays: Int = 14,
) {
    val quote = FREE_QUOTES.getOrElse(2) { FREE_QUOTES[0] }

    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Sumi.Space.s6)
                    .padding(top = Sumi.Space.s8, bottom = Sumi.Space.s5),
            ) {
                HomeTopChrome(onSettings = onSettings)
                Spacer(Modifier.height(Sumi.Space.s5))
                DailyQuoteBlock(quote = quote)
                Spacer(Modifier.height(Sumi.Space.s5))
                QuoteRule()
                Spacer(Modifier.height(Sumi.Space.s5))
                StreakCard(streakDays = streakDays)
                Spacer(Modifier.height(Sumi.Space.s4))
                NewPracticeGrid(onStartGame = onStartGame)
            }
            HomeBottomNav()
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
        LogoWordmark(scale = 0.48f, color = Sumi.Color.ink, accent = Sumi.Color.red)
        Spacer(Modifier.weight(1f))
        SumiIcon(
            icon = SumiIcons.Chart,
            contentDescription = "Stats",
            tint = Sumi.Color.inkSoft,
            size = 22.dp,
        )
        Spacer(Modifier.width(Sumi.Space.s4))
        SumiIcon(
            icon = SumiIcons.Settings,
            contentDescription = "Settings",
            tint = Sumi.Color.inkSoft,
            size = 22.dp,
            modifier = Modifier.clickable(
                interactionSource = settingsSrc,
                indication = null,
                onClick = onSettings,
            ),
        )
    }
}

@Composable
private fun DailyQuoteBlock(quote: Quote) {
    Column {
        SumiEyebrow(text = "Today · Practice", color = Sumi.Color.red)
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = "“${quote.text}”",
            style = SumiTheme.typography.quote.copy(lineHeight = 30.sp),
            color = Sumi.Color.ink,
        )
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = "— ${quote.attribution}".uppercase(),
            style = SumiTheme.typography.uiMeta.copy(letterSpacing = 2.sp),
            color = Sumi.Color.gold,
        )
    }
}

@Composable
private fun StreakCard(streakDays: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(width = 1.5.dp, color = Sumi.Color.red, shape = CircleShape)
                .background(Sumi.Color.red.copy(alpha = 0.04f)),
            contentAlignment = Alignment.Center,
        ) {
            SumiIcon(icon = SumiIcons.Flame, contentDescription = null, tint = Sumi.Color.red, size = 28.dp)
        }
        Column {
            Text(
                text = "STREAK",
                style = SumiTheme.typography.uiMeta.copy(letterSpacing = 2.sp),
                color = Sumi.Color.inkFaint,
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$streakDays",
                    style = SumiTheme.typography.h2.copy(
                        fontSize = 32.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight(500),
                    ),
                    color = Sumi.Color.ink,
                )
                Text(
                    text = "days",
                    style = SumiTheme.typography.h3.copy(
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Normal,
                    ),
                    color = Sumi.Color.inkSoft,
                )
            }
            Spacer(Modifier.height(Sumi.Space.s1))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(minOf(streakDays, 14)) { i ->
                    Box(
                        modifier = Modifier
                            .size(width = 8.dp, height = 4.dp)
                            .background(Sumi.Color.red.copy(alpha = 0.5f + i * 0.035f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun NewPracticeGrid(onStartGame: (String) -> Unit) {
    Column {
        SumiEyebrow(text = "New Practice", color = Sumi.Color.inkFaint)
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
                            onClick = { onStartGame(name) },
                            modifier = Modifier.weight(1f),
                        )
                    }
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val src = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .border(1.dp, Sumi.Color.paperEdge)
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(horizontal = Sumi.Space.s3, vertical = Sumi.Space.s3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
    ) {
        Text(
            text = kanji,
            style = SumiTheme.typography.cjk.copy(fontSize = 26.sp),
            color = Sumi.Color.red,
        )
        Column {
            Text(
                text = name,
                style = SumiTheme.typography.h3.copy(
                    fontSize = 17.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight(500),
                ),
                color = Sumi.Color.ink,
            )
            Text(
                text = avgTime,
                style = SumiTheme.typography.uiMeta.copy(letterSpacing = 1.sp),
                color = Sumi.Color.inkFaint,
            )
        }
    }
}

@Composable
private fun HomeBottomNav() {
    val tabs = listOf(
        Triple(SumiIcons.Book, "Play", true),
        Triple(SumiIcons.Calendar, "Daily", false),
        Triple(SumiIcons.Trophy, "Stats", false),
        Triple(SumiIcons.Quote, "Zen", false),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Sumi.Color.paperEdge, shape = androidx.compose.ui.graphics.RectangleShape)
            .padding(top = Sumi.Space.s3, bottom = Sumi.Space.s4),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        tabs.forEach { (icon, label, active) ->
            val tint = if (active) Sumi.Color.ink else Sumi.Color.inkFaint
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SumiIcon(icon = icon, contentDescription = label, tint = tint, size = 22.dp)
                Text(
                    text = label.uppercase(),
                    style = SumiTheme.typography.uiMeta.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = tint,
                )
            }
        }
    }
}
