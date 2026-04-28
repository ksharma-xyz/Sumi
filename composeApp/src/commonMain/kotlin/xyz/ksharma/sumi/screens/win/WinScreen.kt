@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.win

import androidx.compose.foundation.Image
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
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.composable.InterstitialAd
import xyz.ksharma.sumi.Quote
import xyz.ksharma.sumi.ads.AdUnits
import xyz.ksharma.sumi.design.components.SealComplete
import xyz.ksharma.sumi.design.components.SumiButton
import xyz.ksharma.sumi.design.components.SumiButtonSize
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.resources.ink_bleed_02
import xyz.ksharma.sumi.resources.ink_bleed_03
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

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
    showInterstitialAd: Boolean = false,
    onInterstitialDismiss: () -> Unit = {},
) {
    val shareLayer = rememberGraphicsLayer()
    val cardBackground = SumiTheme.colors.paper
    val coroutineScope = rememberCoroutineScope()

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
                shareLayer = shareLayer,
                cardBackground = cardBackground,
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
        if (showInterstitialAd) {
            InterstitialAd(
                adUnitId = AdUnits.Interstitial,
                onDismissed = onInterstitialDismiss,
                // If load/show fails (no fill, network down), behave the same as a dismissal
                // so the user is never blocked on the Win screen waiting for an ad.
                onFailure = { _ -> onInterstitialDismiss() },
            )
        }
    }
}

@Composable
private fun WinShareCard(
    elapsedMs: Long,
    mistakeCount: Int,
    moveCount: Int,
    difficulty: String,
    quote: Quote,
    shareLayer: GraphicsLayer,
    cardBackground: Color,
) {
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
        WinHeroSection()
        Spacer(Modifier.height(Sumi.Space.s6))
        Text(
            text = "The grid is quiet again.",
            style = SumiTheme.typography.quote.copy(fontSize = 24.sp, lineHeight = 32.sp),
            color = SumiTheme.colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Sumi.Space.s5))
        WinStatsRow(
            timeDisplay = formatTime(elapsedMs),
            mistakeCount = mistakeCount,
            moveCount = moveCount,
            difficulty = difficulty,
        )
        Spacer(Modifier.height(Sumi.Space.s5))
        Text(
            text = "\u201C${quote.text}\u201D",
            style = SumiTheme.typography.quote.copy(fontSize = 14.sp),
            color = SumiTheme.colors.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Sumi.Space.s4),
        )
    }
}

@Composable
private fun WinHeroSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "Complete" },
        ) {
            Text(
                text = "完",
                style = SumiTheme.typography.cjk.copy(fontSize = 28.sp),
                color = SumiTheme.colors.red,
                modifier = Modifier.semantics { hideFromAccessibility() },
            )
            Text(
                text = "  ",
                style = SumiTheme.typography.body.copy(fontSize = 14.sp),
                color = SumiTheme.colors.inkSoft,
                modifier = Modifier.semantics { hideFromAccessibility() },
            )
            Text(
                text = "COMPLETE",
                style = SumiTheme.typography.uiLabel,
                color = SumiTheme.colors.inkSoft,
            )
        }
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = "Sumi",
            style = SumiTheme.typography.quote.copy(
                fontSize = 18.sp,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.02f).em,
            ),
            color = SumiTheme.colors.ink,
        )
        Spacer(Modifier.height(Sumi.Space.s6))
        SealComplete(size = 120.dp)
    }
}

@Composable
private fun WinStatsRow(
    timeDisplay: String,
    mistakeCount: Int,
    moveCount: Int,
    difficulty: String,
) {
    val levelKanji = difficultyKanji(difficulty)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = SumiTheme.colors.paperEdge),
    ) {
        val numericStats = listOf(
            "TIME" to timeDisplay,
            "MARKS" to mistakeCount.toString(),
            "MOVES" to moveCount.toString(),
        )
        numericStats.forEachIndexed { i, (label, value) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(if (i > 0) Modifier.border(1.dp, SumiTheme.colors.paperEdge) else Modifier)
                    .padding(vertical = Sumi.Space.s4, horizontal = Sumi.Space.s2)
                    .semantics(mergeDescendants = true) {},
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = label,
                    style = SumiTheme.typography.uiLabel,
                    color = SumiTheme.colors.inkFaint,
                )
                Spacer(Modifier.height(Sumi.Space.s1))
                Text(
                    text = value,
                    style = SumiTheme.typography.numeral.copy(fontSize = 24.sp, fontStyle = FontStyle.Italic),
                    color = SumiTheme.colors.ink,
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, SumiTheme.colors.paperEdge)
                .padding(vertical = Sumi.Space.s4, horizontal = Sumi.Space.s2)
                .semantics(mergeDescendants = true) {},
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "LEVEL", style = SumiTheme.typography.uiLabel, color = SumiTheme.colors.inkFaint)
            Spacer(Modifier.height(Sumi.Space.s1))
            Text(
                text = levelKanji,
                style = SumiTheme.typography.cjk.copy(fontSize = 28.sp),
                color = SumiTheme.colors.ink,
                modifier = Modifier.semantics { contentDescription = difficulty },
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
