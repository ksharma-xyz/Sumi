package xyz.ksharma.sumi.platform

import androidx.compose.runtime.Composable

/**
 * While this composable is in the composition, keeps the device screen awake
 * — no auto-dim, no auto-lock. Reverts to the system default automatically
 * when it leaves composition. Call once from the Game screen so a long solve
 * is never interrupted by the screen sleeping.
 */
@Composable
expect fun KeepScreenOn()
