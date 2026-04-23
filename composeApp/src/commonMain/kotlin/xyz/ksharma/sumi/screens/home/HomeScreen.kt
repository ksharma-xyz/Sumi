package xyz.ksharma.sumi.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun HomeScreen(
    onStartGame: (difficulty: String) -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s7),
            verticalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
        ) {
            Text(
                text = "Home",
                style = SumiTheme.typography.uiLabel,
                color = Sumi.Color.red,
            )
            Text(
                text = "The Threshold",
                style = SumiTheme.typography.h2,
                color = Sumi.Color.ink,
            )
            Text(
                text = "Today's daily puzzle, hand-crafted.",
                style = SumiTheme.typography.body,
                color = Sumi.Color.inkSoft,
            )
            Spacer(Modifier.height(Sumi.Space.s4))
            SumiTextButton(
                text = "Begin Today's Puzzle",
                onClick = { onStartGame("Medium") },
                modifier = Modifier.fillMaxWidth(),
            )
            SumiTextButton(
                text = "Easy",
                onClick = { onStartGame("Easy") },
                variant = SumiButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.weight(1f))
            SumiTextButton(
                text = "Settings",
                onClick = onSettings,
                variant = SumiButtonVariant.Subtle,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
