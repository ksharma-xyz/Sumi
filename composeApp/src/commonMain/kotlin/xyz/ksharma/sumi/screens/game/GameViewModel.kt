// Each callback maps 1:1 to a Game UI input — splitting hurts clarity.
@file:Suppress("TooManyFunctions")

package xyz.ksharma.sumi.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import xyz.ksharma.sumi.ads.AdOrchestrator
import xyz.ksharma.sumi.game.manager.BoardManager
import xyz.ksharma.sumi.game.manager.RealBoardManager
import xyz.ksharma.sumi.game.model.BoardState
import xyz.ksharma.sumi.game.model.Cell
import xyz.ksharma.sumi.game.model.Difficulty
import xyz.ksharma.sumi.game.puzzle.PuzzleRepository
import xyz.ksharma.sumi.preferences.GameSave
import xyz.ksharma.sumi.preferences.GameSaveRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class GameViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val saveRepository: GameSaveRepository,
    private val adOrchestrator: AdOrchestrator,
) : ViewModel() {

    private val _state = MutableStateFlow(EMPTY_BOARD)
    val state: StateFlow<BoardState> = _state.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private val _celebrationCount = MutableStateFlow(0)
    val celebrationCount: StateFlow<Int> = _celebrationCount.asStateFlow()

    /**
     * Fires `true` when the player has been idle for [IDLE_INTERSTITIAL_MS] while the puzzle
     * is unsolved and unpaused, AND the [AdOrchestrator] permits another interstitial.
     * The Game entry observes this and renders the basic-ads InterstitialAd composable;
     * call [onIdleInterstitialDone] when the ad finishes (or fails to load).
     */
    private val _showIdleInterstitial = MutableStateFlow(false)
    val showIdleInterstitial: StateFlow<Boolean> = _showIdleInterstitial.asStateFlow()

    /**
     * Fires `true` when the player taps the Hint button while [BoardState.hintsRemaining]
     * is 0 — the Game entry then renders a basic-ads RewardedAd. On reward, call
     * [grantHintsFromAd]; on dismiss/failure, call [onRewardedHintAdDone].
     *
     * Not gated by AdOrchestrator — rewarded ads are opt-in (the user chose to watch),
     * so the interstitial frequency cap does not apply.
     */
    private val _showRewardedHintAd = MutableStateFlow(false)
    val showRewardedHintAd: StateFlow<Boolean> = _showRewardedHintAd.asStateFlow()

    private var boardManager: BoardManager = RealBoardManager(EMPTY_BOARD)
    private var currentDifficulty: Difficulty = Difficulty.Medium
    private var currentSeed: Long = 0L
    private var timerJob: Job? = null
    private var syncJob: Job? = null
    private var idleMs: Long = 0L
    private var paused: Boolean = false

    @OptIn(ExperimentalTime::class)
    fun init(difficulty: Difficulty, fresh: Boolean = false, proHints: Boolean = false) {
        timerJob?.cancel()
        syncJob?.cancel()
        currentDifficulty = difficulty
        _elapsedMs.value = 0L
        _celebrationCount.value = 0
        idleMs = 0L
        _showIdleInterstitial.value = false
        _showRewardedHintAd.value = false

        viewModelScope.launch {
            if (fresh) saveRepository.clearSave(difficulty)

            val freshPuzzle: BoardState
            val save: GameSave?
            if (fresh) {
                // Random seed so "New Puzzle" always gives different clues from the daily puzzle.
                currentSeed = Clock.System.now().toEpochMilliseconds()
                freshPuzzle = puzzleRepository.fromSeed(difficulty, currentSeed)
                save = null
            } else {
                val loadedSave = saveRepository.loadSave(difficulty)
                if (loadedSave != null && loadedSave.puzzleSeed != 0L) {
                    currentSeed = loadedSave.puzzleSeed
                    freshPuzzle = puzzleRepository.fromSeed(difficulty, currentSeed)
                } else {
                    currentSeed = 0L
                    freshPuzzle = puzzleRepository.daily(difficulty)
                }
                save = loadedSave
            }

            val restoredState = if (save != null) restoreState(freshPuzzle, save) else freshPuzzle
            val restoredElapsed = save?.elapsedMs ?: 0L

            val hintCount = if (proHints) BoardState.HINTS_UNLIMITED
            else minOf(restoredState.hintsRemaining, BoardState.DEFAULT_HINTS)
            val stateWithHints = restoredState.copy(hintsRemaining = hintCount)

            boardManager = RealBoardManager(stateWithHints)
            val resolved = boardManager.state.value
            if (resolved.isComplete) {
                // Stale save from a completed game (process was killed before clear ran) — start fresh.
                saveRepository.clearSave(difficulty)
                val freshWithHints = freshPuzzle.copy(hintsRemaining = hintCount)
                boardManager = RealBoardManager(freshWithHints)
                _elapsedMs.value = 0L
            } else {
                _elapsedMs.value = restoredElapsed
            }

            _state.value = boardManager.state.value
            startSync()
            startTimer()
        }
    }

    fun select(row: Int, col: Int) {
        resetIdle()
        boardManager.select(row, col)
    }
    fun enter(digit: Int) {
        resetIdle()
        boardManager.enter(digit)
    }
    fun erase() {
        resetIdle()
        boardManager.erase()
    }
    fun undo() {
        resetIdle()
        boardManager.undo()
    }
    fun hint() {
        resetIdle()
        boardManager.hint()
    }
    fun toggleNotes() {
        resetIdle()
        boardManager.toggleNotes()
    }

    /** Called by GameEntry when the user pauses or resumes the game. */
    fun setPaused(value: Boolean) {
        paused = value
        resetIdle()
    }

    /** Idle interstitial finished (dismissed or failed to load) — resume normal play. */
    fun onIdleInterstitialDone() {
        if (_showIdleInterstitial.value) adOrchestrator.onInterstitialShown()
        _showIdleInterstitial.value = false
        resetIdle()
    }

    /** Called by the Game entry when the user taps Hint while hintsRemaining == 0. */
    fun requestRewardedHintAd() {
        if (_state.value.hintsRemaining > 0) return // already has a hint, no ad needed
        _showRewardedHintAd.value = true
    }

    /** Called by basic-ads onRewardEarned — grant the hint the user just earned. */
    fun grantHintsFromAd(count: Int = 1) {
        boardManager.addHints(count)
        // Don't auto-consume — user chose to earn it; let them spend it when they want.
    }

    /** Rewarded ad dismissed (with or without reward) or failed to load. */
    fun onRewardedHintAdDone() {
        _showRewardedHintAd.value = false
        resetIdle()
    }

    private fun resetIdle() {
        idleMs = 0L
    }

    private fun startSync() {
        syncJob = viewModelScope.launch {
            var prevCompleted = completionKey(_state.value)
            boardManager.state.collect { newState ->
                _state.value = newState
                val newCompleted = completionKey(newState)
                if ((newCompleted - prevCompleted).isNotEmpty()) {
                    _celebrationCount.update { it + 1 }
                }
                prevCompleted = newCompleted

                when {
                    newState.isComplete ->
                        // Puzzle solved — clear the save slot so there's nothing stale to resume.
                        launch { saveRepository.clearSave(currentDifficulty) }
                    hasAnyUserMoves(newState) ->
                        // Auto-save after every user move so the game survives process death.
                        launch { saveRepository.writeSave(currentDifficulty, buildSave(newState)) }
                }
            }
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(TIMER_TICK_MS)
                val current = _state.value
                if (current.isComplete) continue
                _elapsedMs.value += TIMER_TICK_MS

                // Idle interstitial: only count idle time when the player can actually act
                // — game in progress, not paused, no ad currently showing.
                if (!paused && !_showIdleInterstitial.value) {
                    idleMs += TIMER_TICK_MS
                    if (idleMs >= IDLE_INTERSTITIAL_MS && adOrchestrator.mayShowInterstitial()) {
                        idleMs = 0L
                        _showIdleInterstitial.value = true
                    }
                }
            }
        }
    }

    private fun buildSave(state: BoardState): GameSave {
        val cells = state.cells
            .flatMap { row -> row.map { cell -> if (cell.given) 0 else cell.value } }
            .joinToString(",")
        return GameSave(
            epochDay = todayEpochDay(),
            cells = cells,
            elapsedMs = _elapsedMs.value,
            mistakeCount = state.mistakeCount,
            moveCount = state.moveCount,
            hintsRemaining = state.hintsRemaining,
            puzzleSeed = currentSeed,
        )
    }

    private fun hasAnyUserMoves(state: BoardState): Boolean =
        state.cells.any { row -> row.any { cell -> !cell.given && cell.value != 0 } }

    // Reconstructs a BoardState from a fresh puzzle and a persisted save.
    // Given cells are always taken from the fresh puzzle (same seed = same puzzle).
    // Non-given cells use the saved user value (0 = empty).
    private fun restoreState(fresh: BoardState, save: GameSave): BoardState {
        val savedValues = save.cells.split(",").map { it.trim().toIntOrNull() ?: 0 }
        val restoredCells = fresh.cells.mapIndexed { r, row ->
            row.mapIndexed { c, cell ->
                if (cell.given) cell
                else cell.copy(value = savedValues.getOrElse(r * BOARD_SIZE + c) { 0 })
            }
        }
        return fresh.copy(
            cells = restoredCells,
            mistakeCount = save.mistakeCount,
            moveCount = save.moveCount,
            hintsRemaining = save.hintsRemaining,
            selected = null,
            notesMode = false,
        )
    }

    private fun completionKey(state: BoardState): Set<String> = buildSet {
        state.completedRows.forEach { add("row$it") }
        state.completedCols.forEach { add("col$it") }
        state.completedBoxes.forEach { add("box$it") }
        if (state.isComplete) add("grid")
    }

    @OptIn(ExperimentalTime::class)
    private fun todayEpochDay(): Long {
        val now = Clock.System.now()
        val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return local.date.toEpochDays()
    }

    private companion object {
        const val TIMER_TICK_MS = 1000L
        // Show an interstitial after 90s of zero input on the Game screen — only when the
        // orchestrator's frequency cap allows. Reset on every user action.
        const val IDLE_INTERSTITIAL_MS = 90_000L
        const val BOARD_SIZE = 9
        // solution uses 1 (not 0) so isComplete = (0 == 1) = false before init() fires.
        // A solution of all-zeros would make isComplete immediately true and trigger Win navigation.
        val EMPTY_BOARD = BoardState(
            cells = List(9) { List(9) { Cell(0, false) } },
            difficulty = Difficulty.Medium,
            solution = List(9) { List(9) { 1 } },
        )
    }
}
