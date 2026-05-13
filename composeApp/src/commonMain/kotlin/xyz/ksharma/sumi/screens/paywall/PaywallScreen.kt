@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.paywall

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import xyz.ksharma.sumi.coroutines.ext.launchWithExceptionHandler
import xyz.ksharma.sumi.design.components.LogoEnso
import xyz.ksharma.sumi.design.components.QuoteRule
import xyz.ksharma.sumi.design.components.SumiIcon
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.preferences.ProProducts
import xyz.ksharma.sumi.preferences.ProRepository
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.resources.ink_bleed_01
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private val PRO_FEATURES = listOf(
    "Remove all ads. Forever quiet.",
    "Unlimited hints whenever you need them",
    "The full quote library, 600 passages",
    "Hard, Master, Edo difficulties",
    "The Salon, weekly global register",
    "Practice log with stats and streaks",
    "Gold, Indigo, Edo themes",
    "Export PDF Zen Sudoku books",
)

// Stagger constants
private const val ROW_STAGGER_MS = 60
private const val ROW_APPEAR_MS = 280
private const val PRICING_DELAY_MS = 120L // delay after last row
private const val PRICING_APPEAR_MS = 300
private const val LOGO_DRAW_MS = 700
private const val LOGO_TEXT_DELAY_MS = 300L

@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val proRepo = koinInject<ProRepository>()
    val scope = rememberCoroutineScope()

    val rowAlphas = remember { List(PRO_FEATURES.size) { Animatable(0f) } }
    val rowOffsets = remember { List(PRO_FEATURES.size) { Animatable(24f) } }
    val pricingAlpha = remember { Animatable(0f) }
    val pricingOffset = remember { Animatable(20f) }
    val ensoProgress = remember { Animatable(0f) }
    val headerAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launchPaywallAnimation(rowAlphas, rowOffsets, pricingAlpha, pricingOffset, ensoProgress, headerAlpha)
    }

    var purchaseError by remember { mutableStateOf(false) }

    WashiBG(modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Image(
                painter = painterResource(Res.drawable.ink_bleed_01),
                contentDescription = null,
                modifier = Modifier.size(400.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.14f,
            )
        }
        PaywallCloseButton(onBack = onBack)
        PaywallContent(
            ensoProgress = ensoProgress.value,
            headerAlpha = headerAlpha.value,
            rowAlphas = rowAlphas.map { it.value },
            rowOffsets = rowOffsets.map { it.value },
            pricingAlpha = pricingAlpha.value,
            pricingOffset = pricingOffset.value,
            purchaseError = purchaseError,
            onPurchase = {
                purchaseError = false
                scope.launchWithExceptionHandler<ProRepository>(
                    dispatcher = Dispatchers.Main,
                    errorBlock = { purchaseError = true },
                ) {
                    val result = proRepo.purchase(ProProducts.LIFETIME)
                    if (result.isFailure) purchaseError = true
                }
            },
            onRestore = {
                scope.launchWithExceptionHandler<ProRepository>(Dispatchers.Default) {
                    proRepo.restorePurchases()
                }
            },
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun PaywallContent(
    ensoProgress: Float,
    headerAlpha: Float,
    rowAlphas: List<Float>,
    rowOffsets: List<Float>,
    pricingAlpha: Float,
    pricingOffset: Float,
    purchaseError: Boolean,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Sumi.Space.s6),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = Sumi.Space.s7, bottom = Sumi.Space.s4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PaywallHeader(ensoProgress = ensoProgress, textAlpha = headerAlpha)
            PaywallFeatures(rowAlphas = rowAlphas, rowOffsets = rowOffsets)
        }
        PaywallPricing(
            alpha = pricingAlpha,
            offsetDp = pricingOffset,
            purchaseError = purchaseError,
            onPurchase = onPurchase,
            onRestore = onRestore,
        )
        Spacer(Modifier.height(Sumi.Space.s6))
    }
}

private fun CoroutineScope.launchPaywallAnimation(
    rowAlphas: List<Animatable<Float, *>>,
    rowOffsets: List<Animatable<Float, *>>,
    pricingAlpha: Animatable<Float, *>,
    pricingOffset: Animatable<Float, *>,
    ensoProgress: Animatable<Float, *>,
    headerAlpha: Animatable<Float, *>,
) {
    val lastRowDelay = (PRO_FEATURES.size - 1) * ROW_STAGGER_MS
    PRO_FEATURES.indices.forEach { i ->
        launch {
            delay((i * ROW_STAGGER_MS).toLong())
            launch { rowAlphas[i].animateTo(1f, tween(ROW_APPEAR_MS, easing = Sumi.Ease.paper)) }
            launch { rowOffsets[i].animateTo(0f, tween(ROW_APPEAR_MS, easing = Sumi.Ease.paper)) }
        }
    }
    launch {
        delay(lastRowDelay + ROW_APPEAR_MS - 40L + PRICING_DELAY_MS)
        launch { pricingAlpha.animateTo(1f, tween(PRICING_APPEAR_MS, easing = Sumi.Ease.paper)) }
        launch { pricingOffset.animateTo(0f, tween(PRICING_APPEAR_MS, easing = Sumi.Ease.paper)) }
    }
    launch {
        delay(lastRowDelay + ROW_APPEAR_MS.toLong())
        ensoProgress.animateTo(1f, tween(LOGO_DRAW_MS, easing = Sumi.Ease.brush))
    }
    launch {
        delay(lastRowDelay + ROW_APPEAR_MS + LOGO_TEXT_DELAY_MS)
        headerAlpha.animateTo(1f, tween(400, easing = Sumi.Ease.paper))
    }
}

@Composable
private fun PaywallHeader(ensoProgress: Float, textAlpha: Float) {
    val density = LocalDensity.current
    val offset8px = with(density) { 8.dp.toPx() }
    val offset6px = with(density) { 6.dp.toPx() }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LogoEnso(size = Sumi.Space.s11, color = SumiTheme.colors.gold, progress = ensoProgress)
        Spacer(Modifier.height(Sumi.Space.s5))
        Text(
            text = "Sumi Pro",
            style = SumiTheme.typography.h1,
            color = SumiTheme.colors.ink,
            modifier = Modifier.graphicsLayer {
                alpha = textAlpha
                translationY = (1f - textAlpha) * offset8px
            },
        )
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = "An uninterrupted practice.",
            style = SumiTheme.typography.subhead,
            color = SumiTheme.colors.inkSoft,
            modifier = Modifier.graphicsLayer {
                alpha = textAlpha
                translationY = (1f - textAlpha) * offset6px
            },
        )
        Spacer(Modifier.height(Sumi.Space.s6))
    }
}

@Composable
private fun PaywallFeatures(rowAlphas: List<Float>, rowOffsets: List<Float>) {
    val density = LocalDensity.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
    ) {
        PRO_FEATURES.forEachIndexed { i, feature ->
            val offsetPx = with(density) { rowOffsets[i].dp.toPx() }
            FeatureRow(
                text = feature,
                modifier = Modifier.graphicsLayer {
                    alpha = rowAlphas[i]
                    translationY = offsetPx
                },
            )
        }
    }
}

@Composable
private fun PaywallPricing(
    alpha: Float,
    offsetDp: Float,
    purchaseError: Boolean,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
) {
    val density = LocalDensity.current
    val offsetPx = with(density) { offsetDp.dp.toPx() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().graphicsLayer {
            this.alpha = alpha
            translationY = offsetPx
        },
    ) {
        QuoteRule(color = SumiTheme.colors.paperEdge, ornament = "墓")
        Spacer(Modifier.height(Sumi.Space.s5))
        SumiTextButton(
            text = "Unlock Sumi Pro",
            onClick = onPurchase,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Sumi.Space.s2))
        if (purchaseError) {
            Text(
                text = "Something went wrong — please try again.",
                style = SumiTheme.typography.uiMeta,
                color = SumiTheme.colors.red,
            )
        } else {
            Text(
                text = "One-time purchase. No subscriptions.",
                style = SumiTheme.typography.uiMeta,
                color = SumiTheme.colors.inkFaint,
            )
        }
        Spacer(Modifier.height(Sumi.Space.s4))
        PaywallFooterLinks(onRestore = onRestore)
    }
}

@Composable
private fun PaywallFooterLinks(onRestore: () -> Unit) {
    val restoreSource = remember { MutableInteractionSource() }
    Text(
        text = "Restore purchase",
        style = SumiTheme.typography.bodySmall,
        color = SumiTheme.colors.inkSoft,
        modifier = Modifier
            .clickable(
                interactionSource = restoreSource,
                indication = null,
                onClick = onRestore,
            )
            .padding(vertical = Sumi.Space.s3, horizontal = Sumi.Space.s4),
    )
}

@Composable
private fun PaywallCloseButton(onBack: () -> Unit) {
    val closeSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Sumi.Space.s4, vertical = Sumi.Space.s3),
        contentAlignment = Alignment.TopEnd,
    ) {
        Box(
            modifier = Modifier
                .clickable(interactionSource = closeSource, indication = null, onClick = onBack)
                .padding(Sumi.Space.s2),
        ) {
            SumiIcon(
                icon = SumiIcons.Close,
                contentDescription = "Close",
                tint = SumiTheme.colors.inkFaint,
                size = Sumi.Space.s5,
            )
        }
    }
}

@Composable
private fun FeatureRow(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
    ) {
        SumiIcon(
            icon = SumiIcons.Check,
            contentDescription = null,
            tint = SumiTheme.colors.gold,
            size = Sumi.Space.s4,
        )
        Text(text = text, style = SumiTheme.typography.bodySmall, color = SumiTheme.colors.inkSoft)
    }
}
