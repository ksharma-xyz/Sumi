package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow
import xyz.ksharma.sumi.theme.SumiSeason

// Cohesive observe/set pairs for theme + gameplay-display preferences; splitting into
// micro-interfaces per setting would add indirection without value (the impl is one class).
@Suppress("TooManyFunctions")
interface ThemePreferences {
    fun observeSeason(): Flow<SumiSeason>
    suspend fun setSeason(season: SumiSeason)
    fun observeHapticsEnabled(): Flow<Boolean>
    suspend fun setHapticsEnabled(enabled: Boolean)

    /**
     * Whether the running mistake count is shown on the Game screen.
     * Default true. Some players prefer to play without seeing the count
     * (less anxiety-inducing); they can toggle it off in Settings.
     */
    fun observeShowMistakes(): Flow<Boolean>
    suspend fun setShowMistakes(enabled: Boolean)

    /**
     * Whether board digits use a high-legibility sans typeface instead of the
     * Cormorant serif numerals. Default false. Helps players who find the serif
     * 1/4/7 ambiguous, and improves readability at a glance.
     */
    fun observeHighLegibility(): Flow<Boolean>
    suspend fun setHighLegibility(enabled: Boolean)

    /**
     * When true, the board highlights only actual rule conflicts (a digit duplicated in a
     * row/column/box) instead of every cell that differs from the solution. Default false —
     * i.e. the board reveals wrong digits. Strict mode lets players guess without spoilers.
     */
    fun observeStrictConflicts(): Flow<Boolean>
    suspend fun setStrictConflicts(enabled: Boolean)

    /**
     * Input mode. Default false = cell-first (tap a cell, then a number). True = digit-first
     * (tap a number to arm it, then tap cells to place it) — faster for filling many of one digit.
     */
    fun observeDigitFirstInput(): Flow<Boolean>
    suspend fun setDigitFirstInput(enabled: Boolean)

    /**
     * Lives mode. Default false. When on, the puzzle ends after a fixed number of mistakes
     * (a more competitive challenge), surfacing a game-over screen instead of running forever.
     */
    fun observeLivesMode(): Flow<Boolean>
    suspend fun setLivesMode(enabled: Boolean)
}
