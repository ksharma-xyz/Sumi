// Each callback maps 1:1 to a Game UI input — splitting hurts clarity.
@file:Suppress("TooManyFunctions")

package xyz.ksharma.sumi.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
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
import xyz.ksharma.sumi.coroutines.ext.launchWithExceptionHandler
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

/** Mistakes allowed before a game ends in lives mode. */
internal const val MAX_LIVES = 3

class GameViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val saveRepository: GameSaveRepository,
    private val adOrchestrator: AdOrchestrator,
) : ViewModel() {

    private val _state = MutableStateFlow(EMPTY_BOARD)
    val state: StateFlow<BoardState> = _state.asStateFlow()

    /**
     * The digit armed in digit-first input mode (pick a number, then tap cells to place it),
     * or null when no digit is armed. The number pad highlights it; cell taps place it.
     */
    private val _selectedDigit = MutableStateFlow<Int?>(null)
    val selectedDigit: StateFlow<Int?> = _selectedDigit.asStateFlow()

    /** Fires `true` in lives mode once mistakes reach [MAX_LIVES]. The Game entry navigates to game-over. */
    private val _gameOver = MutableStateFlow(false)
    val gameOver: StateFlow<Boolean> = _gameOver.asStateFlow()

    // Whether the mistake-limit (lives) rule is active for this game. Read live in startSync so a
    // mid-game Settings toggle takes effect, set from the Game entry via setLivesEnabled.
    private var livesEnabled = false

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private val _celebrationCount = MutableStateFlow(0)
    val celebrationCount: StateFlow<Int> = _celebrationCount.asStateFlow()

    /**
     * Bumps when the *whole* grid is solved (separate from per-row / per-box
     * cell-completion ticks above). The Game screen reads this to fire a
     * larger, longer petal shower than the subtle line-completion bursts.
     */
    private val _gridCelebrationCount = MutableStateFlow(0)
    val gridCelebrationCount: StateFlow<Int> = _gridCelebrationCount.asStateFlow()

    /**
     * True while [init] is still loading / generating the puzzle. The Game
     * screen reads this to render a premium loading overlay (the puzzle gen
     * for Edo can take several seconds on lower-end devices). Resumes are
     * fast — the overlay only appears after a 200ms delay so it doesn't
     * flash on quick loads.
     */
    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

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
        _gridCelebrationCount.value = 0
        _isInitializing.value = true
        idleMs = 0L
        _showIdleInterstitial.value = false
        _showRewardedHintAd.value = false
        _gameOver.value = false
        // Reset pause — the ViewModel is reused across games, so a leftover paused=true
        // (e.g. from auto-pause on background) would otherwise freeze the new game's timer.
        paused = false

        // launchWithExceptionHandler so any failure in the generator / save IO
        // doesn't crash the app. Heavy work (puzzle generation — backtracking
        // sudoku solver — and DataStore IO) runs on Dispatchers.Default to keep
        // the main thread free for the loading-state UI.
        viewModelScope.launchWithExceptionHandler<GameViewModel>(dispatcher = Dispatchers.Default) {
            try {
                if (fresh) saveRepository.clearSave(difficulty)

                val freshPuzzle: BoardState
                val save: GameSave?
                // We're already on Dispatchers.Default thanks to the outer
                // launchWithExceptionHandler, so the generator + DataStore calls
                // below run off-main without further withContext gymnastics.
                if (fresh) {
                    // Random seed so "New Puzzle" always gives different clues from the daily puzzle.
                    currentSeed = Clock.System.now().toEpochMilliseconds()
                    freshPuzzle = puzzleRepository.fromSeed(difficulty, currentSeed)
                    save = null
                } else {
                    val loadedSave = saveRepository.loadSave(difficulty)
                    // Legacy saves (seed 0) and the no-save case fall back to today's
                    // daily seed. buildSave persists this concrete seed, so the game can
                    // be rebuilt with fromSeed and resumes on any later day instead of
                    // being replaced by a freshly-rolled daily puzzle.
                    currentSeed = loadedSave?.puzzleSeed?.takeIf { it != 0L } ?: puzzleRepository.dailySeed()
                    freshPuzzle = puzzleRepository.fromSeed(difficulty, currentSeed)
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
                // Persist the seed immediately when starting a fresh puzzle, even with
                // zero user moves. Without this, "New Puzzle" → navigate away → return
                // would re-open the daily puzzle (no save existed yet) instead of the
                // freshly-rolled one. startSync's auto-save only fires once the user
                // makes a move, so we seed the save up front.
                if (fresh) saveRepository.writeSave(currentDifficulty, buildSave(_state.value))
                startSync()
                startTimer()
            } finally {
                // Always clear the loading flag — the Game screen now hides the
                // board entirely while this is true, so a generation failure must
                // not strand the user on a permanent loading overlay.
                _isInitializing.value = false
            }
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

    /** Enable/disable the mistake-limit rule for the current game (driven by the Settings toggle). */
    fun setLivesEnabled(enabled: Boolean) {
        livesEnabled = enabled
    }

    /** Digit-first: arm a digit (or disarm it if it's already armed). */
    fun selectDigit(digit: Int) {
        resetIdle()
        _selectedDigit.value = if (_selectedDigit.value == digit) null else digit
    }

    /** Digit-first: a cell was tapped — place the armed digit, or just select the cell. */
    fun placeOrSelect(row: Int, col: Int) {
        resetIdle()
        val digit = _selectedDigit.value
        if (digit != null) boardManager.placeAt(row, col, digit) else boardManager.select(row, col)
    }
    fun erase() {
        resetIdle()
        boardManager.erase()
    }
    fun undo() {
        resetIdle()
        boardManager.undo()
    }
    fun redo() {
        resetIdle()
        boardManager.redo()
    }
    fun hint() {
        resetIdle()
        boardManager.hint()
    }
    fun toggleNotes() {
        resetIdle()
        boardManager.toggleNotes()
    }
    fun fillNotes() {
        resetIdle()
        boardManager.fillNotes()
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
            var prevIsComplete = _state.value.isComplete
            boardManager.state.collect { newState ->
                _state.value = newState
                val newCompleted = completionKey(newState)
                val justCompletedGrid = !prevIsComplete && newState.isComplete
                val newLineEvents = (newCompleted - prevCompleted).filterNot { it == "grid" }
                if (newLineEvents.isNotEmpty() && !justCompletedGrid) {
                    _celebrationCount.update { it + 1 }
                }
                if (justCompletedGrid) {
                    _gridCelebrationCount.update { it + 1 }
                }
                prevCompleted = newCompleted
                prevIsComplete = newState.isComplete

                if (newState.isComplete) {
                    // Puzzle solved — clear the save slot so there's nothing stale to resume.
                    launch { saveRepository.clearSave(currentDifficulty) }
                } else if (livesEnabled && newState.mistakeCount >= MAX_LIVES) {
                    // Out of lives. Clear the save FIRST (awaited, so reopening the difficulty
                    // starts fresh instead of bouncing straight back to game-over), then signal.
                    if (!_gameOver.value) {
                        saveRepository.clearSave(currentDifficulty)
                        _gameOver.value = true
                    }
                } else {
                    // Save on EVERY state change — including ones where the user has
                    // no moves entered (undo / erase clearing the board). The previous
                    // hasAnyUserMoves gate caused undo to leave the OLD save in place,
                    // so re-opening the game brought back the digits the user had just
                    // erased. Save the current state unconditionally; puzzleSeed is
                    // preserved so resume always lands on the same puzzle.
                    launch { saveRepository.writeSave(currentDifficulty, buildSave(newState)) }
                }
            }
        }
    }

    // Harder boards (fewer givens) earn a longer idle window so deep thinking
    // isn't broken by an interstitial. See IDLE_INTERSTITIAL_* constants.
    private fun idleInterstitialThresholdMs(): Long = when (currentDifficulty) {
        Difficulty.Hard, Difficulty.Master, Difficulty.Edo -> IDLE_INTERSTITIAL_HARD_MS
        Difficulty.Easy, Difficulty.Medium -> IDLE_INTERSTITIAL_MS
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(TIMER_TICK_MS)
                val current = _state.value
                // Tick only while the puzzle is active and not paused (manual or auto-pause
                // on background), so neither completion nor time away inflates the solve time.
                if (current.isComplete || paused) continue
                _elapsedMs.value += TIMER_TICK_MS

                // Idle interstitial: only count idle time when the player can actually act
                // — game in progress, not paused, no ad currently showing.
                if (!_showIdleInterstitial.value) {
                    idleMs += TIMER_TICK_MS
                    if (idleMs >= idleInterstitialThresholdMs() && adOrchestrator.mayShowInterstitial()) {
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
        val notes = state.cells
            .flatMap { row ->
                row.map { cell -> if (cell.given) "" else cell.notes.sorted().joinToString("") }
            }
            .joinToString(";")
        return GameSave(
            epochDay = todayEpochDay(),
            cells = cells,
            elapsedMs = _elapsedMs.value,
            mistakeCount = state.mistakeCount,
            moveCount = state.moveCount,
            hintsRemaining = state.hintsRemaining,
            puzzleSeed = currentSeed,
            notes = notes,
            selectedRow = state.selected?.first ?: -1,
            selectedCol = state.selected?.second ?: -1,
        )
    }

    // Reconstructs a BoardState from a fresh puzzle and a persisted save.
    // Given cells are always taken from the fresh puzzle (same seed = same puzzle).
    // Non-given cells use the saved user value (0 = empty).
    private fun restoreState(fresh: BoardState, save: GameSave): BoardState {
        val savedValues = save.cells.split(",").map { it.trim().toIntOrNull() ?: 0 }
        val savedNotes = save.notes.split(";")
        val restoredCells = fresh.cells.mapIndexed { r, row ->
            row.mapIndexed { c, cell ->
                if (cell.given) {
                    cell
                } else {
                    val idx = r * BOARD_SIZE + c
                    val value = savedValues.getOrElse(idx) { 0 }
                    // A placed digit clears its notes, so only restore notes for empty cells.
                    val notes = if (value == 0) {
                        savedNotes.getOrElse(idx) { "" }.mapNotNull { it.digitToIntOrNull() }.toSet()
                    } else {
                        emptySet()
                    }
                    cell.copy(value = value, notes = notes)
                }
            }
        }
        val restoredSelection = if (save.selectedRow in 0 until BOARD_SIZE && save.selectedCol in 0 until BOARD_SIZE) {
            save.selectedRow to save.selectedCol
        } else {
            null
        }
        return fresh.copy(
            cells = restoredCells,
            mistakeCount = save.mistakeCount,
            moveCount = save.moveCount,
            hintsRemaining = save.hintsRemaining,
            selected = restoredSelection,
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
        // Show an interstitial after this many ms of zero input on the Game screen — only
        // when the orchestrator's frequency cap allows. Reset on every user action.
        // Harder boards get a longer window so a player thinking through a tough cell
        // isn't interrupted (Hard / Master / Edo => 120s, everything else => 90s).
        const val IDLE_INTERSTITIAL_MS = 90_000L
        const val IDLE_INTERSTITIAL_HARD_MS = 120_000L
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
