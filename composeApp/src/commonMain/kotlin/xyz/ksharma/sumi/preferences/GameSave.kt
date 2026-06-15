package xyz.ksharma.sumi.preferences

/** Serialised snapshot of an in-progress game for one difficulty slot. */
data class GameSave(
    val epochDay: Long,
    val cells: String, // 81 comma-separated ints; 0=empty or given, 1-9=user-placed
    val elapsedMs: Long,
    val mistakeCount: Int,
    val moveCount: Int = 0,
    val hintsRemaining: Int,
    val puzzleSeed: Long = 0L, // 0 = today's daily seed; non-zero = custom seed from fromSeed()
    // 81 cells separated by ';'; each cell is its pencil-mark digits concatenated
    // (e.g. "13" = notes 1 and 3), empty string = no notes. Defaults to "" so old
    // saves written before notes persistence load cleanly.
    val notes: String = "",
)
