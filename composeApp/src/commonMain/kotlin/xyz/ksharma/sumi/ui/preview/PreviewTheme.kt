package xyz.ksharma.sumi.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.ui.theme.AppTheme
import xyz.ksharma.sumi.ui.theme.DarkBackground

@Composable
fun AppPreviewTheme(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AppTheme {
        Box(
            modifier = modifier
                .background(DarkBackground)
                .padding(16.dp),
        ) {
            content()
        }
    }
}
