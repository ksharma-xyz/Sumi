package xyz.ksharma.sumi.design.board

/**
 * How the board's digits appear when a puzzle first becomes visible.
 *
 * Debug-only selector for now — we're trialling a couple of entrances and will
 * promote the winner to the default. Persisted by [BoardEntrance.name].
 */
enum class BoardEntrance(val label: String) {
    /** No reveal — digits use the existing per-cell fade (current behaviour). */
    None(label = "None"),

    /**
     * Ink-bloom: givens wash in like ink soaking washi, staggered along the
     * top-left -> bottom-right diagonal. Each cell blooms from small + faint to
     * full size + opacity.
     */
    InkBloom(label = "Ink bloom"),
    ;

    companion object {
        // Trial default: InkBloom so the entrance is visible without toggling the debug
        // setting. Flip back to None before release if we decide not to ship it.
        val DEFAULT = InkBloom

        /** Lenient parse for persisted values — unknown / legacy strings fall back to [DEFAULT]. */
        fun fromName(name: String?): BoardEntrance =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
