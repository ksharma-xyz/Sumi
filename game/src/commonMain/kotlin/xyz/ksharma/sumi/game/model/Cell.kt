package xyz.ksharma.sumi.game.model

data class Cell(
    val value: Int,
    val given: Boolean,
    val notes: Set<Int> = emptySet(),
) {
    val isEmpty: Boolean get() = value == 0
}
