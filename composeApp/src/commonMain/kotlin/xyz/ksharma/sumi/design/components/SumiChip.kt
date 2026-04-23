package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun SumiChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: SumiChipTone = SumiChipTone.Ink,
) {
    val colors = SumiTheme.colors
    val textColor = chipTextColor(tone, colors)
    val borderColor = chipBorderColor(tone, colors)
    val shape = RoundedCornerShape(Sumi.Radius.xs)

    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            style = SumiTheme.typography.uiLabel,
            color = textColor,
        )
    }
}

private fun chipTextColor(tone: SumiChipTone, c: xyz.ksharma.sumi.theme.SumiColors): Color = when (tone) {
    SumiChipTone.Ink -> c.ink
    SumiChipTone.Red -> c.red
    SumiChipTone.Teal -> c.teal
    SumiChipTone.Gold -> c.gold
    SumiChipTone.Muted -> c.inkFaint
}

private fun chipBorderColor(tone: SumiChipTone, c: xyz.ksharma.sumi.theme.SumiColors): Color = when (tone) {
    SumiChipTone.Ink -> c.ink
    SumiChipTone.Red -> c.red
    SumiChipTone.Teal -> c.teal
    SumiChipTone.Gold -> c.gold
    SumiChipTone.Muted -> c.inkGhost
}
