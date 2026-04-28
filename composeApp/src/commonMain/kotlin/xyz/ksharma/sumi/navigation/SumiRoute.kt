package xyz.ksharma.sumi.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface SumiRoute : NavKey

// ── Setup flow ────────────────────────────────────────────────────────────────
@Serializable data object SplashRoute : SumiRoute
@Serializable data object OnboardingRoute : SumiRoute

// ── Bottom nav roots (top-level) ──────────────────────────────────────────────
@Serializable data object HomeRoute : SumiRoute
@Serializable data object DailyRoute : SumiRoute
@Serializable data object StatsRoute : SumiRoute
@Serializable data object ZenRoute : SumiRoute

// ── Pushable screens (not tabs) ────────────────────────────────────────────────
@Serializable data object PaywallRoute : SumiRoute

// ── Game flow ─────────────────────────────────────────────────────────────────
@Serializable
data class GameRoute(
    val difficulty: String = "Easy",
    val puzzleId: Long = 0L,
) : SumiRoute

@Serializable data class WinRoute(
    val elapsedMs: Long = 0L,
    val mistakeCount: Int = 0,
    val difficulty: String = "Easy",
) : SumiRoute

// ── Utility ───────────────────────────────────────────────────────────────────
@Serializable data object SettingsRoute : SumiRoute
@Serializable data object LicensesRoute : SumiRoute
