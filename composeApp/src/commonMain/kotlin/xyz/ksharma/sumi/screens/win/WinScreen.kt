package xyz.ksharma.sumi.screens.win

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
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun WinScreen(
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s7),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = "完",
                style = SumiTheme.typography.cjk.copy(fontSize = Sumi.Size.h1),
                color = Sumi.Color.red,
            )
            Text(
                text = "Puzzle complete.",
                style = SumiTheme.typography.h3,
                color = Sumi.Color.ink,
            )
            Spacer(Modifier.weight(1f))
            SumiTextButton(
                text = "Return Home",
                onClick = onHome,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
