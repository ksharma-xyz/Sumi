package xyz.ksharma.sumi.screens.paywall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WashiBG(dark = true, modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s7),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = "Sumi Pro",
                style = SumiTheme.typography.h1,
                color = Sumi.Color.Night.paper,
            )
            Text(
                text = "An uninterrupted practice.",
                style = SumiTheme.typography.subhead,
                color = Sumi.Color.Night.inkSoft,
            )
            Spacer(Modifier.weight(1f))
            SumiTextButton(
                text = "$29 / year",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
            SumiTextButton(
                text = "Restore purchase",
                onClick = onBack,
                variant = SumiButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
