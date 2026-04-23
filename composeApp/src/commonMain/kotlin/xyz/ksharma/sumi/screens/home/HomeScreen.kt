package xyz.ksharma.sumi.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.FREE_QUOTES
import xyz.ksharma.sumi.design.components.LogoChop
import xyz.ksharma.sumi.design.components.QuoteRule
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiChip
import xyz.ksharma.sumi.design.components.SumiChipTone
import xyz.ksharma.sumi.design.components.SumiEyebrow
import xyz.ksharma.sumi.design.components.SumiIcon
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun HomeScreen(
    onStartGame: (difficulty: String) -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    streakDays: Int = 0,
) {
    val dailyQuote = FREE_QUOTES[0]

    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopBar(streakDays = streakDays, onSettings = onSettings)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Sumi.Space.s6)
                    .padding(bottom = Sumi.Space.s9),
            ) {
                Spacer(Modifier.height(Sumi.Space.s4))

                // Daily card
                DailyCard(onStartGame = { onStartGame("Medium") })

                Spacer(Modifier.height(Sumi.Space.s6))

                // Quick start
                SumiEyebrow(text = "Quick Start")
                Spacer(Modifier.height(Sumi.Space.s3))
                Row(horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s3)) {
                    SumiTextButton(
                        text = "Easy",
                        onClick = { onStartGame("Easy") },
                        variant = SumiButtonVariant.Ghost,
                        modifier = Modifier.weight(1f),
                    )
                    SumiTextButton(
                        text = "Medium",
                        onClick = { onStartGame("Medium") },
                        variant = SumiButtonVariant.Ghost,
                        modifier = Modifier.weight(1f),
                    )
                    SumiTextButton(
                        text = "Hard",
                        onClick = { onStartGame("Hard") },
                        variant = SumiButtonVariant.Ghost,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(Sumi.Space.s7))

                // Daily quote
                QuoteRule()
                Spacer(Modifier.height(Sumi.Space.s4))
                Text(
                    text = "\u201C${dailyQuote.text}\u201D",
                    style = SumiTheme.typography.quote,
                    color = Sumi.Color.inkSoft,
                )
                Spacer(Modifier.height(Sumi.Space.s2))
                Text(
                    text = "— ${dailyQuote.attribution}",
                    style = SumiTheme.typography.caption,
                    color = Sumi.Color.inkFaint,
                )
                Spacer(Modifier.height(Sumi.Space.s4))
                QuoteRule()
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    streakDays: Int,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogoChop(size = 28.dp)
        Spacer(Modifier.weight(1f))
        if (streakDays > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s1),
            ) {
                SumiIcon(
                    icon = SumiIcons.Flame,
                    contentDescription = "Streak",
                    tint = Sumi.Color.red,
                    size = 16.dp,
                )
                Text(
                    text = "$streakDays days",
                    style = SumiTheme.typography.uiMeta,
                    color = Sumi.Color.inkFaint,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        val settingsSource = remember { MutableInteractionSource() }
        SumiIcon(
            icon = SumiIcons.Settings,
            contentDescription = "Settings",
            tint = Sumi.Color.inkFaint,
            size = 20.dp,
            modifier = Modifier.clickable(
                interactionSource = settingsSource,
                indication = null,
                onClick = onSettings,
            ),
        )
    }
}

@Composable
private fun DailyCard(onStartGame: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Sumi.Color.paperWarm)
            .border(1.dp, Sumi.Color.paperEdge)
            .padding(Sumi.Space.s5),
    ) {
        SumiEyebrow(text = "Today · Daily puzzle")
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = "The Threshold",
            style = SumiTheme.typography.h2,
            color = Sumi.Color.ink,
        )
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = "A hand-crafted puzzle for today.",
            style = SumiTheme.typography.bodySmall,
            color = Sumi.Color.inkSoft,
        )
        Spacer(Modifier.height(Sumi.Space.s3))
        Row(horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s2)) {
            SumiChip(text = "Medium", tone = SumiChipTone.Red)
            SumiChip(text = "Hand-crafted", tone = SumiChipTone.Ink)
        }
        Spacer(Modifier.height(Sumi.Space.s4))
        SumiTextButton(
            text = "Begin Today's Puzzle →",
            onClick = onStartGame,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
