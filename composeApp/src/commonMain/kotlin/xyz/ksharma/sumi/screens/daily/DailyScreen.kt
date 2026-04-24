@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.daily

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.design.components.SumiEyebrow
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun DailyScreen(
    solveDays: Set<Long>,
    streak: Int,
    todayEpoch: Long,
    modifier: Modifier = Modifier,
) {

    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = Sumi.Space.s6)
                .padding(top = Sumi.Space.s8, bottom = Sumi.Space.s7),
        ) {
            SumiEyebrow(text = "Practice log", color = SumiTheme.colors.red)
            Spacer(Modifier.height(Sumi.Space.s2))
            Text(
                text = if (streak > 0) "$streak days, quietly." else "Begin the practice.",
                style = SumiTheme.typography.h2.copy(
                    fontSize = 28.sp,
                    fontStyle = FontStyle.Italic,
                ),
                color = SumiTheme.colors.ink,
            )
            Spacer(Modifier.height(Sumi.Space.s6))

            SumiEyebrow(text = "Last 30 days", color = SumiTheme.colors.inkFaint)
            Spacer(Modifier.height(Sumi.Space.s3))
            Heatmap(solveDays = solveDays, todayEpoch = todayEpoch)

            Spacer(Modifier.height(Sumi.Space.s6))
            HeatmapLegend()
        }
    }
}

@Composable
private fun Heatmap(solveDays: Set<Long>, todayEpoch: Long) {
    // 30 days: last 6 rows of 5 cells, reading from oldest (top-left) to today (bottom-right)
    val days = (29 downTo 0).map { offset -> todayEpoch - offset }
    val rows = days.chunked(5)

    Column(verticalArrangement = Arrangement.spacedBy(Sumi.Space.s2)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s2),
            ) {
                row.forEach { epochDay ->
                    HeatCell(
                        epochDay = epochDay,
                        todayEpoch = todayEpoch,
                        isSolved = epochDay in solveDays,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeatCell(
    epochDay: Long,
    todayEpoch: Long,
    isSolved: Boolean,
    modifier: Modifier = Modifier,
) {
    val isToday = epochDay == todayEpoch
    val isFuture = epochDay > todayEpoch

    val bgColor = when {
        isFuture -> SumiTheme.colors.paper
        isSolved -> SumiTheme.colors.paperWarm
        else -> SumiTheme.colors.paper
    }
    val borderColor = when {
        isToday -> SumiTheme.colors.red
        else -> SumiTheme.colors.paperEdge
    }
    val dotColor = when {
        isSolved -> SumiTheme.colors.red
        else -> null
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .background(bgColor)
            .border(width = if (isToday) 1.5.dp else 1.dp, color = borderColor),
        contentAlignment = Alignment.Center,
    ) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(dotColor),
            )
        }
    }
}

@Composable
private fun HeatmapLegend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s1),
        ) {
            Box(Modifier.size(12.dp).background(SumiTheme.colors.paperWarm).border(1.dp, SumiTheme.colors.paperEdge))
            Text(
                text = "Solved",
                style = SumiTheme.typography.uiMeta.copy(fontSize = 10.sp),
                color = SumiTheme.colors.inkFaint,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s1),
        ) {
            Box(Modifier.size(12.dp).background(SumiTheme.colors.paper).border(1.dp, SumiTheme.colors.paperEdge))
            Text(
                text = "Empty",
                style = SumiTheme.typography.uiMeta.copy(fontSize = 10.sp),
                color = SumiTheme.colors.inkFaint,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s1),
        ) {
            Box(Modifier.size(12.dp).background(SumiTheme.colors.paper).border(1.5.dp, SumiTheme.colors.red))
            Text(
                text = "Today",
                style = SumiTheme.typography.uiMeta.copy(fontSize = 10.sp),
                color = SumiTheme.colors.inkFaint,
            )
        }
    }
}
