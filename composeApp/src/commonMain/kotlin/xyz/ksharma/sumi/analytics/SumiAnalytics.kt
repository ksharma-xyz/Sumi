package xyz.ksharma.sumi.analytics

interface SumiAnalytics {
    fun logGameStarted(difficulty: String)
    fun logGameCompleted(difficulty: String, elapsedSeconds: Long, mistakes: Int)
    fun logHintUsed(difficulty: String)
    fun logOnboardingCompleted(season: String)
}
