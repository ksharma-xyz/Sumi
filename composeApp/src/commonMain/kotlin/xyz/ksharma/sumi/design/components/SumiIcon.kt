package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.theme.SumiTheme

@Composable
fun SumiIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = if (tint == Color.Unspecified) SumiTheme.colors.ink else tint,
        modifier = modifier.size(size),
    )
}
