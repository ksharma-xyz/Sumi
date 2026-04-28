@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.core.graphics.PathParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

// A5 format (148×210mm): pocket-sized, prints 2-per-A4 sheet at home.
// Point dimensions: 1mm = 2.835pt → 148mm = 420pt, 210mm = 595pt.

class AndroidPuzzleBookExporter(private val context: Context) : PuzzleBookExporter {

    override suspend fun generate(spec: PuzzleBookSpec): Result<ByteArray> = runCatching {
        withContext(Dispatchers.Default) { renderPdf(spec) }
    }

    override suspend fun share(bytes: ByteArray, title: String): Result<Unit> = runCatching {
        val file = withContext(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "share").also { it.mkdirs() }
            val f = File(cacheDir, "sumi_puzzles_${System.currentTimeMillis()}.pdf")
            f.writeBytes(bytes)
            f
        }
        withContext(Dispatchers.Main) { sharePdfFile(file, title) }
    }

    private fun sharePdfFile(file: File, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.share.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, title).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
        )
    }

    private fun renderPdf(spec: PuzzleBookSpec): ByteArray {
        val document = PdfDocument()
        val colors = spec.theme.toPdfColors()
        val paperBitmap = spec.paperPng?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        val inkBleedBitmap = spec.inkBleedPng?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

        // Cover page
        val coverInfo = PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), 1).create()
        val coverPdfPage = document.startPage(coverInfo)
        drawCoverPage(coverPdfPage.canvas, spec, colors, paperBitmap, inkBleedBitmap)
        document.finishPage(coverPdfPage)

        // Puzzle pages (start at 2 after cover)
        spec.pages.forEachIndexed { index, page ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), index + 2).create()
            val pdfPage = document.startPage(pageInfo)
            drawPage(pdfPage.canvas, page, spec.appName, colors, paperBitmap, isAnswer = false)
            document.finishPage(pdfPage)
        }

        if (spec.includeAnswers) {
            spec.pages.forEachIndexed { index, page ->
                if (page.solution.isNotEmpty()) {
                    val num = spec.pages.size + index + 2
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), num).create()
                    val pdfPage = document.startPage(pageInfo)
                    drawPage(pdfPage.canvas, page, spec.appName, colors, paperBitmap, isAnswer = true)
                    document.finishPage(pdfPage)
                }
            }
        }

        paperBitmap?.recycle()
        inkBleedBitmap?.recycle()

        val stream = ByteArrayOutputStream()
        document.writeTo(stream)
        document.close()
        return stream.toByteArray()
    }

    private fun drawPaperBackground(canvas: Canvas, paper: Bitmap?, fallback: Int) {
        if (paper != null) {
            val src = Rect(0, 0, paper.width, paper.height)
            val dst = RectF(0f, 0f, PAGE_W, PAGE_H)
            canvas.drawBitmap(paper, src, dst, null)
        } else {
            canvas.drawRect(0f, 0f, PAGE_W, PAGE_H, fillPaint(fallback))
        }
    }

    private fun drawCoverPage(
        canvas: Canvas,
        spec: PuzzleBookSpec,
        c: PdfColors,
        paper: Bitmap?,
        inkBleed: Bitmap?,
    ) {
        drawPaperBackground(canvas, paper, c.background)
        // Soft ink-bleed in the bottom-right corner — gives the cover a hand-stamped feel.
        if (inkBleed != null) {
            val w = 180f
            val h = 180f
            val dst = RectF(PAGE_W - w + 30f, PAGE_H - h + 20f, PAGE_W + 30f, PAGE_H + 20f)
            val tinted = Paint().apply { alpha = 60 }
            canvas.drawBitmap(inkBleed, null, dst, tinted)
        }
        val cx = PAGE_W / 2f
        val cy = PAGE_H * 0.38f
        val logoSize = 110f
        drawSumiLogo(canvas, cx, cy, size = logoSize, color = c.ink)
        val logoBottom = cy + logoSize / 2f
        val titlePaint = textPaint(c.ink, 28f, italic = true, align = Paint.Align.CENTER)
        val titleFm = titlePaint.fontMetrics
        canvas.drawText("Sumi", cx, logoBottom + 24f - (titleFm.ascent + titleFm.descent) / 2f, titlePaint)
        canvas.drawText(
            "Puzzle Book",
            cx,
            logoBottom + 52f,
            textPaint(c.inkSoft, 12f, align = Paint.Align.CENTER, letterSpacing = 0.15f),
        )
        val ruleY = PAGE_H - MARGIN - 24f
        canvas.drawLine(MARGIN, ruleY, PAGE_W - MARGIN, ruleY, linePaint(c.thinLine, 0.5f))
        canvas.drawText(
            "${spec.pages.size} puzzles  /  A5  /  ${spec.theme.paper.name}  /  ${spec.theme.ink.name}",
            cx,
            ruleY + 14f,
            textPaint(c.inkSoft, 8f, align = Paint.Align.CENTER, letterSpacing = 0.1f),
        )
    }

    /**
     * Renders the actual Sumi enso brand mark on the PDF — the same path data that
     * lives in [logo_enso.xml] (used on the splash screen). The vector is parsed via
     * [PathParser] and tinted with the theme ink colour so it adapts to Light/Dark
     * paper.
     *
     * The XML viewport is 120×120; we scale to [size] and centre on (cx, cy).
     */
    private fun drawSumiLogo(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int) {
        val scale = size / LOGO_VIEWPORT
        canvas.save()
        canvas.translate(cx - size / 2f, cy - size / 2f)
        canvas.scale(scale, scale)
        val brush = PathParser.createPathFromPathData("M 95,30 A 42,42 0 1,0 82,92 Q 72,94 60,92")
        val brushPaint = Paint().apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = LOGO_STROKE_WIDTH
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        canvas.drawPath(brush, brushPaint)
        val dotBig = PathParser.createPathFromPathData("M 63,94 A 3,3 0 1,0 57,94 A 3,3 0 1,0 63,94 Z")
        val dotSmall = PathParser.createPathFromPathData("M 67.5,99 A 1.5,1.5 0 1,0 64.5,99 A 1.5,1.5 0 1,0 67.5,99 Z")
        val dotPaint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        dotPaint.alpha = 178 // 70% — matches fillAlpha="0.7" on the big dot
        canvas.drawPath(dotBig, dotPaint)
        dotPaint.alpha = 128 // 50% — matches fillAlpha="0.5" on the small dot
        canvas.drawPath(dotSmall, dotPaint)
        canvas.restore()
    }

    private fun drawWatermark(canvas: Canvas, c: PdfColors) {
        val paint = textPaint(c.ink, 80f, italic = true, align = Paint.Align.CENTER)
        paint.alpha = 10
        canvas.save()
        canvas.rotate(-35f, PAGE_W / 2f, PAGE_H / 2f)
        canvas.drawText("Sumi", PAGE_W / 2f, PAGE_H / 2f, paint)
        canvas.restore()
    }

    private fun drawPage(
        canvas: Canvas,
        page: PuzzleBookPage,
        appName: String,
        c: PdfColors,
        paper: Bitmap?,
        isAnswer: Boolean = false,
    ) {
        drawPaperBackground(canvas, paper, c.background)
        drawWatermark(canvas, c)
        drawHeader(canvas, page, appName, c)
        if (isAnswer && page.solution.isNotEmpty()) {
            drawAnswerGrid(canvas, page.clues, page.solution, c)
        } else {
            drawGrid(canvas, page.clues, c)
        }
        drawFooter(canvas, page, c, isAnswer)
    }

    private fun drawHeader(canvas: Canvas, page: PuzzleBookPage, appName: String, c: PdfColors) {
        // Real Sumi mark — same logo_enso.xml path data as the cover, just smaller.
        val markSize = 22f
        val markCx = MARGIN + markSize / 2f
        val markCy = MARGIN + markSize / 2f
        drawSumiLogo(canvas, markCx, markCy, size = markSize, color = c.ink)
        // Vertically centre the wordmark on the mark
        val wordmarkPaint = textPaint(c.ink, 11f, bold = false, letterSpacing = 0.18f)
        val wordmarkY = markCy - (wordmarkPaint.fontMetrics.ascent + wordmarkPaint.fontMetrics.descent) / 2f
        canvas.drawText(
            appName.uppercase(),
            markCx + markSize / 2f + 6f,
            wordmarkY,
            wordmarkPaint,
        )
        canvas.drawText(
            "#${page.pageNumber.toString().padStart(3, '0')}",
            PAGE_W - MARGIN,
            wordmarkY,
            textPaint(c.inkSoft, 9f, align = Paint.Align.RIGHT),
        )
        canvas.drawLine(MARGIN, HEADER_RULE_Y, PAGE_W - MARGIN, HEADER_RULE_Y, linePaint(c.thinLine, 0.5f))
    }

    private fun drawGrid(canvas: Canvas, clues: List<Int>, c: PdfColors) {
        val left = (PAGE_W - GRID_SIZE) / 2f
        val top = GRID_TOP

        for (i in 0..9) {
            val isBox = i % 3 == 0
            val paint = linePaint(if (isBox) c.boxLine else c.thinLine, if (isBox) 1.6f else 0.6f)
            canvas.drawLine(left + i * CELL_SIZE, top, left + i * CELL_SIZE, top + GRID_SIZE, paint)
            canvas.drawLine(left, top + i * CELL_SIZE, left + GRID_SIZE, top + i * CELL_SIZE, paint)
        }

        val numPaint = textPaint(c.ink, CELL_SIZE * 0.48f, bold = true, align = Paint.Align.CENTER)
        val fontMetrics = numPaint.fontMetrics
        clues.forEachIndexed { idx, value ->
            if (value != 0) {
                val row = idx / 9
                val col = idx % 9
                val cx = left + col * CELL_SIZE + CELL_SIZE / 2f
                val cy = top + row * CELL_SIZE + CELL_SIZE / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f
                canvas.drawText(value.toString(), cx, cy, numPaint)
            }
        }
    }

    private fun drawAnswerGrid(canvas: Canvas, clues: List<Int>, solution: List<Int>, c: PdfColors) {
        val left = (PAGE_W - GRID_SIZE) / 2f
        val top = GRID_TOP

        for (i in 0..9) {
            val isBox = i % 3 == 0
            val paint = linePaint(if (isBox) c.boxLine else c.thinLine, if (isBox) 1.6f else 0.6f)
            canvas.drawLine(left + i * CELL_SIZE, top, left + i * CELL_SIZE, top + GRID_SIZE, paint)
            canvas.drawLine(left, top + i * CELL_SIZE, left + GRID_SIZE, top + i * CELL_SIZE, paint)
        }

        val givenPaint = textPaint(c.ink, CELL_SIZE * 0.48f, bold = true, align = Paint.Align.CENTER)
        val answerPaint = textPaint(c.accent, CELL_SIZE * 0.44f, bold = false, align = Paint.Align.CENTER)
        val fm = givenPaint.fontMetrics
        solution.forEachIndexed { idx, value ->
            if (value != 0) {
                val row = idx / 9
                val col = idx % 9
                val cx = left + col * CELL_SIZE + CELL_SIZE / 2f
                val cy = top + row * CELL_SIZE + CELL_SIZE / 2f - (fm.ascent + fm.descent) / 2f
                val isGiven = clues.getOrElse(idx) { 0 } != 0
                canvas.drawText(value.toString(), cx, cy, if (isGiven) givenPaint else answerPaint)
            }
        }
    }

    private fun drawFooter(canvas: Canvas, page: PuzzleBookPage, c: PdfColors, isAnswer: Boolean = false) {
        val ruleY = GRID_TOP + GRID_SIZE + FOOTER_GAP
        canvas.drawLine(MARGIN, ruleY, PAGE_W - MARGIN, ruleY, linePaint(c.thinLine, 0.5f))

        val diffY = ruleY + 13f
        val diffLabel = if (isAnswer) "${page.difficulty.uppercase()} / ANSWERS" else page.difficulty.uppercase()
        canvas.drawText(
            diffLabel,
            PAGE_W / 2f,
            diffY,
            textPaint(c.accent, 7f, align = Paint.Align.CENTER, letterSpacing = 0.18f),
        )

        if (!isAnswer && page.quote.isNotEmpty()) {
            val maxChars = 60
            val displayQuote = if (page.quote.length > maxChars) page.quote.take(maxChars - 3) + "…" else page.quote
            val quotePaint = textPaint(c.inkSoft, 7f, italic = true, align = Paint.Align.CENTER)
            canvas.drawText("“$displayQuote”", PAGE_W / 2f, diffY + 13f, quotePaint)
            if (page.quoteAuthor.isNotEmpty()) {
                canvas.drawText("— ${page.quoteAuthor}", PAGE_W / 2f, diffY + 24f, quotePaint)
            }
        }
    }

    // ── Paint helpers ──────────────────────────────────────────────────────────

    private fun fillPaint(color: Int) = Paint().apply {
        this.color = color
        style = Paint.Style.FILL
    }

    private fun linePaint(color: Int, width: Float) = Paint().apply {
        this.color = color
        strokeWidth = width
        style = Paint.Style.STROKE
        isAntiAlias = false
    }

    @Suppress("LongParameterList")
    private fun textPaint(
        color: Int,
        size: Float,
        bold: Boolean = false,
        italic: Boolean = false,
        align: Paint.Align = Paint.Align.LEFT,
        letterSpacing: Float = 0f,
    ) = Paint().apply {
        this.color = color
        textSize = size
        typeface = when {
            bold && italic -> Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
            bold -> Typeface.create(Typeface.SERIF, Typeface.BOLD)
            italic -> Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            else -> Typeface.DEFAULT
        }
        textAlign = align
        this.letterSpacing = letterSpacing
        isAntiAlias = true
    }

    private companion object {
        const val PAGE_W = 420f         // A5 width  (148mm)
        const val PAGE_H = 595f         // A5 height (210mm)
        const val MARGIN = 28f
        const val HEADER_RULE_Y = 56f   // below enso (centred at y=42) + text — was 38f (overlapped logo)
        const val GRID_TOP = 70f        // was 50f
        const val CELL_SIZE = 40f
        const val GRID_SIZE = CELL_SIZE * 9  // 360pt
        const val FOOTER_GAP = 14f
        const val LOGO_VIEWPORT = 120f      // matches viewportWidth/Height in logo_enso.xml
        const val LOGO_STROKE_WIDTH = 11f   // matches android:strokeWidth in logo_enso.xml
    }
}

// ── Theme color mapping ────────────────────────────────────────────────────────

private data class PdfColors(
    val background: Int,
    val ink: Int,
    val inkSoft: Int,
    val accent: Int,
    val thinLine: Int,
    val boxLine: Int,
)

@Suppress("MagicNumber")
private fun BookTheme.toPdfColors(): PdfColors {
    val isDark = paper == BookPaper.Dark
    val background = if (isDark) 0xFF1A1210.toInt() else 0xFFFBF7F1.toInt()
    val inkColor = if (isDark) 0xFFE8E0D5.toInt() else 0xFF1A1210.toInt()
    val inkSoft = if (isDark) 0xFFB8A8A0.toInt() else 0xFF6B5E56.toInt()
    val thinLine = if (isDark) 0xFF3D2B1F.toInt() else 0xFFB8A8A0.toInt()
    val boxLine = if (isDark) 0xFF9A9087.toInt() else 0xFF3D2B1F.toInt()
    val accent = when (ink) {
        BookInk.Sumi -> if (isDark) 0xFFB8860B.toInt() else 0xFF8B0000.toInt()
        BookInk.Winter -> if (isDark) 0xFF7AAAD4.toInt() else 0xFF4A6E8A.toInt()
        BookInk.Autumn -> if (isDark) 0xFFE69960.toInt() else 0xFFC45810.toInt()
        BookInk.Gold -> if (isDark) 0xFFD4A017.toInt() else 0xFF9C7B0E.toInt()
    }
    return PdfColors(background, inkColor, inkSoft, accent, thinLine, boxLine)
}
