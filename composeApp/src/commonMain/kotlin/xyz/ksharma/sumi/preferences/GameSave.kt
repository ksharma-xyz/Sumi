package xyz.ksharma.sumi.preferences

/** Serialised snapshot of an in-progress game for one difficulty slot. */
data class GameSave(
    val epochDay: Long,
    val cells: String, // 81 comma-separated ints; 0=empty or given, 1-9=user-placed
    val elapsedMs: Long,
    val mistakeCount: Int,
    val hintsRemaining: Int,
)
