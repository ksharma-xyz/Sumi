package xyz.ksharma.sumi.screens.paywall

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import xyz.ksharma.sumi.design.components.LogoEnso
import xyz.ksharma.sumi.design.components.QuoteRule
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiIcon
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private val PRO_FEATURES = listOf(
    "Remove all ads · Forever quiet",
    "Unlimited hints · Use what you need",
    "The full quote library · 600 passages",
    "Hard, Master, Edo difficulties",
    "The Salon · weekly global register",
    "Practice log · stats + streaks",
    "Gold, Indigo, Edo themes",
    "iCloud / Drive sync · across devices",
    "Export PDF puzzle books",
)

@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = Sumi.Space.s6)
                .padding(top = Sumi.Space.s7, bottom = Sumi.Space.s6),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PaywallHeader()
            PaywallFeatures()
            Spacer(Modifier.weight(1f))
            PaywallPricing(onRestorePurchase = onBack)
        }
    }
}

@Composable
private fun PaywallHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LogoEnso(size = Sumi.Space.s11, color = SumiTheme.colors.gold)
        Spacer(Modifier.height(Sumi.Space.s5))
        Text(text = "Sumi Pro", style = SumiTheme.typography.h1, color = SumiTheme.colors.ink)
        Spacer(Modifier.height(Sumi.Space.s2))
        Text(
            text = "An uninterrupted practice.",
            style = SumiTheme.typography.subhead,
            color = SumiTheme.colors.inkSoft,
        )
        Spacer(Modifier.height(Sumi.Space.s6))
    }
}

@Composable
private fun PaywallFeatures() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Sumi.Space.s3),
    ) {
        PRO_FEATURES.forEach { feature -> FeatureRow(text = feature) }
    }
}

@Composable
private fun PaywallPricing(onRestorePurchase: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        QuoteRule(color = SumiTheme.colors.paperEdge, ornament = "墨")
        Spacer(Modifier.height(Sumi.Space.s5))
        SumiTextButton(
            text = "$29 / year  ·  Save 38%",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Sumi.Space.s2))
        SumiTextButton(
            text = "$3.99 / month",
            onClick = {},
            variant = SumiButtonVariant.Ghost,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Sumi.Space.s4))
        Row(
            horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val restoreSource = remember { MutableInteractionSource() }
            Text(
                text = "Restore purchase",
                style = SumiTheme.typography.uiMeta,
                color = SumiTheme.colors.inkFaint,
                modifier = Modifier.clickable(
                    interactionSource = restoreSource,
                    indication = null,
                    onClick = onRestorePurchase,
                ),
            )
            Text(
                text = "·",
                style = SumiTheme.typography.uiMeta,
                color = SumiTheme.colors.inkFaint,
            )
            Text(
                text = "Terms",
                style = SumiTheme.typography.uiMeta,
                color = SumiTheme.colors.inkFaint,
            )
            Text(
                text = "·",
                style = SumiTheme.typography.uiMeta,
                color = SumiTheme.colors.inkFaint,
            )
            Text(
                text = "Privacy",
                style = SumiTheme.typography.uiMeta,
                color = SumiTheme.colors.inkFaint,
            )
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
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
