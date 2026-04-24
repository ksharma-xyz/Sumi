package xyz.ksharma.sumi.navigation

import androidx.compose.runtime.Composable

/**
 * Intercepts the system back press when [enabled] is true.
 * Android: wraps [androidx.activity.compose.BackHandler].
 * iOS: no-op — iOS has no system back button; swipe-back is handled by NavDisplay.
 */
@Composable
expect fun TabBackHandler(enabled: Boolean, onBack: () -> Unit)
