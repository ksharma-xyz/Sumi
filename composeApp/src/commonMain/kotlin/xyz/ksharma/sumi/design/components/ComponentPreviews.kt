package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.ksharma.darpan.ScreenshotTest
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.ui.preview.AppPreviewTheme
import xyz.ksharma.sumi.ui.preview.PreviewComponent

// A solved grid, row-major, for the thumbnail preview.
private const val SOLVED_GRID =
    "534678912" +
        "672195348" +
        "198342567" +
        "859761423" +
        "426853791" +
        "713924856" +
        "961537284" +
        "287419635" +
        "345286179"

@ScreenshotTest
@PreviewComponent
@Composable
internal fun SumiButtonVariantsPreview() {
    AppPreviewTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SumiButtonVariant.entries.forEach { variant ->
                SumiButton(onClick = {}, variant = variant) {
                    Text(text = variant.name, style = SumiTheme.typography.uiLabel)
                }
            }
        }
    }
}

@ScreenshotTest
@PreviewComponent
@Composable
internal fun SumiButtonSizesPreview() {
    AppPreviewTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SumiButtonSize.entries.forEach { size ->
                SumiButton(onClick = {}, size = size) {
                    Text(text = size.name, style = SumiTheme.typography.uiLabel)
                }
            }
            // Disabled is a distinct visual state, not just a flag.
            SumiButton(onClick = {}, enabled = false) {
                Text(text = "Disabled", style = SumiTheme.typography.uiLabel)
            }
        }
    }
}

@ScreenshotTest
@PreviewComponent
@Composable
internal fun SumiTextButtonPreview() {
    AppPreviewTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SumiTextButton(text = "Restore purchase", onClick = {})
            SumiTextButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}

@ScreenshotTest
@PreviewComponent
@Composable
internal fun SumiChipTonesPreview() {
    AppPreviewTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SumiChipTone.entries.forEach { tone ->
                SumiChip(text = tone.name, tone = tone)
            }
        }
    }
}

@ScreenshotTest
@PreviewComponent
@Composable
internal fun SumiEyebrowPreview() {
    AppPreviewTheme {
        SumiEyebrow(text = "Daily puzzle")
    }
}

@ScreenshotTest(fontScaleSensitive = false)
@PreviewComponent
@Composable
internal fun SumiIconsPreview() {
    AppPreviewTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(
                SumiIcons.Undo,
                SumiIcons.Redo,
                SumiIcons.Erase,
                SumiIcons.Note,
                SumiIcons.Pause,
                SumiIcons.Check,
            ).forEach { icon ->
                SumiIcon(icon = icon, contentDescription = null)
            }
        }
    }
}

@ScreenshotTest
@PreviewComponent
@Composable
internal fun QuoteRulePreview() {
    AppPreviewTheme {
        QuoteRule()
    }
}

@ScreenshotTest
@PreviewComponent
@Composable
internal fun SealPreview() {
    AppPreviewTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Seal()
            SealComplete()
            SealInk()
        }
    }
}

@ScreenshotTest
@PreviewComponent
@Composable
internal fun SudokuThumbnailPreview() {
    AppPreviewTheme {
        SudokuThumbnail(cells = SOLVED_GRID)
    }
}

@ScreenshotTest(fontScaleSensitive = false)
@PreviewComponent
@Composable
internal fun InkStainPreview() {
    AppPreviewTheme {
        Box(modifier = Modifier.size(240.dp)) {
            InkStain()
        }
    }
}

@ScreenshotTest(fontScaleSensitive = false)
@PreviewComponent
@Composable
internal fun InkBleedPreview() {
    AppPreviewTheme {
        Box(modifier = Modifier.size(160.dp)) {
            InkBleed()
        }
    }
}
