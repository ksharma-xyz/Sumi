@file:Suppress("MagicNumber", "TooManyFunctions", "LongMethod")

package xyz.ksharma.sumi.screens.stats

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import xyz.ksharma.sumi.design.components.InkBleed
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun StatsScreen(
    state: StatsState,
    onBack: () -> Unit,
    onUnlockPro: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WashiBG(modifier = modifier.fillMaxSize()) {
        // Ink-bleed sits behind the hero number, alpha 0.08, 200dp.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Box(modifier = Modifier.padding(top = 140.dp)) {
                InkBleed(sizeDp = 200.dp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = Sumi.Space.s6)
                .padding(top = Sumi.Space.s2, bottom = Sumi.Space.s7),
        ) {
            StatsHeader(onBack = onBack)
            Spacer(Modifier.height(Sumi.Space.s5))
            StatHero(total = state.totalPuzzlesSolved)
            Spacer(Modifier.height(Sumi.Space.s8))

            SectionLabel("THIS WEEK")
            Spacer(Modifier.height(Sumi.Space.s3))
            ThisWeekGrid(state = state)
            Spacer(Modifier.height(Sumi.Space.s8))

            SectionLabel("IMPROVEMENT")
            Spacer(Modifier.height(Sumi.Space.s3))
            if (state.isPro || state.recentSolveTimes.size >= 3) {
                ImprovementCard(times = state.recentSolveTimes)
            } else {
                LockedCard(
                    title = "Solve a few more puzzles to see your improvement curve.",
                    cta = if (state.isPro) null else "Sumi Pro unlocks the full history",
                    onCta = onUnlockPro,
                )
            }
            Spacer(Modifier.height(Sumi.Space.s8))

            SectionLabel("PERSONAL BESTS")
            Spacer(Modifier.height(Sumi.Space.s3))
            if (state.isPro || state.bestTimes.isNotEmpty()) {
                PersonalBestsList(bestTimes = state.bestTimes, isPro = state.isPro)
            } else {
                LockedCard(
                    title = "Track your fastest solve at every difficulty.",
                    cta = "Sumi Pro keeps every personal best",
                    onCta = onUnlockPro,
                )
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun StatsHeader(onBack: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(interactionSource = src, indication = null, onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "←", style = SumiTheme.typography.h3, color = SumiTheme.colors.ink)
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "Practice Log",
            style = SumiTheme.typography.quote.copy(fontSize = 22.sp, fontStyle = FontStyle.Italic),
            color = SumiTheme.colors.ink,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(48.dp))
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────

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
            style = SumiTheme.typography.quote.copy(fontSize = 18.sp, fontStyle = FontStyle.Italic),
            color = SumiTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
        )
    }
}

// ── This Week 2×2 grid ────────────────────────────────────────────────────────

@Composable
private fun ThisWeekGrid(state: StatsState) {
    // Solved-this-week: count days within the last 7 epoch days.
    val today = thisEpochDay()
    val solvedThisWeek = (0L..6L).count { offset -> (today - offset) in state.solveDays }

    // Avg solve time = mean of last 7 entries in recentSolveTimes.
    val recent7 = state.recentSolveTimes.takeLast(7)
    val avgMs = if (recent7.isEmpty()) 0L else recent7.sum() / recent7.size

    val levelKanji = state.lastDifficulty?.let { name ->
        Difficulty.entries.firstOrNull { it.name == name }?.let { difficultyKanji(it) }
    } ?: "—"

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ThisWeekCell(value = solvedThisWeek.toString(), label = "SOLVED", modifier = Modifier.weight(1f))
            ThisWeekCell(
                value = if (recent7.isEmpty()) "—" else formatTimeShort(avgMs),
                label = "AVG TIME",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ThisWeekCell(
                value = "${state.currentStreak}",
                label = "STREAK",
                modifier = Modifier.weight(1f),
            )
            ThisWeekCell(
                value = levelKanji,
                label = "LEVEL",
                modifier = Modifier.weight(1f),
                isKanji = true,
            )
        }
    }
}

@Composable
private fun ThisWeekCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    isKanji: Boolean = false,
) {
    Column(
        modifier = modifier
            .height(96.dp)
            .border(width = 1.dp, color = SumiTheme.colors.ink.copy(alpha = 0.08f))
            .padding(vertical = Sumi.Space.s4, horizontal = Sumi.Space.s3)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = value,
            style = if (isKanji) SumiTheme.typography.cjk.copy(fontSize = 28.sp)
            else SumiTheme.typography.h2.copy(fontSize = 26.sp, fontStyle = FontStyle.Italic),
            color = SumiTheme.colors.ink,
        )
        Text(
            text = label,
            style = SumiTheme.typography.uiLabel.copy(
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = SumiTheme.colors.inkSoft,
        )
    }
}

// ── Improvement line chart ────────────────────────────────────────────────────

@Composable
private fun ImprovementCard(times: List<Long>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .border(width = 1.dp, color = SumiTheme.colors.ink.copy(alpha = 0.08f))
            .padding(Sumi.Space.s4),
    ) {
        if (times.size < 2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Solve a couple more to see the trend",
                    style = SumiTheme.typography.uiMeta.copy(fontSize = 12.sp),
                    color = SumiTheme.colors.inkSoft,
                )
            }
            return@Column
        }
        val inkColor = SumiTheme.colors.ink
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val maxV = times.max().toFloat()
            val minV = times.min().toFloat()
            val range = (maxV - minV).coerceAtLeast(1f)
            val stepX = w / (times.size - 1).coerceAtLeast(1)

            val path = Path()
            times.forEachIndexed { i, v ->
                val x = stepX * i
                // Lower time = better → invert Y so a faster trend goes UP visually.
                val y = h - ((v - minV) / range) * h
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = inkColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Text(
            text = "${times.size} solves",
            style = SumiTheme.typography.uiMeta.copy(fontSize = 11.sp),
            color = SumiTheme.colors.inkFaint,
        )
    }
}

// ── Personal Bests ────────────────────────────────────────────────────────────

@Composable
private fun PersonalBestsList(bestTimes: Map<String, Long>, isPro: Boolean) {
    val rows = Difficulty.entries.toList()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = SumiTheme.colors.ink.copy(alpha = 0.08f)),
    ) {
        rows.forEachIndexed { idx, diff ->
            val time = bestTimes[diff.name]
            val isLocked = !isPro && diff in PRO_DIFFICULTIES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = Sumi.Space.s4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = difficultyKanji(diff),
                    style = SumiTheme.typography.cjk.copy(fontSize = 28.sp),
                    color = if (isLocked) SumiTheme.colors.inkFaint else SumiTheme.colors.ink,
                )
                Spacer(Modifier.size(Sumi.Space.s4))
                Text(
                    text = diff.label,
                    style = SumiTheme.typography.body.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (isLocked) SumiTheme.colors.inkFaint else SumiTheme.colors.ink,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = when {
                        isLocked -> "Pro"
                        time == null -> "—"
                        else -> formatTimeShort(time)
                    },
                    style = SumiTheme.typography.quote.copy(
                        fontSize = 22.sp,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = if (isLocked) SumiTheme.colors.inkFaint else SumiTheme.colors.ink,
                )
            }
            if (idx < rows.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(SumiTheme.colors.ink.copy(alpha = 0.08f)),
                )
            }
        }
    }
}

// ── Locked card (Pro upsell) ─────────────────────────────────────────────────

@Composable
private fun LockedCard(title: String, cta: String?, onCta: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = SumiTheme.colors.ink.copy(alpha = 0.08f))
            .clickable(interactionSource = src, indication = null, onClick = onCta)
            .padding(Sumi.Space.s5),
        verticalArrangement = Arrangement.spacedBy(Sumi.Space.s2),
    ) {
        Text(
            text = "錠",
            style = SumiTheme.typography.cjk.copy(fontSize = 28.sp),
            color = SumiTheme.colors.gold,
        )
        Text(
            text = title,
            style = SumiTheme.typography.body.copy(fontSize = 14.sp),
            color = SumiTheme.colors.ink,
        )
        if (cta != null) {
            Text(
                text = "$cta  →",
                style = SumiTheme.typography.uiLabel.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = SumiTheme.colors.red,
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = SumiTheme.typography.uiLabel.copy(
            fontSize = 11.sp,
            letterSpacing = 2.5.sp,
            fontWeight = FontWeight.Medium,
        ),
        color = SumiTheme.colors.inkFaint,
    )
}

private fun difficultyKanji(d: Difficulty): String = when (d) {
    Difficulty.Easy -> "一"
    Difficulty.Medium -> "二"
    Difficulty.Hard -> "三"
    Difficulty.Master -> "四"
    Difficulty.Edo -> "五"
}

/** Compact mm:ss for the cells. */
private fun formatTimeShort(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "$min:${sec.toString().padStart(2, '0')}"
}

@OptIn(kotlin.time.ExperimentalTime::class)
private fun thisEpochDay(): Long {
    val now = kotlin.time.Clock.System.now()
    return now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays()
}

private val PRO_DIFFICULTIES = setOf(Difficulty.Hard, Difficulty.Master, Difficulty.Edo)
