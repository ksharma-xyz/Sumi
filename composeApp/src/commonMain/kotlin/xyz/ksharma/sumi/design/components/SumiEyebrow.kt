package xyz.ksharma.sumi.design.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import xyz.ksharma.sumi.theme.SumiTheme

@Composable
fun SumiEyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Text(
        text = text.uppercase(),
        style = SumiTheme.typography.uiLabel,
        color = if (color == Color.Unspecified) SumiTheme.colors.red else color,
        modifier = modifier.semantics { heading() },
    )
}
