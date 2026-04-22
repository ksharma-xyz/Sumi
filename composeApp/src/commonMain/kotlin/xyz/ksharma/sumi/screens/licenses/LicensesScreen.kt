package xyz.ksharma.sumi.screens.licenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private data class LicenseEntry(
    val name: String,
    val author: String,
    val license: String,
    val note: String = "",
)

private val FONT_LICENSES = listOf(
    LicenseEntry(
        name = "Cormorant Garamond",
        author = "Christian Thalmann",
        license = "SIL Open Font License 1.1",
    ),
    LicenseEntry(
        name = "Source Serif 4",
        author = "Frank Grießhammer / Adobe",
        license = "SIL Open Font License 1.1",
    ),
    LicenseEntry(
        name = "Inter",
        author = "Rasmus Andersson",
        license = "SIL Open Font License 1.1",
    ),
    LicenseEntry(
        name = "Caveat",
        author = "Impallari Type",
        license = "SIL Open Font License 1.1",
    ),
    LicenseEntry(
        name = "Shippori Mincho",
        author = "Fontdasu",
        license = "SIL Open Font License 1.1",
    ),
)

private val LIBRARY_LICENSES = listOf(
    LicenseEntry(
        name = "Kotlin",
        author = "JetBrains s.r.o.",
        license = "Apache License 2.0",
    ),
    LicenseEntry(
        name = "Compose Multiplatform",
        author = "JetBrains s.r.o.",
        license = "Apache License 2.0",
    ),
    LicenseEntry(
        name = "Koin",
        author = "insert-koin.io",
        license = "Apache License 2.0",
    ),
    LicenseEntry(
        name = "SQLDelight",
        author = "Cash App",
        license = "Apache License 2.0",
    ),
    LicenseEntry(
        name = "Ktor",
        author = "JetBrains s.r.o.",
        license = "Apache License 2.0",
    ),
    LicenseEntry(
        name = "kotlinx.datetime",
        author = "JetBrains s.r.o.",
        license = "Apache License 2.0",
    ),
    LicenseEntry(
        name = "kotlinx.serialization",
        author = "JetBrains s.r.o.",
        license = "Apache License 2.0",
    ),
)

@Composable
fun LicensesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WashiBG(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LicensesTopBar(onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Sumi.Space.s6)
                    .padding(bottom = Sumi.Space.s9),
            ) {
                Spacer(Modifier.height(Sumi.Space.s4))

                SectionEyebrow("Typefaces")
                Spacer(Modifier.height(Sumi.Space.s3))
                FONT_LICENSES.forEach { entry ->
                    LicenseRow(entry)
                    HorizontalDivider(color = Sumi.Color.paperEdge, thickness = 1.dp)
                }

                Spacer(Modifier.height(Sumi.Space.s7))

                SectionEyebrow("Open Source Libraries")
                Spacer(Modifier.height(Sumi.Space.s3))
                LIBRARY_LICENSES.forEach { entry ->
                    LicenseRow(entry)
                    HorizontalDivider(color = Sumi.Color.paperEdge, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun LicensesTopBar(onBack: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Sumi.Space.s6, vertical = Sumi.Space.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "←",
            style = SumiTheme.typography.h3,
            color = Sumi.Color.ink,
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onBack,
            ),
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "Open Licenses",
            style = SumiTheme.typography.uiLabel,
            color = Sumi.Color.inkSoft,
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SectionEyebrow(text: String) {
    Text(
        text = text.uppercase(),
        style = SumiTheme.typography.uiLabel,
        color = Sumi.Color.red,
    )
}

@Composable
private fun LicenseRow(entry: LicenseEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Sumi.Space.s4),
    ) {
        Text(
            text = entry.name,
            style = SumiTheme.typography.bodySmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight(Sumi.Weight.SEMI),
            ),
            color = Sumi.Color.ink,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = entry.author,
            style = SumiTheme.typography.caption,
            color = Sumi.Color.inkFaint,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = entry.license,
            style = SumiTheme.typography.uiMeta,
            color = Sumi.Color.teal,
        )
        if (entry.note.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = entry.note,
                style = SumiTheme.typography.caption,
                color = Sumi.Color.inkGhost,
            )
        }
    }
}
