package xyz.ksharma.sumi.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun TabBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS has no system back button — swipe-back gestures are handled by NavDisplay.
}
