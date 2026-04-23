@file:Suppress("MatchingDeclarationName")

package xyz.ksharma.sumi

data class Quote(val text: String, val attribution: String)

val FREE_QUOTES: List<Quote> = listOf(
    Quote("Simplicity is the ultimate sophistication.", "Leonardo da Vinci"),
    Quote("The quieter you become, the more you can hear.", "Ram Dass"),
    Quote("To know what you know and what you do not know, that is true knowledge.", "Confucius"),
    Quote("Do not be afraid to give up the good to go for the great.", "John D. Rockefeller"),
    Quote("Out of clutter, find simplicity.", "Albert Einstein"),
    Quote("The best time to plant a tree was twenty years ago.", "Chinese Proverb"),
    Quote("Patience is bitter, but its fruit is sweet.", "Aristotle"),
    Quote("A jug fills drop by drop.", "Buddha"),
    Quote("The journey of a thousand miles begins with one step.", "Lao Tzu"),
    Quote("Tension is who you think you should be. Relaxation is who you are.", "Chinese Proverb"),
    Quote("Before enlightenment, chop wood, carry water.", "Zen Proverb"),
    Quote("In the middle of difficulty lies opportunity.", "Albert Einstein"),
    Quote("An unexamined life is not worth living.", "Socrates"),
)
