package com.sumi.data

/**
 * SUMI — Quote library.
 * Cycle deterministically by day-of-year. Mix attributed classics with "House"
 * (original) lines. Keep the tone quiet, contemplative, craft-focused.
 *
 * Short anthology (13) for Free tier. Extend to 600 for Pro ("The Library").
 */
data class SumiQuote(val q: String, val a: String)

val SumiQuotes: List<SumiQuote> = listOf(
    SumiQuote("A line drawn once, with certainty, is worth a thousand redrawn.", "Unsui, 1812"),
    SumiQuote("The quieter you become, the more you can hear.", "Ram Dass"),
    SumiQuote("Nothing in excess.", "Delphic Maxim"),
    SumiQuote("The master begins where the student finishes.", "—"),
    SumiQuote("What is essential is invisible to the eye.", "Saint-Exupéry"),
    SumiQuote("Nine lines. Nine columns. One patience.", "House"),
    SumiQuote("Empty space is not empty. It is waiting.", "House"),
    SumiQuote("Make haste slowly.", "Augustus"),
    SumiQuote("The brush lifts; the mind descends.", "House"),
    SumiQuote("Do the difficult things while they are easy.", "Lao Tzu"),
    SumiQuote("Simplicity is the ultimate sophistication.", "da Vinci"),
    SumiQuote("A puzzle is a conversation with its maker.", "House"),
    SumiQuote("Mistakes are the ink the solution is written in.", "House"),
)

fun quoteForDayOfYear(day: Int): SumiQuote =
    SumiQuotes[((day % SumiQuotes.size) + SumiQuotes.size) % SumiQuotes.size]
