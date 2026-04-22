package xyz.ksharma.sumi

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.ksharma.sumi.screens.DemoScreen
import xyz.ksharma.sumi.ui.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        DemoScreen(modifier = Modifier.fillMaxSize())
    }
}
