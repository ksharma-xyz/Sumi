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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import xyz.ksharma.sumi.design.components.LogoEnso
import xyz.ksharma.sumi.design.components.QuoteRule
import xyz.ksharma.sumi.design.components.SumiButtonVariant
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

// TODO: Replace dummy URLs with real privacy policy and terms of service before release
private const val URL_PRIVACY = "https://example.com/privacy"
private const val URL_TERMS = "https://example.com/terms"

private val PRO_FEATURES = listOf(
    "Remove all ads. Forever quiet.",
    "Unlimited hints whenever you need them",
    "The full quote library, 600 passages",
    "Hard, Master, Edo difficulties",
    "The Salon, weekly global register",
    "Practice log with stats and streaks",
    "Gold, Indigo, Edo themes",
    "Export PDF puzzle books",
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
    val yearlyPrice by proRepo.observePrice(ProProducts.YEARLY).collectAsState(initial = null)
    val monthlyPrice by proRepo.observePrice(ProProducts.MONTHLY).collectAsState(initial = null)

    val rowAlphas = remember { List(PRO_FEATURES.size) { Animatable(0f) } }
    val rowOffsets = remember { List(PRO_FEATURES.size) { Animatable(24f) } }
    val pricingAlpha = remember { Animatable(0f) }
    val pricingOffset = remember { Animatable(20f) }
    val ensoProgress = remember { Animatable(0f) }
    val headerAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launchPaywallAnimation(rowAlphas, rowOffsets, pricingAlpha, pricingOffset, ensoProgress, headerAlpha)
    }

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
        PaywallContent(
            ensoProgress = ensoProgress.value,
            headerAlpha = headerAlpha.value,
            rowAlphas = rowAlphas.map { it.value },
            rowOffsets = rowOffsets.map { it.value },
            pricingAlpha = pricingAlpha.value,
            pricingOffset = pricingOffset.value,
            yearlyPrice = yearlyPrice,
            monthlyPrice = monthlyPrice,
            onPurchaseYearly = { scope.launch { proRepo.purchase(ProProducts.YEARLY) } },
            onPurchaseMonthly = { scope.launch { proRepo.purchase(ProProducts.MONTHLY) } },
            onRestore = { scope.launch { proRepo.restorePurchases() } },
            onDismiss = onBack,
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
    yearlyPrice: String?,
    monthlyPrice: String?,
    onPurchaseYearly: () -> Unit,
    onPurchaseMonthly: () -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
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
            yearlyPrice = yearlyPrice,
            monthlyPrice = monthlyPrice,
            onPurchaseYearly = onPurchaseYearly,
            onPurchaseMonthly = onPurchaseMonthly,
            onRestore = onRestore,
            onDismiss = onDismiss,
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

@Suppress("LongParameterList")
@Composable
private fun PaywallPricing(
    alpha: Float,
    offsetDp: Float,
    yearlyPrice: String?,
    monthlyPrice: String?,
    onPurchaseYearly: () -> Unit,
    onPurchaseMonthly: () -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val offsetPx = with(density) { offsetDp.dp.toPx() }
    val yearlyLabel = yearlyPrice?.let { "$it / year" } ?: "Yearly subscription"
    val monthlyLabel = monthlyPrice?.let { "$it / month" } ?: "Monthly subscription"
    val pricesLoading = yearlyPrice == null && monthlyPrice == null
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().graphicsLayer {
            this.alpha = alpha
            translationY = offsetPx
        },
    ) {
        QuoteRule(color = SumiTheme.colors.paperEdge, ornament = "墓")
        Spacer(Modifier.height(Sumi.Space.s5))
        // Prices come from the store (Play Billing / StoreKit) via [ProRepository.observePrice].
        // While the flow returns null we show the placeholder labels above; never hardcode currency
        // or amount — both Apple and Google reject paywalls with mismatched / hardcoded prices.
        SumiTextButton(
            text = yearlyLabel,
            onClick = onPurchaseYearly,
            modifier = Modifier.fillMaxWidth(),
            enabled = !pricesLoading,
        )
        Spacer(Modifier.height(Sumi.Space.s2))
        SumiTextButton(
            text = monthlyLabel,
            onClick = onPurchaseMonthly,
            variant = SumiButtonVariant.Ghost,
            modifier = Modifier.fillMaxWidth(),
            enabled = !pricesLoading,
        )
        Spacer(Modifier.height(Sumi.Space.s4))
        PaywallFooterLinks(onRestore = onRestore, onDismiss = onDismiss)
    }
}

@Composable
private fun PaywallFooterLinks(onRestore: () -> Unit, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val restoreSource = remember { MutableInteractionSource() }
    val notNowSource = remember { MutableInteractionSource() }
    val termsSource = remember { MutableInteractionSource() }
    val privacySource = remember { MutableInteractionSource() }
    Row(
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Restore",
            style = SumiTheme.typography.uiMeta,
            color = SumiTheme.colors.inkFaint,
            modifier = Modifier.clickable(
                interactionSource = restoreSource,
                indication = null,
                onClick = onRestore,
            ),
        )
        Text(text = "/", style = SumiTheme.typography.uiMeta, color = SumiTheme.colors.inkFaint)
        Text(
            text = "Not now",
            style = SumiTheme.typography.uiMeta,
            color = SumiTheme.colors.inkFaint,
            modifier = Modifier.clickable(
                interactionSource = notNowSource,
                indication = null,
                onClick = onDismiss,
            ),
        )
        Text(text = "/", style = SumiTheme.typography.uiMeta, color = SumiTheme.colors.inkFaint)
        Text(
            text = "Terms",
            style = SumiTheme.typography.uiMeta,
            color = SumiTheme.colors.inkFaint,
            modifier = Modifier.clickable(
                interactionSource = termsSource,
                indication = null,
                onClick = { uriHandler.openUri(URL_TERMS) },
            ),
        )
        Text(text = "/", style = SumiTheme.typography.uiMeta, color = SumiTheme.colors.inkFaint)
        Text(
            text = "Privacy",
            style = SumiTheme.typography.uiMeta,
            color = SumiTheme.colors.inkFaint,
            modifier = Modifier.clickable(
                interactionSource = privacySource,
                indication = null,
                onClick = { uriHandler.openUri(URL_PRIVACY) },
            ),
        )
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
