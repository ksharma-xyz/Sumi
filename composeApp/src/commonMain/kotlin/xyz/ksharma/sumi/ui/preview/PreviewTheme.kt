package xyz.ksharma.sumi.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.ui.theme.AppTheme

@Composable
fun AppPreviewTheme(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    AppTheme(dark = dark) {
        Box(
            modifier = modifier
                .background(SumiTheme.colors.paper)
                .padding(16.dp),
        ) {
            content()
        }
    }
}
