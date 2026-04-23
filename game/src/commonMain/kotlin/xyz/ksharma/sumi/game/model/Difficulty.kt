package xyz.ksharma.sumi.game.model

enum class Difficulty(
    val givenCount: Int,
    val label: String,
) {
    Easy(givenCount = 40, label = "Easy"),
    Medium(givenCount = 32, label = "Medium"),
    Hard(givenCount = 28, label = "Hard"),
    Master(givenCount = 24, label = "Master"),
    Edo(givenCount = 22, label = "Edo"),
}
