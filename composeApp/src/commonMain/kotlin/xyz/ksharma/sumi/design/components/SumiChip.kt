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
import xyz.ksharma.sumi.theme.SumiTokens as Sumi
import xyz.ksharma.sumi.theme.SumiTheme

enum class SumiChipTone { Ink, Red, Teal, Gold, Muted }

@Composable
fun SumiChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: SumiChipTone = SumiChipTone.Ink,
) {
    val textColor = chipTextColor(tone)
    val borderColor = chipBorderColor(tone)
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

private fun chipTextColor(tone: SumiChipTone): Color = when (tone) {
    SumiChipTone.Ink   -> Sumi.Color.ink
    SumiChipTone.Red   -> Sumi.Color.red
    SumiChipTone.Teal  -> Sumi.Color.teal
    SumiChipTone.Gold  -> Sumi.Color.gold
    SumiChipTone.Muted -> Sumi.Color.inkFaint
}

private fun chipBorderColor(tone: SumiChipTone): Color = when (tone) {
    SumiChipTone.Ink   -> Sumi.Color.ink
    SumiChipTone.Red   -> Sumi.Color.red
    SumiChipTone.Teal  -> Sumi.Color.teal
    SumiChipTone.Gold  -> Sumi.Color.gold
    SumiChipTone.Muted -> Sumi.Color.inkGhost
}
