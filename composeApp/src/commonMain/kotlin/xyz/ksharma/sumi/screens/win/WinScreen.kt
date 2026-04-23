package xyz.ksharma.sumi.screens.win

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import xyz.ksharma.sumi.FREE_QUOTES
import xyz.ksharma.sumi.design.components.QuoteRule
import xyz.ksharma.sumi.design.components.SealComplete
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun WinScreen(
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    onNextPuzzle: (() -> Unit)? = null,
    timeDisplay: String = "00:00",
    mistakeCount: Int = 0,
    difficulty: String = "Medium",
) {
    val quote = FREE_QUOTES[1]

    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s7),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            SealComplete(size = Sumi.Space.s11)

            Spacer(Modifier.height(Sumi.Space.s6))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(label = "Time", value = timeDisplay)
                StatItem(label = "Mistakes", value = mistakeCount.toString())
                StatItem(label = "Difficulty", value = difficulty)
            }

            Spacer(Modifier.height(Sumi.Space.s7))

            Text(
                text = "\u201C${quote.text}\u201D",
                style = SumiTheme.typography.quote,
                color = Sumi.Color.inkSoft,
            )
            Spacer(Modifier.height(Sumi.Space.s2))
            Text(
                text = "— ${quote.attribution}",
                style = SumiTheme.typography.caption,
                color = Sumi.Color.inkFaint,
            )

            Spacer(Modifier.height(Sumi.Space.s5))
            QuoteRule()

            Spacer(Modifier.weight(1f))

            if (onNextPuzzle != null) {
                SumiTextButton(
                    text = "Next Puzzle →",
                    onClick = onNextPuzzle,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Sumi.Space.s3))
            }
            SumiTextButton(
                text = "Return Home",
                onClick = onHome,
                variant = SumiButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = SumiTheme.typography.h3,
            color = Sumi.Color.ink,
        )
        Text(
            text = label.uppercase(),
            style = SumiTheme.typography.uiMeta,
            color = Sumi.Color.inkFaint,
        )
    }
}
