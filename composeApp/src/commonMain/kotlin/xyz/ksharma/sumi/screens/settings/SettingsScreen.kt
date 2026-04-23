package xyz.ksharma.sumi.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLicenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsTopBar(onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Sumi.Space.s6)
                    .padding(bottom = Sumi.Space.s9),
            ) {
                Spacer(Modifier.height(Sumi.Space.s4))

                SettingsSectionLabel("About")
                Spacer(Modifier.height(Sumi.Space.s3))

                SettingsPanel {
                    SettingsRow(
                        label = "Open Licenses",
                        description = "Typefaces and libraries used in Sumi.",
                    )
                    Spacer(Modifier.height(Sumi.Space.s3))
                    SumiTextButton(
                        text = "View Licenses",
                        onClick = onLicenses,
                        variant = SumiButtonVariant.Ghost,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(Sumi.Layout.minTap)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "←", style = SumiTheme.typography.h3, color = SumiTheme.colors.ink)
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "Settings",
            style = SumiTheme.typography.uiLabel,
            color = SumiTheme.colors.inkSoft,
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = SumiTheme.typography.uiLabel,
        color = SumiTheme.colors.red,
    )
}

@Composable
private fun SettingsPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Sumi.Space.s2),
    ) {
        HorizontalDivider(color = SumiTheme.colors.paperEdge, thickness = 1.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Sumi.Space.s4),
        ) {
            content()
        }
        HorizontalDivider(color = SumiTheme.colors.paperEdge, thickness = 1.dp)
    }
}

@Composable
private fun SettingsRow(
    label: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = SumiTheme.typography.bodySmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight(Sumi.Weight.SEMI),
            ),
            color = SumiTheme.colors.ink,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = description,
            style = SumiTheme.typography.caption,
            color = SumiTheme.colors.inkFaint,
        )
    }
}
