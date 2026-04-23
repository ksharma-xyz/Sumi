package xyz.ksharma.sumi.design.board

sealed class BoardSweep {
    data class Row(val index: Int) : BoardSweep()
    data class Col(val index: Int) : BoardSweep()
    data class Box(val index: Int) : BoardSweep()
    data object Win : BoardSweep()
}

enum class AuroraTone { Paper, Night }
