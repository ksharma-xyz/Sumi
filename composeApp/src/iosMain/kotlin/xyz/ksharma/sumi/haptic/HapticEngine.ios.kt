package xyz.ksharma.sumi.haptic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle.UIImpactFeedbackStyleLight
import platform.UIKit.UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType.UINotificationFeedbackTypeError
import platform.UIKit.UINotificationFeedbackType.UINotificationFeedbackTypeSuccess

@Composable
actual fun rememberHapticEngine(): HapticEngine = remember { IosHapticEngine() }

private class IosHapticEngine : HapticEngine {

    override fun tick() {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyleLight).impactOccurred()
    }

    override fun confirm() {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyleMedium).impactOccurred()
    }

    override fun error() {
        UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackTypeError)
    }

    override fun win() {
        UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackTypeSuccess)
    }
}
