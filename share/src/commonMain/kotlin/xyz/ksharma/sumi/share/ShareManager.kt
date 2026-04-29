package xyz.ksharma.sumi.share

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Platform-specific manager that handles sharing an [ImageBitmap] to other apps via the OS share sheet.
 */
interface ShareManager {
    /**
     * Share [bitmap] as an image to other apps.
     *
     * Returns [Result.success] when the OS share sheet is shown.
     * Returns [Result.failure] for any encoding, file-system, or OS-level error.
     *
     * @param bitmap The image to share.
     * @param title  Chooser title (Android) or email/AirDrop subject (iOS).
     * @param text   Optional text caption accompanying the image (e.g. WhatsApp, Messages).
     */
    suspend fun shareImage(
        bitmap: ImageBitmap,
        title: String = "Sumi",
        text: String? = null,
    ): Result<Unit>

    /**
     * Share a plain text message via the OS share sheet — used for the Home
     * "invite a friend" affordance.
     *
     * @param text  Body of the message (can include URLs).
     * @param title Chooser title (Android) or subject (iOS mail / message).
     */
    suspend fun shareText(text: String, title: String = "Sumi"): Result<Unit>
}
