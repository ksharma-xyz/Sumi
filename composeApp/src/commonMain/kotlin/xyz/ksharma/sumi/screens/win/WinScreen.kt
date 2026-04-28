@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.win

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
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
    onHome: () -> Unit,
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            Image(
                painter = painterResource(Res.drawable.ink_bleed_02),
                contentDescription = null,
                modifier = Modifier.size(280.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.08f,
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = painterResource(Res.drawable.ink_bleed_03),
                contentDescription = null,
                modifier = Modifier.size(220.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.10f,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            Spacer(Modifier.weight(1f))
            WinActions(
                onNextPuzzle = onNextPuzzle,
                onHome = onHome,
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
    val cardBackground = surface.background
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                shareLayer.record {
                    drawRect(color = cardBackground)
                    this@drawWithContent.drawContent()
                }
                drawLayer(shareLayer)
            }
            .padding(vertical = Sumi.Space.s4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Brand mark — pure Compose primitives so the share PNG always renders it.
        ChopSeal(size = 88.dp)
        Spacer(Modifier.height(Sumi.Space.s5))
        // The actual completed grid — proof of the solve. Renders inside the share layer
        // so the exported PNG includes the puzzle, not just the stats.
        // 2. Filled puzzle — proof of the solve.
        if (solution.length == 81) {
            SudokuThumbnail(cells = solution, sizeDp = 220.dp)
            Spacer(Modifier.height(Sumi.Space.s6))
        }

        // 3. Time as the headline number.
        Text(
            text = "TIME",
            style = SumiTheme.typography.uiLabel.copy(letterSpacing = 2.sp),
            color = SumiTheme.colors.inkFaint,
        )
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = formatTime(elapsedMs),
            style = SumiTheme.typography.numeral.copy(
                fontSize = 56.sp,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.02f).em,
            ),
            color = SumiTheme.colors.ink,
        )
        Spacer(Modifier.height(Sumi.Space.s5))

        // 4. Secondary stats inline — single row, separators between, no wrap risk.
        InlineSecondaryStats(
            mistakeCount = mistakeCount,
            moveCount = moveCount,
            difficulty = difficulty,
        )
        if (quote.text.isNotBlank()) {
            QuoteFooter(text = quote.text, attribution = quote.attribution)
        }
    }
}

@Composable
private fun ChopSeal(size: Dp = 88.dp) {
    val cream = SumiTheme.colors.paper
    val red = SumiTheme.colors.red
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(size * 0.07f)
    Box(
        modifier = Modifier
            .size(size)
            .background(color = red, shape = shape)
            .border(width = 2.dp, color = cream.copy(alpha = 0.9f), shape = shape)
            .semantics { contentDescription = "Sumi" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "\u5B8C",
            style = SumiTheme.typography.cjk.copy(
                fontSize = (size.value * 0.55f).sp,
                color = cream,
            ),
        )
    }
}

@Composable
private fun InlineSecondaryStats(mistakeCount: Int, moveCount: Int, difficulty: String) {
    val levelKanji = difficultyKanji(difficulty)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.semantics(mergeDescendants = true) {
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
            color = SumiTheme.colors.ink,
        )
        Spacer(Modifier.size(Sumi.Space.s1))
        Text(
            text = label,
            style = SumiTheme.typography.uiMeta.copy(fontSize = 12.sp),
            color = SumiTheme.colors.inkSoft,
        )
    }
}

@Composable
private fun StatSeparator() {
    Text(
        text = "/",
        style = SumiTheme.typography.uiMeta.copy(fontSize = 14.sp),
        color = SumiTheme.colors.paperEdge,
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
            color = SumiTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
        )
        if (attribution.isNotBlank()) {
            Spacer(Modifier.height(Sumi.Space.s1))
            Text(
                text = "\u2014 $attribution",
                style = SumiTheme.typography.uiMeta.copy(fontSize = 11.sp),
                color = SumiTheme.colors.inkFaint,
            )
        }
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
    onHome: () -> Unit,
    onNextPuzzle: (() -> Unit)?,
    onShare: (() -> Unit)?,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Sumi.Space.s2)) {
        if (onNextPuzzle != null) {
            SumiButton(
                onClick = onNextPuzzle,
                modifier = Modifier.fillMaxWidth(),
                size = SumiButtonSize.Lg,
            ) {
                Text(text = "Next Practice →", style = SumiTheme.typography.uiButton)
            }
        }
        if (onShare != null) {
            SumiButton(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
                variant = SumiButtonVariant.Ghost,
                size = SumiButtonSize.Md,
            ) {
                Text(text = "Share Result", style = SumiTheme.typography.uiButton)
            }
        }
        SumiButton(
            onClick = onHome,
            modifier = Modifier.fillMaxWidth(),
            variant = SumiButtonVariant.Ghost,
            size = SumiButtonSize.Md,
        ) {
            Text(text = "Return Home", style = SumiTheme.typography.uiButton)
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
}
