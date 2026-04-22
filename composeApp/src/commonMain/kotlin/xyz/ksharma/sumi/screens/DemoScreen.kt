package xyz.ksharma.sumi.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.design.components.SumiButton
import xyz.ksharma.sumi.design.components.SumiButtonSize
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiChip
import xyz.ksharma.sumi.design.components.SumiChipTone
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTokens as Sumi
import xyz.ksharma.sumi.theme.SumiTheme

@Composable
fun DemoScreen(modifier: Modifier = Modifier) {
    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s7),
            verticalArrangement = Arrangement.spacedBy(Sumi.Space.s6),
        ) {
            // ── Typography ──────────────────────────────────────────────────
            SectionLabel("Typography")

            Text("Display italic", style = SumiTheme.typography.h1, color = Sumi.Color.ink)
            Text("Heading two", style = SumiTheme.typography.h2, color = Sumi.Color.ink)
            Text("Heading three", style = SumiTheme.typography.h3, color = Sumi.Color.ink)
            Text("Subheading text", style = SumiTheme.typography.subhead, color = Sumi.Color.ink)
            Text(
                "Body — each puzzle is a small practice in patience. The grid asks nothing of you but attention.",
                style = SumiTheme.typography.body,
                color = Sumi.Color.inkSoft,
            )
            Text("Caption text", style = SumiTheme.typography.caption, color = Sumi.Color.inkFaint)
            Text("UI BUTTON LABEL", style = SumiTheme.typography.uiButton, color = Sumi.Color.ink)
            Text("UI SMALL LABEL", style = SumiTheme.typography.uiLabel, color = Sumi.Color.ink)
            Text(
                "486 puzzles solved, all time.",
                style = SumiTheme.typography.quote,
                color = Sumi.Color.inkSoft,
            )
            Text("墨 休 完", style = SumiTheme.typography.cjk.copy(fontSize = Sumi.Size.h2), color = Sumi.Color.red)
            Text("7  4  9", style = SumiTheme.typography.numeral, color = Sumi.Color.ink)
            Text("3  1  8", style = SumiTheme.typography.hand, color = Sumi.Color.teal)

            Spacer(Modifier.height(Sumi.Space.s2))
            SectionDivider()

            // ── Buttons ─────────────────────────────────────────────────────
            SectionLabel("Buttons")

            Column(verticalArrangement = Arrangement.spacedBy(Sumi.Space.s3)) {
                SumiTextButton("Begin practice", onClick = {}, variant = SumiButtonVariant.Primary, modifier = Modifier.fillMaxWidth())
                SumiTextButton("Continue", onClick = {}, variant = SumiButtonVariant.Ghost, modifier = Modifier.fillMaxWidth())
                SumiTextButton("Today's puzzle", onClick = {}, variant = SumiButtonVariant.Subtle, modifier = Modifier.fillMaxWidth())
                SumiTextButton("Reset grid", onClick = {}, variant = SumiButtonVariant.Red, modifier = Modifier.fillMaxWidth())
            }

            // Sizes
            Row(horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s3)) {
                SumiTextButton("Sm", onClick = {}, size = SumiButtonSize.Sm)
                SumiTextButton("Medium", onClick = {}, size = SumiButtonSize.Md)
                SumiTextButton("Large btn", onClick = {}, size = SumiButtonSize.Lg)
            }

            // Disabled
            Row(horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s3)) {
                SumiTextButton("Disabled", onClick = {}, enabled = false)
                SumiTextButton("Disabled", onClick = {}, variant = SumiButtonVariant.Ghost, enabled = false)
            }

            SectionDivider()

            // ── Chips ────────────────────────────────────────────────────────
            SectionLabel("Chips")

            Row(horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s2)) {
                SumiChip("Ink",   tone = SumiChipTone.Ink)
                SumiChip("Today", tone = SumiChipTone.Red)
                SumiChip("Teal",  tone = SumiChipTone.Teal)
                SumiChip("Pro",   tone = SumiChipTone.Gold)
                SumiChip("Muted", tone = SumiChipTone.Muted)
            }

            Spacer(Modifier.height(Sumi.Space.s7))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = SumiTheme.typography.uiLabel,
        color = Sumi.Color.red,
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(color = Sumi.Color.paperEdge, thickness = 1.dp)
}
