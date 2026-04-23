@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.win

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.Quote
import xyz.ksharma.sumi.design.components.SealComplete
import xyz.ksharma.sumi.design.components.SumiButton
import xyz.ksharma.sumi.design.components.SumiButtonSize
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiEyebrow
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun WinScreen(
    elapsedMs: Long,
    mistakeCount: Int,
    difficulty: String,
    streakDays: Int,
    quote: Quote,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    onNextPuzzle: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize()) {
        WashiBG(modifier = Modifier.fillMaxSize())
        InkBleedAccent(
            color = SumiTheme.colors.red,
            size = 180.dp,
            seed = 1,
            modifier = Modifier.padding(start = 30.dp, top = 80.dp),
        )
        InkBleedAccent(
            color = SumiTheme.colors.teal,
            size = 160.dp,
            seed = 2,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 20.dp).padding(top = 320.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s6)
                .padding(top = Sumi.Space.s11, bottom = Sumi.Space.s7),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WinHeroSection(difficulty = difficulty)
            Spacer(Modifier.height(Sumi.Space.s7))
            WinStatsRow(
                timeDisplay = formatTime(elapsedMs),
                mistakeCount = mistakeCount,
                streakDays = streakDays,
            )
            Spacer(Modifier.height(Sumi.Space.s5))
            Text(
                text = "“${quote.text}”",
                style = SumiTheme.typography.quote.copy(fontSize = 14.sp),
                color = SumiTheme.colors.inkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Sumi.Space.s4),
            )
            Spacer(Modifier.weight(1f))
            WinActions(onNextPuzzle = onNextPuzzle, onHome = onHome)
        }
    }
}

@Composable
private fun WinHeroSection(difficulty: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SumiEyebrow(text = "完 · Complete · $difficulty", color = SumiTheme.colors.red)
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = "Sumi",
            style = SumiTheme.typography.h1.copy(fontSize = 68.sp, letterSpacing = (-0.03f).em),
            color = SumiTheme.colors.ink,
        )
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = "The grid is quiet again.",
            style = SumiTheme.typography.body.copy(fontSize = 14.sp, fontStyle = FontStyle.Italic),
            color = SumiTheme.colors.inkSoft,
        )
        Spacer(Modifier.height(Sumi.Space.s5))
        SealComplete(size = 80.dp)
    }
}

@Composable
private fun WinStatsRow(timeDisplay: String, mistakeCount: Int, streakDays: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = SumiTheme.colors.paperEdge),
    ) {
        listOf(
            "Time" to timeDisplay,
            "Marks" to mistakeCount.toString(),
            "Streak" to "${streakDays}d",
        ).forEachIndexed { i, (label, value) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (i > 0) {
                            Modifier.border(width = 1.dp, color = SumiTheme.colors.paperEdge)
                        } else {
                            Modifier
                        },
                    )
                    .padding(vertical = Sumi.Space.s4, horizontal = Sumi.Space.s2),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = label.uppercase(),
                    style = SumiTheme.typography.uiMeta.copy(letterSpacing = 2.sp),
                    color = SumiTheme.colors.inkFaint,
                )
                Spacer(Modifier.height(Sumi.Space.s1))
                Text(
                    text = value,
                    style = SumiTheme.typography.numeral.copy(
                        fontSize = 24.sp,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = SumiTheme.colors.ink,
                )
            }
        }
    }
}

@Composable
private fun WinActions(onNextPuzzle: (() -> Unit)?, onHome: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Sumi.Space.s2)) {
        if (onNextPuzzle != null) {
            SumiButton(
                onClick = onNextPuzzle,
                modifier = Modifier.fillMaxWidth(),
                size = SumiButtonSize.Lg,
            ) {
                Text(text = "Next Practice →", style = SumiTheme.typography.uiButton)
            }
        }
        SumiButton(
            onClick = onHome,
            modifier = Modifier.fillMaxWidth(),
            variant = SumiButtonVariant.Ghost,
            size = SumiButtonSize.Md,
        ) {
            Text(text = "Return Home", style = SumiTheme.typography.uiButton)
        }
    }
}

@Composable
private fun InkBleedAccent(color: Color, size: Dp, seed: Int, modifier: Modifier = Modifier) {
    val s = seed.toFloat()
    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val baseR = this.size.width * 0.30f
        drawCircle(
            color = color,
            radius = baseR * 1.5f,
            center = androidx.compose.ui.geometry.Offset(cx + s * 3, cy - s * 2),
            alpha = 0.08f,
        )
        drawCircle(color = color, radius = baseR, center = androidx.compose.ui.geometry.Offset(cx, cy), alpha = 0.12f)
        drawCircle(
            color = color,
            radius = baseR * 0.5f,
            center = androidx.compose.ui.geometry.Offset(cx - s * 2, cy + s),
            alpha = 0.18f,
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
}
