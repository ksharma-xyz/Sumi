package xyz.ksharma.sumi.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s7),
            verticalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
        ) {
            Text(
                text = "Stats",
                style = SumiTheme.typography.uiLabel,
                color = Sumi.Color.red,
            )
            Text(
                text = "486",
                style = SumiTheme.typography.h1,
                color = Sumi.Color.ink,
            )
            Text(
                text = "puzzles solved, all time.",
                style = SumiTheme.typography.body,
                color = Sumi.Color.inkSoft,
            )
        }
    }
}
