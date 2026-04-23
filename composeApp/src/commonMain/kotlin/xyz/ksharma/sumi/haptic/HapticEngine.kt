package xyz.ksharma.sumi.haptic

import androidx.compose.runtime.Composable

interface HapticEngine {
    fun tick() // light: cell tap, tool press, note toggle
    fun confirm() // medium: correct digit placed
    fun error() // heavy: wrong digit / mistake
    fun win() // celebratory: puzzle complete
}

@Composable
expect fun rememberHapticEngine(): HapticEngine
