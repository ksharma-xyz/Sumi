@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.win

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.lexilabs.basic.ads.AdState
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.InterstitialAd
import app.lexilabs.basic.ads.composable.rememberInterstitialAd
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import xyz.ksharma.sumi.Quote
import xyz.ksharma.sumi.ads.AdUnits
import xyz.ksharma.sumi.design.components.SudokuThumbnail
import xyz.ksharma.sumi.design.components.SumiButton
import xyz.ksharma.sumi.design.components.SumiButtonSize
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.resources.ink_bleed_02
import xyz.ksharma.sumi.resources.ink_bleed_03
import xyz.ksharma.sumi.resources.logo_chop
import xyz.ksharma.sumi.resources.logo_enso
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

// Long because the screen orchestrates a share-layer card + interstitial-ad lifecycle +
// onShare callback wiring. Helpers below split the visual sections — further extraction
// would split the surface state and hurt traceability.
@Suppress("LongMethod", "LambdaParameterInRestartableEffect")
@OptIn(DependsOnGoogleMobileAds::class)
@Composable
fun WinScreen(
    elapsedMs: Long,
    mistakeCount: Int,
    moveCount: Int,
    difficulty: String,
    quote: Quote,
    modifier: Modifier = Modifier,
    onNextPuzzle: (() -> Unit)? = null,
    onShare: ((ImageBitmap) -> Unit)? = null,
    /** 81-char solution string from WinRoute; rendered inside the share card. */
    solution: String = "",
    showInterstitialAd: Boolean = false,
    onInterstitialDismiss: () -> Unit = {},
) {
    val shareLayer = rememberGraphicsLayer()
    val cardBackground = SumiTheme.colors.paper
    val coroutineScope = rememberCoroutineScope()

    // Pre-load the post-completion interstitial as soon as the screen enters composition.
    // basic-ads' InterstitialAd composable crashes if shown before its async load completes
    // ("InterstitialAd not loaded yet. InterstitialAd.load() must be called first") — so we
    // pre-load via rememberInterstitialAd and only render InterstitialAd when state == READY.
    // Must be called unconditionally per Compose composable rules.
    val interstitialAdState = rememberInterstitialAd(
        adUnitId = AdUnits.Interstitial,
        onLoad = {},
        onFailure = { /* fall-through; the conditional render below skips when state != READY */ },
    )

    Box(modifier = modifier.fillMaxSize()) {
        WashiBG(modifier = Modifier.fillMaxSize())
        // Heavier ink-bleed wash than before (0.08 → 0.18) so the result reads
        // more like a sumi-e print and less like a flat status sheet.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            Image(
                painter = painterResource(Res.drawable.ink_bleed_02),
                contentDescription = null,
                modifier = Modifier.size(360.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.18f,
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = painterResource(Res.drawable.ink_bleed_03),
                contentDescription = null,
                modifier = Modifier.size(300.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.20f,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Sumi.Space.s6)
                .padding(top = Sumi.Space.s9, bottom = Sumi.Space.s7),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WinShareCard(
                elapsedMs = elapsedMs,
                mistakeCount = mistakeCount,
                moveCount = moveCount,
                difficulty = difficulty,
                quote = quote,
                solution = solution,
                surface = ShareCardSurface(layer = shareLayer, background = cardBackground),
            )
            Spacer(Modifier.height(Sumi.Space.s7))
            WinActions(
                onNextPuzzle = onNextPuzzle,
                onShare = if (onShare != null) {
                    { coroutineScope.launch { onShare(shareLayer.toImageBitmap()) } }
                } else null,
            )
        }
        // Only render InterstitialAd once the pre-loaded ad reports READY.
        // Other states (LOADING, FAILED, SHOWING, SHOWN) either skip or are
        // self-managed by the library — see comment on rememberInterstitialAd above.
        if (showInterstitialAd) {
            when (interstitialAdState.value.state) {
                AdState.READY -> InterstitialAd(
                    loadedAd = interstitialAdState.value,
                    onDismissed = onInterstitialDismiss,
                    onFailure = { _ -> onInterstitialDismiss() },
                )
                AdState.SHOWING, AdState.SHOWN -> Unit // ad on screen; onDismissed will dismiss the flag
                else -> {
                    // Not ready (still loading / failed / no fill) — release the flag
                    // so the user isn't blocked waiting on an ad that never appears.
                    LaunchedEffect(showInterstitialAd) { onInterstitialDismiss() }
                }
            }
        }
    }
}

/** Bundles share-layer plumbing so [WinShareCard]'s param list stays under detekt's threshold. */
private data class ShareCardSurface(val layer: GraphicsLayer, val background: Color)

// Hardcoded share-card palette so the captured image looks identical regardless
// of whether the user has light or dark theme active in-app. The shared PNG is
// always cream paper / dark ink — a portable artifact that doesn't surprise
// the recipient with the sender's theme choice.
private val ShareInk = Color(0xFF2A2218)
private val ShareInkSoft = Color(0xFF5A4838)
private val ShareInkFaint = Color(0xFF8A7560)
private val SharePaper = Color(0xFFFBF7EC)

@Composable
private fun WinShareCard(
    elapsedMs: Long,
    mistakeCount: Int,
    moveCount: Int,
    difficulty: String,
    quote: Quote,
    solution: String,
    surface: ShareCardSurface,
) {
    val shareLayer = surface.layer
    // Outer hairline frame using the THEME ink color — visible only on screen
    // (NOT inside the captured layer). In dark mode the cream-on-dark contrast
    // of the card was washing out at the edges; this hairline gives it a clear
    // boundary regardless of theme. The captured PNG is unaffected because the
    // border is drawn outside the layer.record block.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SumiTheme.colors.ink.copy(alpha = 0.18f))
            .drawWithContent {
                shareLayer.record {
                    drawRect(color = SharePaper)
                    this@drawWithContent.drawContent()
                }
                drawLayer(shareLayer)
            }
            .padding(top = Sumi.Space.s5, bottom = Sumi.Space.s5),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Brand header — Sumi mark + wordmark on the same paper bg as the
        // rest of the card so the header is visually seamless. Compose
        // primitives only (no painterResource for the mark, since we need
        // bitmap-capture to render reliably across all platforms).
        ShareHeader()
        Spacer(Modifier.height(Sumi.Space.s5))
        // 2. Smaller chop seal — was 88dp, now 56dp so it doesn't dominate
        // the card alongside the new header.
        ChopSeal(size = 56.dp)
        Spacer(Modifier.height(Sumi.Space.s5))
        // The actual completed grid — proof of the solve. Renders inside the share layer
        // so the exported PNG includes the puzzle, not just the stats.
        // 2. Filled puzzle — proof of the solve.
        if (solution.length == 81) {
            SudokuThumbnail(cells = solution, sizeDp = 220.dp)
            Spacer(Modifier.height(Sumi.Space.s6))
        }

        // 3. Time as the headline number.
        // All text inside the share card uses the hardcoded ShareInk* palette
        // so the captured PNG looks identical regardless of the user's app
        // theme (light vs dark) — the image is a portable artifact.
        Text(
            text = "TIME",
            style = SumiTheme.typography.uiLabel.copy(letterSpacing = 2.sp),
            color = ShareInkFaint,
        )
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = formatTime(elapsedMs),
            style = SumiTheme.typography.numeral.copy(
                fontSize = 56.sp,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.02f).em,
            ),
            color = ShareInk,
        )
        Spacer(Modifier.height(Sumi.Space.s5))

        // 4. Secondary stats inline — single row, separators between, no wrap risk.
        InlineSecondaryStats(
            mistakeCount = mistakeCount,
            moveCount = moveCount,
            difficulty = difficulty,
        )
        if (quote.text.isNotBlank()) {
            // Generous gap between the stats line and the closing quote — they're
            // semantically distinct sections (your run vs an offering for reflection)
            // and the previous tight spacing made them read as one block.
            Spacer(Modifier.height(Sumi.Space.s7))
            QuoteFooter(text = quote.text, attribution = quote.attribution)
        }
    }
}

/**
 * The real Sumi chop-seal vector, tilted -12\u00B0 so it reads as a stamp pressed
 * onto the paper. Using the resource (instead of a Compose-primitive box) means
 * the design system stays the source of truth \u2014 and the share-card capture
 * still works because painterResource of an XML vector renders fine inside
 * graphicsLayer (verified earlier when capturing this card to a bitmap).
 */
@Composable
private fun ChopSeal(size: Dp = 88.dp) {
    Image(
        painter = painterResource(Res.drawable.logo_chop),
        contentDescription = "Sumi",
        modifier = Modifier
            .size(size)
            .graphicsLayer { rotationZ = -12f }
            .semantics { contentDescription = "Sumi" },
    )
}

/**
 * FlowRow so chips wrap onto the next line when they don't fit horizontally —
 * "Easy" was getting squished out of the row on narrower screens.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InlineSecondaryStats(mistakeCount: Int, moveCount: Int, difficulty: String) {
    val levelKanji = difficultyKanji(difficulty)
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "$mistakeCount mistakes, $moveCount moves, level $difficulty"
            },
    ) {
        StatChip(value = mistakeCount.toString(), label = "mistakes")
        StatSeparator()
        StatChip(value = moveCount.toString(), label = "moves")
        StatSeparator()
        StatChip(value = levelKanji, label = difficulty.lowercase(), isKanji = true)
    }
}

@Composable
private fun StatChip(value: String, label: String, isKanji: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = value,
            style = if (isKanji) SumiTheme.typography.cjk.copy(fontSize = 22.sp)
            else SumiTheme.typography.numeral.copy(fontSize = 22.sp, fontStyle = FontStyle.Italic),
            color = ShareInk,
        )
        Spacer(Modifier.size(Sumi.Space.s1))
        Text(
            text = label,
            style = SumiTheme.typography.uiMeta.copy(fontSize = 12.sp),
            color = ShareInkSoft,
        )
    }
}

@Composable
private fun StatSeparator() {
    Text(
        text = "/",
        style = SumiTheme.typography.uiMeta.copy(fontSize = 14.sp),
        color = ShareInkFaint,
        modifier = Modifier.padding(horizontal = Sumi.Space.s2),
    )
}

@Composable
private fun QuoteFooter(text: String, attribution: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = Sumi.Space.s4),
    ) {
        Text(
            text = "\u201C$text\u201D",
            style = SumiTheme.typography.quote.copy(fontSize = 14.sp, fontStyle = FontStyle.Italic),
            color = ShareInkSoft,
            textAlign = TextAlign.Center,
        )
        if (attribution.isNotBlank()) {
            Spacer(Modifier.height(Sumi.Space.s1))
            Text(
                text = "\u2014 $attribution",
                style = SumiTheme.typography.uiMeta.copy(fontSize = 11.sp),
                color = ShareInkFaint,
            )
        }
    }
}

/**
 * Sumi mark + wordmark anchored at the top of the shared card. Sized small
 * so the headline stays the time, not the brand. Both elements use the
 * hardcoded share palette so the header is seamless on the cream paper bg.
 */
@Composable
private fun ShareHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.logo_enso),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(ShareInk),
        )
        Spacer(Modifier.size(Sumi.Space.s2))
        Text(
            text = "Sumi",
            style = SumiTheme.typography.h3.copy(fontSize = 22.sp, fontStyle = FontStyle.Italic),
            color = ShareInk,
        )
    }
}

private fun difficultyKanji(difficulty: String): String = when (difficulty.lowercase()) {
    "easy" -> "一"
    "medium" -> "二"
    "hard" -> "三"
    "master" -> "四"
    "edo" -> "五"
    else -> difficulty
}

@Composable
private fun WinActions(
    onNextPuzzle: (() -> Unit)?,
    onShare: (() -> Unit)?,
) {
    // Two-button hierarchy: Share is the primary (high-contrast) action — most
    // users want to celebrate the run; Next Practice is the secondary ghost.
    // System back from Win returns Home, so a dedicated "Return Home" button
    // is redundant and was removed.
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Sumi.Space.s2)) {
        if (onShare != null) {
            SumiButton(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
                size = SumiButtonSize.Lg,
            ) {
                Text(text = "Share Result", style = SumiTheme.typography.uiButton)
            }
        }
        if (onNextPuzzle != null) {
            SumiButton(
                onClick = onNextPuzzle,
                modifier = Modifier.fillMaxWidth(),
                variant = SumiButtonVariant.Ghost,
                size = SumiButtonSize.Md,
            ) {
                Text(text = "Next Practice →", style = SumiTheme.typography.uiButton)
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
}
