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

/** Full quote library shown to Pro members in the Zen tab. Expands to 600 passages post-launch. */
val PRO_QUOTES: List<Quote> = listOf(
    Quote("A line drawn once, with certainty, is worth a thousand redrawn.", "Unsui, 1812"),
    Quote("Nine lines. Nine columns. One patience.", "House"),
    Quote("Empty space is not empty. It is waiting.", "House"),
    Quote("The brush lifts; the mind descends.", "House"),
    Quote("A puzzle is a conversation with its maker.", "House"),
    Quote("Mistakes are the ink the solution is written in.", "House"),
    Quote("The quieter you become, the more you can hear.", "Ram Dass"),
    Quote("Nothing in excess.", "Delphic Maxim"),
    Quote("The master begins where the student finishes.", "—"),
    Quote("What is essential is invisible to the eye.", "Saint-Exupéry"),
    Quote("Make haste slowly.", "Augustus"),
    Quote("Do the difficult things while they are easy.", "Lao Tzu"),
    Quote("Simplicity is the ultimate sophistication.", "da Vinci"),
    Quote("Out of clutter, find simplicity.", "Albert Einstein"),
    Quote("Patience is bitter, but its fruit is sweet.", "Aristotle"),
    Quote("A jug fills drop by drop.", "Buddha"),
    Quote("The journey of a thousand miles begins with one step.", "Lao Tzu"),
    Quote("Before enlightenment, chop wood, carry water.", "Zen Proverb"),
    Quote("In the middle of difficulty lies opportunity.", "Albert Einstein"),
    Quote("An unexamined life is not worth living.", "Socrates"),
)
