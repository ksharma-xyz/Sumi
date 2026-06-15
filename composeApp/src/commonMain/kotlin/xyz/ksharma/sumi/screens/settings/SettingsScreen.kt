package xyz.ksharma.sumi.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import xyz.ksharma.sumi.design.components.SumiButtonVariant
import xyz.ksharma.sumi.design.components.SumiTextButton
import xyz.ksharma.sumi.design.components.WashiBG
import xyz.ksharma.sumi.theme.SumiSeason
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

@Suppress("LongParameterList", "LongMethod")
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLicenses: () -> Unit,
    themeState: ThemeState,
    onSeasonSelect: (SumiSeason) -> Unit,
    onHapticsToggle: () -> Unit,
    onShowMistakesToggle: () -> Unit,
    onHighLegibilityToggle: () -> Unit,
    debugCallbacks: DebugCallbacks?,
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

                SettingsSectionLabel("Theme")
                Spacer(Modifier.height(Sumi.Space.s3))
                SettingsPanel {
                    SettingsRow(
                        label = "Season",
                        description = "Changes the accent colour across the app.",
                    )
                    Spacer(Modifier.height(Sumi.Space.s4))
                    SeasonChips(current = themeState.season, onSelect = onSeasonSelect)
                }

                Spacer(Modifier.height(Sumi.Space.s3))
                SettingsPanel {
                    SettingsToggleRow(
                        label = "Haptics",
                        description = "Vibration feedback while playing.",
                        checked = themeState.hapticsEnabled,
                        onToggle = onHapticsToggle,
                    )
                }

                Spacer(Modifier.height(Sumi.Space.s3))
                SettingsPanel {
                    SettingsToggleRow(
                        label = "Show mistake count",
                        description = "Hide the running mistake counter on the game screen.",
                        checked = themeState.showMistakes,
                        onToggle = onShowMistakesToggle,
                    )
                }

                Spacer(Modifier.height(Sumi.Space.s3))
                SettingsPanel {
                    SettingsToggleRow(
                        label = "High-legibility numerals",
                        description = "Use a clearer sans typeface for board digits.",
                        checked = themeState.highLegibility,
                        onToggle = onHighLegibilityToggle,
                    )
                }

                Spacer(Modifier.height(Sumi.Space.s6))
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

                if (debugCallbacks != null) {
                    Spacer(Modifier.height(Sumi.Space.s6))
                    DebugSection(callbacks = debugCallbacks)
                }
            }
        }
    }
}

// FlowRow so chips wrap onto the next line at large font scales instead of the
// trailing chip getting squished.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeasonChips(current: SumiSeason, onSelect: (SumiSeason) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
        verticalArrangement = Arrangement.spacedBy(Sumi.Space.s4),
    ) {
        SumiSeason.entries.forEach { season ->
            SeasonChip(
                season = season,
                selected = season == current,
                onSelect = onSelect,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SeasonChip(
    season: SumiSeason,
    selected: Boolean,
    onSelect: (SumiSeason) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = Sumi.Color.Season.forSeason(season).accent
    val label = when (season) {
        SumiSeason.Spring -> "春 Spring"
        SumiSeason.Summer -> "夏 Summer"
        SumiSeason.Autumn -> "秋 Autumn"
        SumiSeason.Winter -> "冬 Winter"
    }
    val src = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(interactionSource = src, indication = null) { onSelect(season) },
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = if (selected) 1f else 0.25f))
                .then(if (selected) Modifier.border(2.dp, accent, CircleShape) else Modifier),
        )
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = label,
            style = SumiTheme.typography.uiMeta,
            color = if (selected) SumiTheme.colors.ink else SumiTheme.colors.inkSoft,
        )
    }
}

// Linear list of debug-only items — splitting hurts scannability.
@Suppress("LongMethod")
@Composable
private fun DebugSection(callbacks: DebugCallbacks) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsSectionLabel("Debug")
        Spacer(Modifier.height(Sumi.Space.s3))
        SettingsPanel {
            SettingsToggleRow(
                label = "Simulate Pro",
                description = "Unlocks all Pro features. Persists across launches.",
                checked = callbacks.isSimulatingPro,
                onToggle = callbacks.onToggleSimulatePro,
            )
        }
        Spacer(Modifier.height(Sumi.Space.s4))
        SettingsPanel {
            SettingsToggleRow(
                label = "Show Ads",
                description = "Toggle ad placeholders on/off.",
                checked = callbacks.isAdsEnabled,
                onToggle = callbacks.onToggleAdsEnabled,
            )
        }
        Spacer(Modifier.height(Sumi.Space.s4))
        DebugItem(
            label = "Seed: 5-day streak",
            description = "Marks the last 5 days as solved + 20 total puzzles. For QA of Stats / Daily / Win.",
            buttonText = "Seed",
            onClick = callbacks.onSeedSampleStreak,
        )
        Spacer(Modifier.height(Sumi.Space.s4))
        DebugItem(
            label = "Open Win screen",
            description = "Jumps to a sample Win result page so the screen can be QA'd without solving.",
            buttonText = "Open",
            onClick = callbacks.onOpenSampleWin,
        )
        Spacer(Modifier.height(Sumi.Space.s4))
        DebugItem(
            label = "Reset Onboarding",
            description = "Shows intro slides on next launch.",
            buttonText = "Reset",
            onClick = callbacks.onResetOnboarding,
        )
        Spacer(Modifier.height(Sumi.Space.s4))
        DebugItem(
            label = "Clear Streak & Stats",
            description = "Wipes streak, best streak, total puzzles solved.",
            buttonText = "Clear Stats",
            onClick = callbacks.onClearStats,
        )
        Spacer(Modifier.height(Sumi.Space.s4))
        DebugItem(
            label = "Clear Game Saves",
            description = "Discards in-progress puzzles for all difficulties.",
            buttonText = "Clear Saves",
            onClick = callbacks.onClearSaves,
        )
        Spacer(Modifier.height(Sumi.Space.s4))
        DebugItem(
            label = "Nuke All Data",
            description = "Wipes everything — onboarding, stats, saves. Fresh install.",
            buttonText = "Clear All",
            onClick = callbacks.onClearAll,
        )
    }
}

private const val CONFIRM_VISIBLE_MS = 2000L

@Composable
private fun DebugItem(
    label: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
) {
    var confirmed by remember { mutableStateOf(false) }
    LaunchedEffect(confirmed) {
        if (confirmed) {
            delay(CONFIRM_VISIBLE_MS)
            confirmed = false
        }
    }
    SettingsPanel {
        SettingsRow(label = label, description = description)
        Spacer(Modifier.height(Sumi.Space.s3))
        SumiTextButton(
            text = if (confirmed) "All cleared" else buttonText,
            onClick = {
                onClick()
                confirmed = true
            },
            variant = SumiButtonVariant.Ghost,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
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
private fun SettingsToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val src = remember { MutableInteractionSource() }
    Row(
        modifier = modifier.fillMaxWidth()
            .clickable(interactionSource = src, indication = null, onClick = onToggle),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = SumiTheme.typography.bodySmall.copy(fontWeight = FontWeight(Sumi.Weight.SEMI)),
                color = SumiTheme.colors.ink,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = SumiTheme.typography.caption,
                color = SumiTheme.colors.inkFaint,
            )
        }
        Spacer(Modifier.width(Sumi.Space.s4))
        Text(
            text = if (checked) "On" else "Off",
            style = SumiTheme.typography.uiLabel,
            color = if (checked) SumiTheme.colors.red else SumiTheme.colors.inkFaint,
        )
    }
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
