@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.screens.gameover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Composable
fun GameOverScreen(
    difficulty: String,
    elapsedLabel: String,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Sumi.Space.s8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // The brush "X" mark — a quiet full stop on the run, in the calligraphy red.
            Text(
                text = "終",
                style = SumiTheme.typography.h1,
                color = SumiTheme.colors.red,
            )
            Spacer(Modifier.height(Sumi.Space.s5))
            Text(
                text = "Out of lives",
                style = SumiTheme.typography.h2,
                color = SumiTheme.colors.ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Sumi.Space.s3))
            Text(
                text = "$difficulty   $elapsedLabel",
                style = SumiTheme.typography.body,
                color = SumiTheme.colors.inkSoft,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Sumi.Space.s8))
            SumiTextButton(
                text = "Try again",
                onClick = onRetry,
                variant = SumiButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Sumi.Space.s3))
            SumiTextButton(
                text = "Home",
                onClick = onHome,
                variant = SumiButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
