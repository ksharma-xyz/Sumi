package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun QuoteRule(
    modifier: Modifier = Modifier,
    ornament: String = "墨",
    color: Color = Color.Unspecified,
) {
    val resolvedColor = if (color == Color.Unspecified) SumiTheme.colors.paperEdge else color
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = resolvedColor, thickness = 1.dp)
        Spacer(Modifier.width(14.dp))
        Text(
            text = ornament,
            style = SumiTheme.typography.cjk.copy(
                fontSize = 16.sp,
            ),
            color = SumiTheme.colors.inkFaint,
            modifier = Modifier.padding(horizontal = Sumi.Space.s1),
        )
        Spacer(Modifier.width(14.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = resolvedColor, thickness = 1.dp)
    }
}
