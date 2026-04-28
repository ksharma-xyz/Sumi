package xyz.ksharma.sumi.screens.zen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import xyz.ksharma.sumi.PRO_QUOTES
import xyz.ksharma.sumi.Quote
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.game.puzzle.PuzzleRepository
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.share.BookInk
import xyz.ksharma.sumi.share.BookPaper
import xyz.ksharma.sumi.share.BookTheme
import xyz.ksharma.sumi.share.PuzzleBookExporter
import xyz.ksharma.sumi.share.PuzzleBookPage
import xyz.ksharma.sumi.share.PuzzleBookSpec
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

sealed interface BookGenState {
    data object Idle : BookGenState
    data object Generating : BookGenState
    data object Ready : BookGenState
    data class Error(val message: String) : BookGenState
}

class ZenViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val exporter: PuzzleBookExporter,
) : ViewModel() {

    private val _quoteIndex = MutableStateFlow(0)
    val quoteIndex: StateFlow<Int> = _quoteIndex.asStateFlow()

    private val _bookDifficultyMix = MutableStateFlow(DifficultyMix())
    val bookDifficultyMix: StateFlow<DifficultyMix> = _bookDifficultyMix.asStateFlow()

    private val _bookTheme = MutableStateFlow(BookTheme())
    val bookTheme: StateFlow<BookTheme> = _bookTheme.asStateFlow()

    private val _bookIncludeAnswers = MutableStateFlow(false)
    val bookIncludeAnswers: StateFlow<Boolean> = _bookIncludeAnswers.asStateFlow()

    private val _bookGenState = MutableStateFlow<BookGenState>(BookGenState.Idle)
    val bookGenState: StateFlow<BookGenState> = _bookGenState.asStateFlow()

    val quotes: List<Quote> = PRO_QUOTES

    private var generatedBytes: ByteArray? = null

    fun setQuoteIndex(index: Int) {
        _quoteIndex.value = index.coerceIn(0, quotes.size - 1)
    }

    fun setBookDifficultyMix(mix: DifficultyMix) { _bookDifficultyMix.value = mix }
    fun setBookPaper(p: BookPaper) { _bookTheme.value = _bookTheme.value.copy(paper = p) }
    fun setBookInk(i: BookInk) { _bookTheme.value = _bookTheme.value.copy(ink = i) }
    fun setBookIncludeAnswers(include: Boolean) { _bookIncludeAnswers.value = include }

    @OptIn(ExperimentalTime::class, ExperimentalResourceApi::class)
    fun generateBook() {
        if (_bookGenState.value is BookGenState.Generating) return
        generatedBytes = null
        viewModelScope.launch {
            val startTime = Clock.System.now().toEpochMilliseconds()
            _bookGenState.value = BookGenState.Generating
            val mix = _bookDifficultyMix.value
            val theme = _bookTheme.value
            val includeAnswers = _bookIncludeAnswers.value
            val paperPng = runCatching {
                val path = when (theme.paper) {
                    BookPaper.Light -> "drawable/washi_paper_light.png"
                    BookPaper.Dark -> "drawable/washi_paper_dark.png"
                }
                Res.readBytes(path)
            }.getOrNull()
            val inkBleedPng = runCatching { Res.readBytes("drawable/ink_bleed_01.png") }.getOrNull()
            val pages = buildPages(mix, startTime)
            val spec = PuzzleBookSpec(
                pages = pages,
                theme = theme,
                appName = "Sumi",
                includeAnswers = includeAnswers,
                paperPng = paperPng,
                inkBleedPng = inkBleedPng,
            )
            val result = exporter.generate(spec)
            // Hold the crafting view for a minimum dwell time so the transition reads as intentional.
            val elapsed = Clock.System.now().toEpochMilliseconds() - startTime
            if (elapsed < MIN_CRAFTING_MS) delay(MIN_CRAFTING_MS - elapsed)
            if (result.isSuccess) {
                generatedBytes = result.getOrNull()
                _bookGenState.value = BookGenState.Ready
            } else {
                _bookGenState.value = BookGenState.Error(result.exceptionOrNull()?.message ?: "Export failed")
            }
        }
    }

    private fun buildPages(mix: DifficultyMix, baseSeed: Long): List<PuzzleBookPage> {
        val diffEntries = listOf(
            Difficulty.Easy to mix.easy,
            Difficulty.Medium to mix.medium,
            Difficulty.Hard to mix.hard,
            Difficulty.Master to mix.master,
            Difficulty.Edo to mix.edo,
        ).filter { (_, n) -> n > 0 }
        var globalIndex = 0
        return buildList {
            diffEntries.forEach { (difficulty, count) ->
                repeat(count) {
                    val puzzle = puzzleRepository.fromSeed(difficulty, baseSeed + globalIndex * SEED_STEP)
                    val clues = puzzle.cells.flatten().map { cell -> if (cell.given) cell.value else 0 }
                    val solution = puzzle.solution.flatten()
                    val q = quotes[globalIndex % quotes.size]
                    add(
                        PuzzleBookPage(
                            clues = clues,
                            solution = solution,
                            pageNumber = globalIndex + 1,
                            difficulty = difficulty.name,
                            quote = q.text,
                            quoteAuthor = q.attribution,
                        ),
                    )
                    globalIndex++
                }
            }
        }
    }

    fun shareBook() {
        val bytes = generatedBytes ?: return
        viewModelScope.launch {
            val result = exporter.share(bytes, "Sumi Puzzles")
            if (result.isFailure) {
                _bookGenState.value = BookGenState.Error(result.exceptionOrNull()?.message ?: "Share failed")
            }
        }
    }

    fun clearError() {
        _bookGenState.value = BookGenState.Idle
    }

    private companion object {
        const val SEED_STEP = 10_000L
        const val MIN_CRAFTING_MS = 2500L
    }
}
