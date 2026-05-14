package xyz.ksharma.sumi.platform

import platform.AppTrackingTransparency.ATTrackingManager
import platform.AppTrackingTransparency.ATTrackingManagerAuthorizationStatusNotDetermined

actual fun requestTrackingPermission() {
    if (ATTrackingManager.trackingAuthorizationStatus !=
        ATTrackingManagerAuthorizationStatusNotDetermined
    ) return
    ATTrackingManager.requestTrackingAuthorizationWithCompletionHandler { _ -> }
}
