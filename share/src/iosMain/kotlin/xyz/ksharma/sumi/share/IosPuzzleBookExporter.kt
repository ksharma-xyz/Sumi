@file:Suppress("MagicNumber", "TooManyFunctions", "LongMethod", "LongParameterList")

package xyz.ksharma.sumi.share

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGContextAddPath
import platform.CoreGraphics.CGContextRestoreGState
import platform.CoreGraphics.CGContextRotateCTM
import platform.CoreGraphics.CGContextSaveGState
import platform.CoreGraphics.CGContextScaleCTM
import platform.CoreGraphics.CGContextSetAlpha
import platform.CoreGraphics.CGContextSetLineCap
import platform.CoreGraphics.CGContextSetLineWidth
import platform.CoreGraphics.CGContextSetStrokeColorWithColor
import platform.CoreGraphics.CGContextStrokePath
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGLineCap
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.numberWithDouble
import platform.Foundation.setValue
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToURL
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSKernAttributeName
import platform.UIKit.NSMutableParagraphStyle
import platform.UIKit.NSParagraphStyleAttributeName
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.NSTextAlignmentLeft
import platform.UIKit.NSTextAlignmentRight
import platform.UIKit.UIActivityViewController
import platform.UIKit.drawAtPoint
import platform.UIKit.setAlignment
import platform.UIKit.sizeWithAttributes
import platform.UIKit.UIApplication
import platform.UIKit.UIBezierPath
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsPDFRenderer
import platform.UIKit.UIGraphicsPDFRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import platform.posix.memcpy
import kotlin.math.PI

// A5 format (148×210mm): pocket-sized, prints 2-per-A4 sheet at home.
// Point dimensions: 1mm = 2.835pt → 148mm = 420pt, 210mm = 595pt.
private const val PAGE_W = 420.0
private const val PAGE_H = 595.0
private const val MARGIN = 28.0
private const val HEADER_RULE_Y = 56.0
private const val GRID_TOP = 70.0
private const val CELL_SIZE = 40.0
private const val GRID_SIZE = CELL_SIZE * 9.0  // 360pt
private const val FOOTER_GAP = 14.0
private const val LOGO_VIEWPORT = 120.0
private const val LOGO_STROKE_WIDTH = 11.0

private enum class TextAlign { LEFT, CENTER, RIGHT }

private data class PdfColors(
    val background: UIColor,
    val ink: UIColor,
    val inkSoft: UIColor,
    val accent: UIColor,
    val thinLine: UIColor,
    val boxLine: UIColor,
)

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosPuzzleBookExporter : PuzzleBookExporter {

    override suspend fun generate(spec: PuzzleBookSpec): Result<ByteArray> = runCatching {
        // PDF rendering is CPU-heavy — keep off Main.
        withContext(Dispatchers.Default) { renderPdf(spec) }
    }

    override suspend fun share(bytes: ByteArray, title: String): Result<Unit> = runCatching {
        val tempUrl = withContext(Dispatchers.Default) { writeToTempFile(bytes) }
        withContext(Dispatchers.Main) { presentShareSheet(tempUrl, title) }
    }

    // ── PDF rendering ─────────────────────────────────────────────────────────

    private fun renderPdf(spec: PuzzleBookSpec): ByteArray {
        val colors = spec.theme.toPdfColors()
        val paperImage = spec.paperPng?.let { decodeImage(it) }
        val inkBleedImage = spec.inkBleedPng?.let { decodeImage(it) }

        val pageRect = CGRectMake(0.0, 0.0, PAGE_W, PAGE_H)
        val format = UIGraphicsPDFRendererFormat()
        val renderer = UIGraphicsPDFRenderer(bounds = pageRect, format = format)

        val pdfData = renderer.PDFDataWithActions { ctxOrNull ->
            val ctx = checkNotNull(ctxOrNull) { "UIGraphicsPDFRenderer gave null context" }

            // Cover page
            ctx.beginPage()
            drawCoverPage(spec, colors, paperImage, inkBleedImage)

            // Puzzle pages
            spec.pages.forEach { page ->
                ctx.beginPage()
                drawPage(page, spec.appName, colors, paperImage, isAnswer = false)
            }

            // Answer pages
            if (spec.includeAnswers) {
                spec.pages.forEach { page ->
                    if (page.solution.isNotEmpty()) {
                        ctx.beginPage()
                        drawPage(page, spec.appName, colors, paperImage, isAnswer = true)
                    }
                }
            }
        }

        return pdfData.toByteArray()
    }

    // ── Page composers ────────────────────────────────────────────────────────

    private fun drawCoverPage(
        spec: PuzzleBookSpec,
        c: PdfColors,
        paper: UIImage?,
        inkBleed: UIImage?,
    ) {
        drawPaperBackground(paper, c.background)

        // Soft ink-bleed in the bottom-right corner — gives the cover a hand-stamped feel.
        // Use context alpha rather than UIImage.draw(in:blendMode:alpha:) so we don't depend
        // on the CGBlendMode enum's K/N representation (Int vs ULong has churned across versions).
        if (inkBleed != null) {
            val ctx = UIGraphicsGetCurrentContext()
            if (ctx != null) {
                val w = 180.0
                val h = 180.0
                val rect = CGRectMake(PAGE_W - w + 30.0, PAGE_H - h + 20.0, w, h)
                CGContextSaveGState(ctx)
                CGContextSetAlpha(ctx, 60.0 / 255.0)
                inkBleed.drawInRect(rect)
                CGContextRestoreGState(ctx)
            }
        }

        val cx = PAGE_W / 2.0
        val cy = PAGE_H * 0.38
        val logoSize = 110.0
        drawSumiLogo(cx, cy, size = logoSize, color = c.ink)

        val logoBottom = cy + logoSize / 2.0

        // "Sumi" — italic serif, 28pt, centered. Match Android's vertical-center math:
        // baseline = (logoBottom + 24) + ascender/2 + descender/2 (where iOS ascender > 0, descender < 0)
        val titleFont = serifFont(28.0, italic = true)
        val titleBaselineY = logoBottom + 24.0 +
            (titleFont.ascender + titleFont.descender) / 2.0
        drawText("Sumi", cx, titleBaselineY, titleFont, c.ink, TextAlign.CENTER)

        // "Zen Sudoku" subtitle — 12pt, kerning 0.15em
        drawText(
            "Zen Sudoku",
            cx,
            logoBottom + 52.0,
            sansSerifFont(12.0),
            c.inkSoft,
            TextAlign.CENTER,
            letterSpacingEm = 0.15,
        )

        val ruleY = PAGE_H - MARGIN - 24.0
        drawLine(MARGIN, ruleY, PAGE_W - MARGIN, ruleY, c.thinLine, 0.5)

        drawText(
            "${spec.pages.size} puzzles  /  A5  /  ${spec.theme.paper.name}  /  ${spec.theme.ink.name}",
            cx,
            ruleY + 14.0,
            sansSerifFont(8.0),
            c.inkSoft,
            TextAlign.CENTER,
            letterSpacingEm = 0.10,
        )
    }

    private fun drawPage(
        page: PuzzleBookPage,
        appName: String,
        c: PdfColors,
        paper: UIImage?,
        isAnswer: Boolean,
    ) {
        drawPaperBackground(paper, c.background)
        drawWatermark(c)
        drawHeader(page, appName, c)
        if (isAnswer && page.solution.isNotEmpty()) {
            drawAnswerGrid(page.clues, page.solution, c)
        } else {
            drawGrid(page.clues, c)
        }
        drawFooter(page, c, isAnswer)
    }

    // ── Header / footer / grid ────────────────────────────────────────────────

    private fun drawHeader(page: PuzzleBookPage, appName: String, c: PdfColors) {
        val markSize = 22.0
        val markCx = MARGIN + markSize / 2.0
        val markCy = MARGIN + markSize / 2.0
        drawSumiLogo(markCx, markCy, size = markSize, color = c.ink)

        // Italic serif "Sumi" — same face as the cover wordmark, just smaller.
        // The previous all-caps sans variant looked generic on every page.
        val wordmarkFont = serifFont(14.0, italic = true)
        val wordmarkBaseY = markCy + (wordmarkFont.ascender + wordmarkFont.descender) / 2.0
        drawText(
            appName,
            markCx + markSize / 2.0 + 6.0,
            wordmarkBaseY,
            wordmarkFont,
            c.ink,
            TextAlign.LEFT,
        )

        drawText(
            "#${page.pageNumber.toString().padStart(3, '0')}",
            PAGE_W - MARGIN,
            wordmarkBaseY,
            sansSerifFont(9.0),
            c.inkSoft,
            TextAlign.RIGHT,
        )

        drawLine(MARGIN, HEADER_RULE_Y, PAGE_W - MARGIN, HEADER_RULE_Y, c.thinLine, 0.5)
    }

    private fun drawGrid(clues: List<Int>, c: PdfColors) {
        val left = (PAGE_W - GRID_SIZE) / 2.0
        val top = GRID_TOP

        for (i in 0..9) {
            val isBox = i % 3 == 0
            val color = if (isBox) c.boxLine else c.thinLine
            val width = if (isBox) 1.6 else 0.6
            drawLine(left + i * CELL_SIZE, top, left + i * CELL_SIZE, top + GRID_SIZE, color, width)
            drawLine(left, top + i * CELL_SIZE, left + GRID_SIZE, top + i * CELL_SIZE, color, width)
        }

        val numFont = serifFont(CELL_SIZE * 0.48, bold = true)
        clues.forEachIndexed { idx, value ->
            if (value != 0) {
                val row = idx / 9
                val col = idx % 9
                val cellCx = left + col * CELL_SIZE + CELL_SIZE / 2.0
                val cellCy = top + row * CELL_SIZE + CELL_SIZE / 2.0
                val baselineY = cellCy + (numFont.ascender + numFont.descender) / 2.0
                drawText(value.toString(), cellCx, baselineY, numFont, c.ink, TextAlign.CENTER)
            }
        }
    }

    private fun drawAnswerGrid(clues: List<Int>, solution: List<Int>, c: PdfColors) {
        val left = (PAGE_W - GRID_SIZE) / 2.0
        val top = GRID_TOP

        for (i in 0..9) {
            val isBox = i % 3 == 0
            val color = if (isBox) c.boxLine else c.thinLine
            val width = if (isBox) 1.6 else 0.6
            drawLine(left + i * CELL_SIZE, top, left + i * CELL_SIZE, top + GRID_SIZE, color, width)
            drawLine(left, top + i * CELL_SIZE, left + GRID_SIZE, top + i * CELL_SIZE, color, width)
        }

        val givenFont = serifFont(CELL_SIZE * 0.48, bold = true)
        val answerFont = serifFont(CELL_SIZE * 0.44, bold = false)
        solution.forEachIndexed { idx, value ->
            if (value != 0) {
                val row = idx / 9
                val col = idx % 9
                val cellCx = left + col * CELL_SIZE + CELL_SIZE / 2.0
                val cellCy = top + row * CELL_SIZE + CELL_SIZE / 2.0
                val isGiven = clues.getOrElse(idx) { 0 } != 0
                val font = if (isGiven) givenFont else answerFont
                val color = if (isGiven) c.ink else c.accent
                val baselineY = cellCy + (font.ascender + font.descender) / 2.0
                drawText(value.toString(), cellCx, baselineY, font, color, TextAlign.CENTER)
            }
        }
    }

    private fun drawFooter(page: PuzzleBookPage, c: PdfColors, isAnswer: Boolean) {
        val ruleY = GRID_TOP + GRID_SIZE + FOOTER_GAP
        drawLine(MARGIN, ruleY, PAGE_W - MARGIN, ruleY, c.thinLine, 0.5)

        val diffY = ruleY + 13.0
        val diffLabel = if (isAnswer) "${page.difficulty.uppercase()} / ANSWERS" else page.difficulty.uppercase()
        drawText(
            diffLabel,
            PAGE_W / 2.0,
            diffY,
            sansSerifFont(7.0),
            c.accent,
            TextAlign.CENTER,
            letterSpacingEm = 0.18,
        )

        if (!isAnswer && page.quote.isNotEmpty()) {
            val maxChars = 60
            val displayQuote = if (page.quote.length > maxChars) page.quote.take(maxChars - 3) + "…" else page.quote
            val quoteFont = serifFont(7.0, italic = true)
            drawText("“$displayQuote”", PAGE_W / 2.0, diffY + 13.0, quoteFont, c.inkSoft, TextAlign.CENTER)
            if (page.quoteAuthor.isNotEmpty()) {
                drawText("— ${page.quoteAuthor}", PAGE_W / 2.0, diffY + 24.0, quoteFont, c.inkSoft, TextAlign.CENTER)
            }
        }
    }

    // ── Logo & background ─────────────────────────────────────────────────────

    private fun drawPaperBackground(paper: UIImage?, fallback: UIColor) {
        val rect = CGRectMake(0.0, 0.0, PAGE_W, PAGE_H)
        if (paper != null) {
            paper.drawInRect(rect)
        } else {
            fallback.setFill()
            UIBezierPath.bezierPathWithRect(rect).fill()
        }
    }

    /**
     * Renders the Sumi enso brand mark — same path data as logo_enso.xml on Android.
     *
     * Original SVG path: `M 95,30 A 42,42 0 1,0 82,92 Q 72,94 60,92`
     *
     * The SVG arc parameters were converted to UIBezierPath's center-based form using the
     * W3C arc endpoint-to-center conversion. For arc with rx=ry=42 from (95,30) to (82,92),
     * large-arc=1, sweep=0:
     *   center ≈ (61.5, 55.34), startAngle ≈ -0.6477, endAngle ≈ 1.0608, counterclockwise.
     *
     * The two dots are simple circles (full SVG arcs that go all the way around).
     */
    private fun drawSumiLogo(cx: Double, cy: Double, size: Double, color: UIColor) {
        val ctx = UIGraphicsGetCurrentContext() ?: return
        val scale = size / LOGO_VIEWPORT

        CGContextSaveGState(ctx)
        CGContextTranslateCTM(ctx, cx - size / 2.0, cy - size / 2.0)
        CGContextScaleCTM(ctx, scale, scale)

        // Brush stroke — driven through CGContext so the line-cap enum type doesn't matter.
        val brush = UIBezierPath()
        brush.moveToPoint(CGPointMake(95.0, 30.0))
        brush.addArcWithCenter(
            center = CGPointMake(61.5, 55.34),
            radius = 42.0,
            startAngle = -0.6477,
            endAngle = 1.0608,
            clockwise = false,
        )
        brush.addQuadCurveToPoint(
            endPoint = CGPointMake(60.0, 92.0),
            controlPoint = CGPointMake(72.0, 94.0),
        )
        CGContextAddPath(ctx, brush.CGPath)
        CGContextSetStrokeColorWithColor(ctx, color.CGColor)
        CGContextSetLineWidth(ctx, LOGO_STROKE_WIDTH)
        CGContextSetLineCap(ctx, CGLineCap.kCGLineCapRound)
        CGContextStrokePath(ctx)

        // Big dot at (60, 94), radius 3, alpha 0.7
        color.colorWithAlphaComponent(0.7).setFill()
        UIBezierPath.bezierPathWithOvalInRect(CGRectMake(57.0, 91.0, 6.0, 6.0)).fill()

        // Small dot at (66, 99), radius 1.5, alpha 0.5
        color.colorWithAlphaComponent(0.5).setFill()
        UIBezierPath.bezierPathWithOvalInRect(CGRectMake(64.5, 97.5, 3.0, 3.0)).fill()

        CGContextRestoreGState(ctx)
    }

    private fun drawWatermark(c: PdfColors) {
        val ctx = UIGraphicsGetCurrentContext() ?: return
        val font = serifFont(80.0, italic = true)
        val pageCx = PAGE_W / 2.0
        val pageCy = PAGE_H / 2.0
        val rotation = -35.0 * PI / 180.0

        CGContextSaveGState(ctx)
        // alpha 10/255 ≈ 0.039 — subtle paper watermark
        CGContextSetAlpha(ctx, 10.0 / 255.0)
        CGContextTranslateCTM(ctx, pageCx, pageCy)
        CGContextRotateCTM(ctx, rotation)
        CGContextTranslateCTM(ctx, -pageCx, -pageCy)

        val baselineY = pageCy + (font.ascender + font.descender) / 2.0
        drawText("Sumi", pageCx, baselineY, font, c.ink, TextAlign.CENTER)

        CGContextRestoreGState(ctx)
    }

    // ── Drawing primitives ────────────────────────────────────────────────────

    private fun drawLine(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        color: UIColor,
        width: Double,
    ) {
        val path = UIBezierPath()
        path.moveToPoint(CGPointMake(x1, y1))
        path.addLineToPoint(CGPointMake(x2, y2))
        path.lineWidth = width
        color.setStroke()
        path.stroke()
    }

    /**
     * Draws [text] with its baseline at Y = [baselineY], horizontally aligned around [anchorX].
     * - [TextAlign.LEFT]   — anchorX is the left edge
     * - [TextAlign.CENTER] — anchorX is the centre
     * - [TextAlign.RIGHT]  — anchorX is the right edge
     *
     * iOS draws NSAttributedString from the top-left of the glyph bounding box, so we
     * convert baseline → top by subtracting the font's ascender.
     */
    private fun drawText(
        text: String,
        anchorX: Double,
        baselineY: Double,
        font: UIFont,
        color: UIColor,
        align: TextAlign,
        letterSpacingEm: Double = 0.0,
    ) {
        val attrs = mutableMapOf<Any?, Any>(
            NSFontAttributeName to font,
            NSForegroundColorAttributeName to color,
        )
        if (letterSpacingEm != 0.0) {
            // NSKern is in points; Android letterSpacing is in em (multiples of font size).
            attrs[NSKernAttributeName] = NSNumber.numberWithDouble(letterSpacingEm * font.pointSize)
        }
        // Always attach a paragraph style so iOS doesn't apply default justification quirks.
        val paragraph = NSMutableParagraphStyle()
        paragraph.setAlignment(
            when (align) {
                TextAlign.LEFT -> NSTextAlignmentLeft
                TextAlign.CENTER -> NSTextAlignmentCenter
                TextAlign.RIGHT -> NSTextAlignmentRight
            },
        )
        attrs[NSParagraphStyleAttributeName] = paragraph

        // Use NSString's UIKit drawing category — more reliably exposed in K/N than
        // NSAttributedString's drawing methods, and identical visual output.
        // String→NSString bridges at runtime; the static-cast warning is a known K/N pessimism.
        @Suppress("CAST_NEVER_SUCCEEDS")
        val nsText = text as NSString
        val textWidth = nsText.sizeWithAttributes(attrs).useContents { width }

        val leftX = when (align) {
            TextAlign.LEFT -> anchorX
            TextAlign.CENTER -> anchorX - textWidth / 2.0
            TextAlign.RIGHT -> anchorX - textWidth
        }
        val topY = baselineY - font.ascender

        nsText.drawAtPoint(CGPointMake(leftX, topY), withAttributes = attrs)
    }

    // ── Fonts ─────────────────────────────────────────────────────────────────
    //
    // Android uses Typeface.SERIF (Times-equivalent) for body/cover text and Typeface.DEFAULT
    // (sans-serif) for the wordmark / page numbers. Match by mapping:
    //   Android SERIF  → Times-Roman / Times-Bold / Times-Italic / Times-BoldItalic
    //   Android DEFAULT → UIFont.systemFontOfSize (San Francisco)

    private fun serifFont(size: Double, bold: Boolean = false, italic: Boolean = false): UIFont {
        val name = when {
            bold && italic -> "Times-BoldItalic"
            bold -> "Times-Bold"
            italic -> "Times-Italic"
            else -> "Times-Roman"
        }
        // UIFont(name:size:) returns nil if the name is missing — fall back to system serif.
        return UIFont.fontWithName(fontName = name, size = size)
            ?: UIFont.systemFontOfSize(size)
    }

    private fun sansSerifFont(size: Double): UIFont = UIFont.systemFontOfSize(size)

    // ── Theme color mapping ───────────────────────────────────────────────────

    @Suppress("MagicNumber")
    private fun BookTheme.toPdfColors(): PdfColors {
        val isDark = paper == BookPaper.Dark
        val backgroundC = if (isDark) rgb(0x1A, 0x12, 0x10) else rgb(0xFB, 0xF7, 0xF1)
        val inkC = if (isDark) rgb(0xE8, 0xE0, 0xD5) else rgb(0x1A, 0x12, 0x10)
        val inkSoftC = if (isDark) rgb(0xB8, 0xA8, 0xA0) else rgb(0x6B, 0x5E, 0x56)
        val thinLineC = if (isDark) rgb(0x3D, 0x2B, 0x1F) else rgb(0xB8, 0xA8, 0xA0)
        val boxLineC = if (isDark) rgb(0x9A, 0x90, 0x87) else rgb(0x3D, 0x2B, 0x1F)
        val accentC = when (this.ink) {
            BookInk.Sumi -> if (isDark) rgb(0xB8, 0x86, 0x0B) else rgb(0x8B, 0x00, 0x00)
            BookInk.Winter -> if (isDark) rgb(0x7A, 0xAA, 0xD4) else rgb(0x4A, 0x6E, 0x8A)
            BookInk.Autumn -> if (isDark) rgb(0xE6, 0x99, 0x60) else rgb(0xC4, 0x58, 0x10)
            BookInk.Gold -> if (isDark) rgb(0xD4, 0xA0, 0x17) else rgb(0x9C, 0x7B, 0x0E)
        }
        return PdfColors(backgroundC, inkC, inkSoftC, accentC, thinLineC, boxLineC)
    }

    private fun rgb(r: Int, g: Int, b: Int): UIColor = UIColor(
        red = r / 255.0,
        green = g / 255.0,
        blue = b / 255.0,
        alpha = 1.0,
    )

    // ── Sharing ───────────────────────────────────────────────────────────────

    private fun writeToTempFile(bytes: ByteArray): NSURL {
        val ts = NSDate().timeIntervalSince1970.toLong()
        val path = NSTemporaryDirectory() + "sumi_puzzles_$ts.pdf"
        val url = NSURL.fileURLWithPath(path)
        val nsData = bytes.toNSData()
        val ok = nsData.writeToURL(url, atomically = true)
        check(ok) { "Failed to write PDF to $path" }
        return url
    }

    private fun presentShareSheet(url: NSURL, title: String) {
        val items = listOf(url)
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

    private fun topmostViewController(): UIViewController? {
        val keyWindow: UIWindow? = UIApplication.sharedApplication
            .connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
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

    // ── ByteArray ↔ NSData ↔ UIImage ──────────────────────────────────────────

    private fun decodeImage(bytes: ByteArray): UIImage? {
        if (bytes.isEmpty()) return null
        return UIImage(data = bytes.toNSData())
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = if (isEmpty()) {
    NSData()
} else {
    usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val len = this.length.toInt()
    if (len == 0) return ByteArray(0)
    val bytes = ByteArray(len)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
    }
    return bytes
}
