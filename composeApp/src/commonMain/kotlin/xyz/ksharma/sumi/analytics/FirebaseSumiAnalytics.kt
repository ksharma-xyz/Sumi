package xyz.ksharma.sumi.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.analytics.logEvent

class FirebaseSumiAnalytics : SumiAnalytics {

    override fun logGameStarted(difficulty: String) {
        Firebase.analytics.logEvent("game_started") {
            param("difficulty", difficulty)
        }
    }

    override fun logGameCompleted(difficulty: String, elapsedSeconds: Long, mistakes: Int) {
        Firebase.analytics.logEvent("game_completed") {
            param("difficulty", difficulty)
            param("elapsed_seconds", elapsedSeconds)
            param("mistakes", mistakes.toLong())
        }
    }

    override fun logGameOver(difficulty: String) {
        Firebase.analytics.logEvent("game_over") {
            param("difficulty", difficulty)
        }
    }

    override fun logHintUsed(difficulty: String) {
        Firebase.analytics.logEvent("hint_used") {
            param("difficulty", difficulty)
        }
    }

    override fun logOnboardingCompleted(season: String) {
        Firebase.analytics.logEvent("onboarding_completed") {
            param("season", season)
        }
    }
}
