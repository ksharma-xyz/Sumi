package xyz.ksharma.sumi.share

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.setValue
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosShareManager : ShareManager {

    override suspend fun shareImage(bitmap: ImageBitmap, title: String, text: String?): Result<Unit> =
        runCatching {
            // Skia PNG encoding is CPU-heavy — run off Main.
            val byteArray = withContext(Dispatchers.Default) {
                val skiaImage = Image.makeFromBitmap(bitmap.asSkiaBitmap())
                checkNotNull(skiaImage.encodeToData(EncodedImageFormat.PNG, 100)) {
                    "Skia encodeToData returned null — bitmap may be empty or invalid"
                }.bytes
            }

            val nsData = byteArray.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = byteArray.size.toULong())
            }
            val uiImage = checkNotNull(UIImage(data = nsData)) {
                "UIImage(data:) returned null — NSData may be malformed"
            }

            withContext(Dispatchers.Main) {
                val items = buildList {
                    add(uiImage)
                    if (text != null) add(text)
                }
                val activityVC = UIActivityViewController(
                    activityItems = items,
                    applicationActivities = null,
                )
                activityVC.setValue(title, forKey = "subject")

                val topVC = checkNotNull(topmostViewController()) {
                    "No UIViewController to present the share sheet from"
                }
                // iPad requires a popover anchor to avoid a crash on presentation.
                activityVC.popoverPresentationController?.sourceView = topVC.view
                activityVC.popoverPresentationController?.sourceRect = topVC.view.bounds
                topVC.presentViewController(activityVC, animated = true, completion = null)
            }
        }

    override suspend fun shareText(text: String, title: String): Result<Unit> = runCatching {
        withContext(Dispatchers.Main) {
            val activityVC = UIActivityViewController(
                activityItems = listOf(text),
                applicationActivities = null,
            )
            activityVC.setValue(title, forKey = "subject")
            val topVC = checkNotNull(topmostViewController()) {
                "No UIViewController to present the share sheet from"
            }
            activityVC.popoverPresentationController?.sourceView = topVC.view
            activityVC.popoverPresentationController?.sourceRect = topVC.view.bounds
            topVC.presentViewController(activityVC, animated = true, completion = null)
        }
    }

    private fun topmostViewController(): UIViewController? {
        // keyWindow is deprecated in iOS 13+ — walk connectedScenes instead.
        val keyWindow: UIWindow? = UIApplication.sharedApplication
            .connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == platform.UIKit.UISceneActivationStateForegroundActive }
            ?.windows
            ?.filterIsInstance<UIWindow>()
            ?.firstOrNull { it.isKeyWindow() }
            ?: UIApplication.sharedApplication.connectedScenes
                .filterIsInstance<UIWindowScene>()
                .firstOrNull()
                ?.windows
                ?.filterIsInstance<UIWindow>()
                ?.firstOrNull()

        var topVC = keyWindow?.rootViewController
        while (topVC?.presentedViewController != null) {
            topVC = topVC.presentedViewController
        }
        return topVC
    }
}
