package xyz.ksharma.sumi.share

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface

private const val ARGB_MAX_CHANNEL = 255
private const val ARGB_ALPHA_SHIFT = 24
private const val ARGB_RED_SHIFT = 16
private const val ARGB_GREEN_SHIFT = 8
private const val ARGB_BLUE_SHIFT = 0

actual fun ImageBitmap.withBrandingHeader(
    titleText: String,
    subtitleText: String,
    backgroundColor: Color,
    textColor: Color,
    density: Float,
): ImageBitmap {
    val originalBitmap = asSkiaBitmap()
    val width = originalBitmap.width
    val originalHeight = originalBitmap.height

    val titleSizePx = BRANDING_TITLE_SIZE_SP * density
    val subtitleSizePx = BRANDING_SUBTITLE_SIZE_SP * density
    val paddingPx = BRANDING_HEADER_PADDING_DP * density
    val gapPx = BRANDING_TITLE_SUBTITLE_GAP_DP * density

    val titleBaseline = paddingPx + titleSizePx
    val subtitleBaseline = titleBaseline + gapPx + subtitleSizePx
    val headerHeight = (subtitleBaseline + paddingPx).toInt()

    fun Color.toSkiaArgb(): Int =
        ((alpha * ARGB_MAX_CHANNEL).toInt() shl ARGB_ALPHA_SHIFT) or
            ((red * ARGB_MAX_CHANNEL).toInt() shl ARGB_RED_SHIFT) or
            ((green * ARGB_MAX_CHANNEL).toInt() shl ARGB_GREEN_SHIFT) or
            ((blue * ARGB_MAX_CHANNEL).toInt() shl ARGB_BLUE_SHIFT)

    val surface = Surface.makeRasterN32Premul(width, originalHeight + headerHeight)
    val canvas: Canvas = surface.canvas

    canvas.drawRect(
        Rect.makeXYWH(0f, 0f, width.toFloat(), headerHeight.toFloat()),
        Paint().apply { color = backgroundColor.toSkiaArgb() },
    )

    val textPaint = Paint().apply { color = textColor.toSkiaArgb() }

    // "Helvetica Neue" is guaranteed present on iOS and resolves correctly with CoreText.
    val boldTypeface: Typeface =
        FontMgr.default.matchFamilyStyle("Helvetica Neue", FontStyle.BOLD) ?: Typeface.makeEmpty()

    val titleLine = TextLine.make(titleText, Font(boldTypeface, titleSizePx))
    canvas.drawTextLine(titleLine, (width - titleLine.width) / 2f, titleBaseline, textPaint)

    val subtitleLine = TextLine.make(subtitleText, Font(boldTypeface, subtitleSizePx))
    canvas.drawTextLine(subtitleLine, (width - subtitleLine.width) / 2f, subtitleBaseline, textPaint)

    canvas.drawImage(Image.makeFromBitmap(originalBitmap), 0f, headerHeight.toFloat())

    return surface.makeImageSnapshot().toComposeImageBitmap()
}
