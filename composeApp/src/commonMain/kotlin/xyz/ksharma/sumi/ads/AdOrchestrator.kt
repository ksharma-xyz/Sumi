package xyz.ksharma.sumi.ads

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Single source of truth for "may we show an interstitial right now?".
 *
 * - **90s frequency cap** shared across every interstitial source (Win, idle on Game, …).
 *   Two interstitials within 90s of each other will never both fire.
 * - **3 / day max** so an extended session can't be carpet-bombed with full-screen ads.
 * - Caller is responsible for the upstream `!isPro && isAdsEnabled` check; the orchestrator
 *   is purely about cadence.
 *
 * Held as a Koin `single` so the cap is honoured across screens within a session. Day-rollover
 * resets are computed at call time using the system clock — no observer needed.
 */
@OptIn(ExperimentalTime::class)
class AdOrchestrator {

    private var lastInterstitialEpochMs: Long = 0L
    private var todayShownCount: Int = 0
    private var todayEpochDay: Long = 0L

    fun mayShowInterstitial(): Boolean {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        rolloverIfNewDay(nowMs)
        if (todayShownCount >= MAX_PER_DAY) return false
        if (nowMs - lastInterstitialEpochMs < MIN_GAP_MS) return false
        return true
    }

    fun onInterstitialShown() {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        rolloverIfNewDay(nowMs)
        lastInterstitialEpochMs = nowMs
        todayShownCount += 1
    }

    private fun rolloverIfNewDay(nowMs: Long) {
        val nowDay = nowMs / MS_PER_DAY
        if (nowDay != todayEpochDay) {
            todayEpochDay = nowDay
            todayShownCount = 0
        }
    }

    private companion object {
        const val MIN_GAP_MS = 90_000L // 90 seconds between any two interstitials
        const val MAX_PER_DAY = 3
        const val MS_PER_DAY = 24L * 60L * 60L * 1000L
    }
}
