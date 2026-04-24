@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onNewPuzzle: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // BG-5: paper-tinted frosted glass — 0.82 light / 0.88 dark (BACKGROUNDS.md §BG-5)
    val scrimColor = if (SumiTheme.isDark)
        SumiTheme.colors.paper.copy(alpha = 0.88f)
    else
        SumiTheme.colors.paper.copy(alpha = 0.82f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scrimColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .fillMaxWidth()
                .padding(horizontal = Sumi.Space.s6),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
        ) {
            Text(
                text = "休",
                style = SumiTheme.typography.cjk.copy(fontSize = 52.sp),
                color = SumiTheme.colors.red,
            )
            Text(
                text = "A moment of rest",
                style = SumiTheme.typography.h2.copy(
                    fontSize = 34.sp,
                    fontStyle = FontStyle.Italic,
                ),
                color = SumiTheme.colors.ink,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Your time is paused. Return when you are ready.",
                style = SumiTheme.typography.body.copy(lineHeight = (15 * 1.5f).sp),
                color = SumiTheme.colors.inkSoft,
                textAlign = TextAlign.Center,
            )
            PauseButton(
                label = "RESUME",
                onClick = onResume,
                bg = SumiTheme.colors.ink,
                fg = SumiTheme.colors.paper,
                modifier = Modifier.fillMaxWidth().padding(top = Sumi.Space.s4),
            )
            PauseButton(
                label = "NEW PUZZLE",
                onClick = onNewPuzzle,
                bg = Color.Transparent,
                fg = SumiTheme.colors.ink,
                borderColor = SumiTheme.colors.ink,
                modifier = Modifier.fillMaxWidth(),
            )
            PauseTextLink(label = "RETURN HOME", color = SumiTheme.colors.inkSoft, onClick = onHome)
        }
    }
}

@Composable
private fun PauseButton(
    label: String,
    onClick: () -> Unit,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
) {
    val src = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .background(bg)
            .then(if (borderColor != Color.Transparent) Modifier.border(1.dp, borderColor) else Modifier)
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(vertical = Sumi.Space.s4),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = SumiTheme.typography.uiButton.copy(letterSpacing = 3.sp),
            color = fg,
        )
    }
}

@Composable
private fun PauseTextLink(label: String, color: Color, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(vertical = Sumi.Space.s3),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = SumiTheme.typography.uiLabel.copy(letterSpacing = 2.5.sp),
            color = color,
        )
    }
}
