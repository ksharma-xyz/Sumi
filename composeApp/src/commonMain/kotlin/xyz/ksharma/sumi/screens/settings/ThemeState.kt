package xyz.ksharma.sumi.screens.settings

import xyz.ksharma.sumi.theme.SumiSeason

data class ThemeState(
    val season: SumiSeason,
    val hapticsEnabled: Boolean,
    val showMistakes: Boolean,
    val highLegibility: Boolean,
)
