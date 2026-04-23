package xyz.ksharma.sumi.design.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun SumiEyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Sumi.Color.red,
) {
    Text(
        text = text.uppercase(),
        style = SumiTheme.typography.uiLabel,
        color = color,
        modifier = modifier,
    )
}
