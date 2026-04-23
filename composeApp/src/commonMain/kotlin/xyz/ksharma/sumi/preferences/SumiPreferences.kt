package xyz.ksharma.sumi.preferences

interface SumiPreferences {
    suspend fun hasSeenOnboarding(): Boolean
    suspend fun setSeenOnboarding()

    suspend fun getStreak(): Int

    // Records today as a solve day. Returns updated streak.
    suspend fun recordSolve(): Int

    // Returns a set of epoch-day longs for days that had at least one solve.
    suspend fun getSolveDays(): Set<Long>
}
