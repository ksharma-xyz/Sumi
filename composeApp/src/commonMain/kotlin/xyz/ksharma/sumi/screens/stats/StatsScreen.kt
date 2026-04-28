@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.stats

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xyz.ksharma.sumi.design.components.QuoteRule
import xyz.ksharma.sumi.design.components.SumiEyebrow
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.resources.ink_bleed_01
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun StatsScreen(
    state: StatsState,
    modifier: Modifier = Modifier,
) {
    WashiBG(modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(Res.drawable.ink_bleed_01),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.08f,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = Sumi.Space.s6)
                .padding(top = Sumi.Space.s8, bottom = Sumi.Space.s7),
        ) {
            SumiEyebrow(
                text = "練習  Practice Stats",
                color = SumiTheme.colors.red,
                modifier = Modifier.semantics { contentDescription = "Practice Stats" },
            )
            Spacer(Modifier.height(Sumi.Space.s5))
            StatHero(total = state.totalPuzzlesSolved)
            Spacer(Modifier.height(Sumi.Space.s6))
            QuoteRule()
            Spacer(Modifier.height(Sumi.Space.s6))
            StatSummaryRow(state = state)
        }
    }
}

@Composable
private fun StatHero(total: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = "$total puzzles solved, all time" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = total.toString(),
            style = SumiTheme.typography.h1.copy(
                fontSize = 88.sp,
                letterSpacing = (-0.03f).em,
                fontStyle = FontStyle.Italic,
            ),
            color = SumiTheme.colors.ink,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "puzzles solved,\nall time",
            style = SumiTheme.typography.quote.copy(fontSize = 18.sp),
            color = SumiTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatSummaryRow(state: StatsState) {
    val cells = listOf(
        "Streak" to "${state.currentStreak}d",
        "Best" to "${state.bestStreak}d",
        "Days" to state.daysPlayed.toString(),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = SumiTheme.colors.paperEdge),
    ) {
        cells.forEachIndexed { i, (label, value) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (i > 0) Modifier.border(width = 1.dp, color = SumiTheme.colors.paperEdge)
                        else Modifier,
                    )
                    .padding(vertical = Sumi.Space.s4, horizontal = Sumi.Space.s2)
                    .semantics(mergeDescendants = true) {},
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Sumi.Space.s1),
            ) {
                Text(
                    text = label.uppercase(),
                    style = SumiTheme.typography.uiMeta.copy(letterSpacing = 2.sp),
                    color = SumiTheme.colors.inkFaint,
                )
                Text(
                    text = value,
                    style = SumiTheme.typography.numeral.copy(
                        fontSize = 22.sp,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = SumiTheme.colors.ink,
                )
            }
        }
    }
}
