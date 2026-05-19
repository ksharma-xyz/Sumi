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
import androidx.compose.foundation.layout.absoluteOffset
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import xyz.ksharma.sumi.design.components.InkBleed
import xyz.ksharma.sumi.design.components.SumiIcon
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun StatsScreen(
    state: StatsState,
    onBack: () -> Unit,
    onUnlockPro: () -> Unit,
    onShare: (ImageBitmap) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Offscreen share card → captured on tap of the share icon in StatsHeader.
    val shareLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
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
            StatsHeader(
                onBack = onBack,
                onShare = {
                    coroutineScope.launch { onShare(shareLayer.toImageBitmap()) }
                },
            )
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
        // Offscreen share card — laid out + drawn into shareLayer every
        // composition, never visible. Tap of the header share icon snapshots
        // the layer to a bitmap and hands it to the share sheet.
        Box(
            modifier = Modifier.absoluteOffset(x = (-10_000).dp, y = (-10_000).dp),
        ) {
            StatsShareCard(state = state, layer = shareLayer)
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun StatsHeader(onBack: () -> Unit, onShare: () -> Unit) {
    val backSrc = remember { MutableInteractionSource() }
    val shareSrc = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(interactionSource = backSrc, indication = null, onClick = onBack),
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
        // Share — captures the offscreen StatsShareCard and hands the bitmap up.
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(interactionSource = shareSrc, indication = null, onClick = onShare),
            contentAlignment = Alignment.Center,
        ) {
            SumiIcon(
                icon = SumiIcons.Share,
                contentDescription = "Share stats",
                tint = SumiTheme.colors.ink,
                size = 22.dp,
            )
        }
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
    // Puzzles solved in the last 7 days — a solve count (computed in the VM),
    // not a distinct-day count.
    val solvedThisWeek = state.solvedThisWeek

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
            // heightIn (not fixed height) so the tile grows when system font
            // scale is large — fixed 96dp was clipping the value / label at
            // accessibility text sizes.
            .heightIn(min = 96.dp)
            .border(width = 1.dp, color = SumiTheme.colors.ink.copy(alpha = 0.08f))
            .padding(vertical = Sumi.Space.s4, horizontal = Sumi.Space.s3)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Sumi.Space.s2, Alignment.CenterVertically),
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

/**
 * Plots the user's last N solve times (oldest → newest, left → right). The Y
 * axis is normalised between the min and max time in the window:
 *
 *   y = (time - minTime) / (maxTime - minTime) * height
 *
 * Smaller time = faster = drawn nearer the TOP of the chart (y close to 0).
 * Larger time = slower = drawn nearer the BOTTOM (y close to height). So an
 * improving streak (faster solves over time) reads as a line that climbs up.
 *
 * Source data: [SumiPreferences.observeRecentSolveTimes], a rolling list of the
 * most recent N elapsedMs values appended by [SumiPreferences.recordSolve].
 * The chart is hidden until at least 2 points exist (no useful trend with 1).
 */
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
                // Faster (smaller v) sits NEAR THE TOP. Earlier code had this
                // inverted — the line went down on improvement, opposite of
                // the intent stated in the doc above.
                val y = ((v - minV) / range) * h
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

private val PRO_DIFFICULTIES = setOf(Difficulty.Hard, Difficulty.Master, Difficulty.Edo)

// ── Share card (offscreen, captured to bitmap) ────────────────────────────────

private val StatsShareInk = Color(0xFF2A2218)
private val StatsShareInkSoft = Color(0xFF5A4838)
private val StatsShareInkFaint = Color(0xFF8A7560)
private val StatsSharePaper = Color(0xFFFBF7EC)
private val StatsShareGold = Color(0xFF8A6B2A)

/**
 * Stats summary card rendered offscreen and captured to a bitmap on share-tap.
 * Hardcoded cream/dark palette so the captured PNG looks identical regardless
 * of the user's app theme. Compose primitives only — no painterResource since
 * vector capture is unreliable across platforms.
 */
@Composable
private fun StatsShareCard(state: StatsState, layer: GraphicsLayer) {
    Column(
        modifier = Modifier
            .size(width = 360.dp, height = 600.dp)
            .drawWithContent {
                layer.record {
                    drawRect(color = StatsSharePaper)
                    this@drawWithContent.drawContent()
                }
                drawLayer(layer)
            }
            .padding(Sumi.Space.s7),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Brand line
        Text(
            text = "Sumi",
            style = SumiTheme.typography.quote.copy(fontSize = 22.sp, fontStyle = FontStyle.Italic),
            color = StatsShareInk,
        )
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = "PRACTICE LOG",
            style = SumiTheme.typography.uiLabel.copy(fontSize = 11.sp, letterSpacing = 2.sp),
            color = StatsShareInkFaint,
        )

        Spacer(Modifier.height(Sumi.Space.s5))

        // Hero — total solved
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = state.totalPuzzlesSolved.toString(),
                style = SumiTheme.typography.h1.copy(fontSize = 64.sp, fontStyle = FontStyle.Italic),
                color = StatsShareInk,
            )
            Text(
                text = if (state.totalPuzzlesSolved == 1) "puzzle solved" else "puzzles solved",
                style = SumiTheme.typography.subhead.copy(fontStyle = FontStyle.Italic),
                color = StatsShareInkSoft,
            )
        }

        Spacer(Modifier.height(Sumi.Space.s5))

        // Streak (only header stat alongside total — best times now break out by difficulty below)
        ShareStat(
            value = state.currentStreak.toString(),
            label = "DAY STREAK",
        )

        Spacer(Modifier.height(Sumi.Space.s5))

        // Personal Bests — full list across every difficulty so the share
        // image stands as a complete log, not a one-stat summary.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(StatsShareInkFaint.copy(alpha = 0.5f)),
        )
        Spacer(Modifier.height(Sumi.Space.s3))
        Text(
            text = "PERSONAL BESTS",
            style = SumiTheme.typography.uiLabel.copy(fontSize = 11.sp, letterSpacing = 2.sp),
            color = StatsShareInkFaint,
        )
        Spacer(Modifier.height(Sumi.Space.s3))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Sumi.Space.s2),
        ) {
            Difficulty.entries.forEach { diff ->
                BestTimeRow(
                    kanji = difficultyKanji(diff),
                    label = diff.label,
                    time = state.bestTimes[diff.name],
                )
            }
        }
    }
}

@Composable
private fun BestTimeRow(kanji: String, label: String, time: Long?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = kanji,
            style = SumiTheme.typography.cjk.copy(fontSize = 18.sp),
            color = StatsShareInk,
        )
        Spacer(Modifier.size(Sumi.Space.s3))
        Text(
            text = label,
            style = SumiTheme.typography.body.copy(fontSize = 14.sp),
            color = StatsShareInk,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = time?.let { formatTimeShort(it) } ?: "—",
            style = SumiTheme.typography.numeral.copy(
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
            ),
            color = if (time == null) StatsShareInkFaint else StatsShareGold,
        )
    }
}

@Composable
private fun ShareStat(value: String, label: String, accent: Color = StatsShareInk) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = SumiTheme.typography.numeral.copy(
                fontSize = 32.sp,
                fontStyle = FontStyle.Italic,
            ),
            color = accent,
        )
        Text(
            text = label,
            style = SumiTheme.typography.uiLabel.copy(fontSize = 10.sp, letterSpacing = 2.sp),
            color = StatsShareInkFaint,
        )
    }
}
