@file:Suppress("MagicNumber", "TooManyFunctions", "LongMethod")

package xyz.ksharma.sumi.screens.daily

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import xyz.ksharma.sumi.design.components.InkBleed
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.theme.SumiTheme
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@OptIn(ExperimentalTime::class)
@Composable
fun DailyScreen(
    state: DailyState,
    onBack: () -> Unit,
    onPlayToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val todayEpoch = remember(today) { today.toEpochDays() }

    WashiBG(modifier = modifier.fillMaxSize()) {
        // Subtle ink-bleed accent in the top-right (handoff: 0.06 alpha, 120dp).
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
            Box(modifier = Modifier.padding(top = 80.dp, end = 24.dp)) {
                InkBleed(sizeDp = 120.dp)
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
            DailyHeader(onBack = onBack)
            Spacer(Modifier.height(Sumi.Space.s4))
            MonthHero(month = today.month, year = today.year, solveDays = state.solveDays, today = today)
            Spacer(Modifier.height(Sumi.Space.s6))
            CalendarGrid(month = today.month, year = today.year, today = today, solveDays = state.solveDays)
            Spacer(Modifier.height(Sumi.Space.s8))
            SectionLabel("TODAY")
            Spacer(Modifier.height(Sumi.Space.s3))
            TodayCard(
                difficulty = state.currentDifficulty,
                puzzleId = todayEpoch,
                status = state.todayStatus,
                onClick = onPlayToday,
            )
            Spacer(Modifier.height(Sumi.Space.s8))
            SectionLabel("PREVIOUS MONTHS")
            Spacer(Modifier.height(Sumi.Space.s3))
            PreviousMonths(today = today, solveDays = state.solveDays)
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun DailyHeader(onBack: () -> Unit) {
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
            Text(
                text = "←",
                style = SumiTheme.typography.h3,
                color = SumiTheme.colors.ink,
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "Daily",
            style = SumiTheme.typography.quote.copy(fontSize = 22.sp, fontStyle = FontStyle.Italic),
            color = SumiTheme.colors.ink,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(48.dp)) // mirror back button so title stays centered
    }
}

// ── Month hero ────────────────────────────────────────────────────────────────

@Composable
private fun MonthHero(month: Month, year: Int, solveDays: Set<Long>, today: LocalDate) {
    // X of Y days solved — counts solve days in the current month, against days elapsed.
    val firstOfMonth = LocalDate(year, month, 1)
    val daysElapsed = (today.toEpochDays() - firstOfMonth.toEpochDays() + 1).toInt()
    val solvedThisMonth = (0 until daysElapsed).count { offset ->
        (firstOfMonth.toEpochDays() + offset) in solveDays
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = month.name.lowercase().replaceFirstChar { it.uppercase() },
            style = SumiTheme.typography.h2.copy(
                fontSize = 36.sp,
                fontStyle = FontStyle.Italic,
            ),
            color = SumiTheme.colors.ink,
        )
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = "$solvedThisMonth of $daysElapsed days solved",
            style = SumiTheme.typography.uiMeta.copy(fontSize = 13.sp),
            color = SumiTheme.colors.inkSoft,
        )
    }
}

// ── Calendar grid ─────────────────────────────────────────────────────────────

@Composable
private fun CalendarGrid(month: Month, year: Int, today: LocalDate, solveDays: Set<Long>) {
    val firstDay = LocalDate(year, month, 1)
    // DayOfWeek.ordinal: Monday=0, ..., Sunday=6 — exactly the slot index we want for the
    // M T W T F S S header row in the grid.
    val firstWeekdayIndex = firstDay.dayOfWeek.ordinal
    val daysInMonth = daysInMonth(year, month)

    // Build a grid of cells: blanks before the 1st, then 1..daysInMonth, padded to fill last row.
    val totalCells = ((firstWeekdayIndex + daysInMonth + 6) / 7) * 7
    val cells: List<Int?> = (0 until totalCells).map { idx ->
        val dayNum = idx - firstWeekdayIndex + 1
        if (dayNum in 1..daysInMonth) dayNum else null
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { letter ->
                Text(
                    text = letter,
                    style = SumiTheme.typography.uiMeta.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = SumiTheme.colors.inkSoft,
                    modifier = Modifier.size(width = 40.dp, height = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(Sumi.Space.s2))
        cells.chunked(7).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { dayNum ->
                    if (dayNum == null) {
                        Spacer(Modifier.size(40.dp))
                    } else {
                        val cellDate = LocalDate(year, month, dayNum)
                        DayCell(
                            day = dayNum,
                            isToday = cellDate == today,
                            isSolved = cellDate.toEpochDays() in solveDays,
                            isFuture = cellDate > today,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, isToday: Boolean, isSolved: Boolean, isFuture: Boolean) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .then(
                if (isToday) {
                    Modifier.border(width = 1.5.dp, color = SumiTheme.colors.red)
                } else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val (style, color) = when {
                isToday -> SumiTheme.typography.quote.copy(
                    fontSize = 17.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold,
                ) to SumiTheme.colors.ink

                isSolved -> SumiTheme.typography.quote.copy(
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                ) to SumiTheme.colors.ink

                isFuture -> SumiTheme.typography.body.copy(fontSize = 12.sp) to
                    SumiTheme.colors.inkSoft.copy(alpha = 0.4f)

                else -> SumiTheme.typography.body.copy(fontSize = 12.sp) to
                    SumiTheme.colors.inkSoft.copy(alpha = 0.7f)
            }
            Text(text = day.toString(), style = style, color = color)
            if (isSolved && !isToday) {
                Spacer(Modifier.height(2.dp))
                Box(modifier = Modifier.size(5.dp).background(SumiTheme.colors.red))
            }
        }
    }
}

// ── Today card ────────────────────────────────────────────────────────────────

@Composable
private fun TodayCard(
    difficulty: Difficulty,
    puzzleId: Long,
    status: TodayStatus,
    onClick: () -> Unit,
) {
    val src = remember { MutableInteractionSource() }
    val kanji = difficultyKanji(difficulty)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // heightIn (not fixed height) so the card grows when system font
            // scale is large — fixed 88dp was clipping the Play CTA at
            // accessibility text sizes.
            .heightIn(min = 88.dp)
            .border(width = 1.dp, color = SumiTheme.colors.ink.copy(alpha = 0.08f))
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(horizontal = Sumi.Space.s5, vertical = Sumi.Space.s4),
        verticalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = kanji,
                style = SumiTheme.typography.cjk.copy(fontSize = 16.sp),
                color = SumiTheme.colors.ink,
            )
            Spacer(Modifier.size(Sumi.Space.s2))
            Text(
                text = "${difficulty.label}  /  #${puzzleId.toString().takeLast(4)}",
                style = SumiTheme.typography.body.copy(fontSize = 13.sp),
                color = SumiTheme.colors.ink,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val (statusText, ctaText) = when (status) {
                TodayStatus.Unstarted -> "Unstarted" to "Play  →"
                is TodayStatus.InProgress -> "In progress  /  ${formatTime(status.elapsedMs)}" to "Continue  →"
                TodayStatus.Done -> "Complete" to "Replay  →"
            }
            Text(
                text = statusText,
                style = SumiTheme.typography.uiMeta.copy(fontSize = 12.sp),
                color = SumiTheme.colors.inkSoft,
            )
            Text(
                text = ctaText,
                style = SumiTheme.typography.uiLabel.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                color = SumiTheme.colors.ink,
            )
        }
    }
}

// ── Previous months ───────────────────────────────────────────────────────────

@OptIn(ExperimentalTime::class)
@Composable
private fun PreviousMonths(today: LocalDate, solveDays: Set<Long>) {
    // Last 3 calendar months excluding the current.
    val months = (1..3).map { offset ->
        val d = today.minus(offset, DateTimeUnit.MONTH)
        d.year to d.month
    }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        months.forEachIndexed { index, (year, month) ->
            val firstOfMonth = LocalDate(year, month, 1)
            val daysInThisMonth = daysInMonth(year, month)
            val solvedThisMonth = (0 until daysInThisMonth).count { offset ->
                (firstOfMonth.toEpochDays() + offset) in solveDays
            }
            PreviousMonthRow(
                monthName = month.name.lowercase().replaceFirstChar { it.uppercase() },
                solved = solvedThisMonth,
                total = daysInThisMonth,
                showDivider = index < months.lastIndex,
            )
        }
    }
}

@Composable
private fun PreviousMonthRow(monthName: String, solved: Int, total: Int, showDivider: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = Sumi.Space.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = monthName,
                style = SumiTheme.typography.quote.copy(fontSize = 18.sp, fontStyle = FontStyle.Italic),
                color = SumiTheme.colors.ink,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "$solved of $total",
                style = SumiTheme.typography.uiMeta.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                color = SumiTheme.colors.inkSoft,
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(SumiTheme.colors.ink.copy(alpha = 0.08f)),
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

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalTime::class)
private fun daysInMonth(year: Int, month: Month): Int {
    // Days in month = date(month+1, day=1) - date(month, day=1) in epoch days.
    val first = LocalDate(year, month, 1)
    val nextMonthFirst = if (month == Month.DECEMBER) LocalDate(year + 1, Month.JANUARY, 1)
    else LocalDate(year, Month.entries[month.ordinal + 1], 1)
    return (nextMonthFirst.toEpochDays() - first.toEpochDays()).toInt()
}
