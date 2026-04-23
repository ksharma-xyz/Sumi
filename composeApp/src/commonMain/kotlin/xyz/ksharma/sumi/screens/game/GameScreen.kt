package xyz.ksharma.sumi.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiChip
import xyz.ksharma.sumi.design.components.SumiChipTone
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun GameScreen(
    difficulty: String,
    onBack: () -> Unit,
    onWin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s7),
            verticalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
        ) {
            SumiChip(text = difficulty, tone = SumiChipTone.Ink)
            Text(
                text = "The Board",
                style = SumiTheme.typography.h2,
                color = Sumi.Color.ink,
            )
            Text(
                text = "Sudoku board coming in Stage 2.",
                style = SumiTheme.typography.body,
                color = Sumi.Color.inkSoft,
            )
            Spacer(Modifier.weight(1f))
            SumiTextButton(
                text = "Simulate Win",
                onClick = onWin,
                modifier = Modifier.fillMaxWidth(),
            )
            SumiTextButton(
                text = "Back",
                onClick = onBack,
                variant = SumiButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
