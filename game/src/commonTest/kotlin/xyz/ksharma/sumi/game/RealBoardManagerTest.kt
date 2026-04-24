@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.game

import xyz.ksharma.sumi.game.generator.SudokuGenerator
import xyz.ksharma.sumi.game.manager.RealBoardManager
import xyz.ksharma.sumi.game.model.Difficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RealBoardManagerTest {

    private fun manager() = RealBoardManager(SudokuGenerator.generate(Difficulty.Easy, seed = 42L))

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initialStateMatchesConstructorInput() {
        val board = SudokuGenerator.generate(Difficulty.Easy, seed = 42L)
        val mgr = RealBoardManager(board)
        assertEquals(board, mgr.state.value)
    }

    @Test
    fun initialStateHasNoSelection() {
        val mgr = manager()
        assertEquals(null, mgr.state.value.selected)
    }

    @Test
    fun initialStateHasThreeHints() {
        val mgr = manager()
        assertEquals(3, mgr.state.value.hintsRemaining)
    }

    @Test
    fun initialStateIsNotComplete() {
        val mgr = manager()
        assertFalse(mgr.state.value.isComplete)
    }

    // ── select() ─────────────────────────────────────────────────────────────

    @Test
    fun selectMutatesStateFlow() {
        val mgr = manager()
        mgr.select(0, 0)
        assertEquals(Pair(0, 0), mgr.state.value.selected)
    }

    @Test
    fun selectSameCellDeselects() {
        val mgr = manager()
        mgr.select(1, 1)
        mgr.select(1, 1)
        assertEquals(null, mgr.state.value.selected)
    }

    @Test
    fun selectDifferentCellMovesSelection() {
        val mgr = manager()
        mgr.select(0, 0)
        mgr.select(2, 3)
        assertEquals(Pair(2, 3), mgr.state.value.selected)
    }

    // ── enter() ──────────────────────────────────────────────────────────────

    @Test
    fun enterOnEmptyCellPlacesDigit() {
        val mgr = manager()
        val (r, c) = firstEmptyCell(mgr)
        mgr.select(r, c)
        val digit = mgr.state.value.solution[r][c]
        mgr.enter(digit)
        assertEquals(digit, mgr.state.value.cells[r][c].value)
    }

    @Test
    fun enterWrongDigitIncrementsMistakes() {
        val mgr = manager()
        val (r, c) = firstEmptyCell(mgr)
        mgr.select(r, c)
        val wrong = wrongFor(mgr, r, c)
        mgr.enter(wrong)
        assertEquals(1, mgr.state.value.mistakeCount)
    }

    @Test
    fun enterWithoutSelectionIsNoOp() {
        val mgr = manager()
        val before = mgr.state.value
        mgr.enter(5)
        assertEquals(before, mgr.state.value)
    }

    // ── erase() ──────────────────────────────────────────────────────────────

    @Test
    fun eraseClearsUserEntry() {
        val mgr = manager()
        val (r, c) = firstEmptyCell(mgr)
        mgr.select(r, c)
        mgr.enter(mgr.state.value.solution[r][c])
        mgr.erase()
        assertEquals(0, mgr.state.value.cells[r][c].value)
    }

    @Test
    fun eraseWithoutSelectionIsNoOp() {
        val mgr = manager()
        val before = mgr.state.value
        mgr.erase()
        assertEquals(before, mgr.state.value)
    }

    // ── undo() ───────────────────────────────────────────────────────────────

    @Test
    fun undoRevertsLastEntry() {
        val mgr = manager()
        val (r, c) = firstEmptyCell(mgr)
        mgr.select(r, c)
        mgr.enter(mgr.state.value.solution[r][c])
        val afterEntry = mgr.state.value.cells[r][c].value
        assertEquals(mgr.state.value.solution[r][c], afterEntry)
        mgr.undo()
        assertEquals(0, mgr.state.value.cells[r][c].value)
    }

    @Test
    fun undoOnFreshBoardIsNoOp() {
        val mgr = manager()
        val before = mgr.state.value.cells
        mgr.undo()
        assertEquals(before, mgr.state.value.cells)
    }

    // ── hint() ───────────────────────────────────────────────────────────────

    @Test
    fun hintDecrementsRemainingAndReturnsTrue() {
        val mgr = manager()
        val used = mgr.hint()
        assertTrue(used, "hint() should return true when a hint was applied")
        assertEquals(2, mgr.state.value.hintsRemaining)
    }

    @Test
    fun hintRevealsCellMatchingSolution() {
        val mgr = manager()
        val before = mgr.state.value.cells
        mgr.hint()
        val after = mgr.state.value.cells
        for (r in 0..8) for (c in 0..8) {
            if (!before[r][c].given && after[r][c].given) {
                assertEquals(mgr.state.value.solution[r][c], after[r][c].value)
                return
            }
        }
    }

    @Test
    fun hintReturnsFalseAfterExhausted() {
        val mgr = manager()
        repeat(3) { mgr.hint() }
        val used = mgr.hint()
        assertFalse(used, "hint() should return false when hints are exhausted")
        assertEquals(0, mgr.state.value.hintsRemaining)
    }

    // ── toggleNotes() ─────────────────────────────────────────────────────────

    @Test
    fun toggleNotesSetsNotesModeTrue() {
        val mgr = manager()
        mgr.toggleNotes()
        assertTrue(mgr.state.value.notesMode)
    }

    @Test
    fun toggleNotesTogglesTwiceRestoresOriginal() {
        val mgr = manager()
        mgr.toggleNotes()
        mgr.toggleNotes()
        assertFalse(mgr.state.value.notesMode)
    }

    @Test
    fun enterInNotesModeAddsNote() {
        val mgr = manager()
        val (r, c) = firstEmptyCell(mgr)
        mgr.toggleNotes()
        mgr.select(r, c)
        mgr.enter(4)
        assertTrue(4 in mgr.state.value.cells[r][c].notes)
    }

    // ── tick() ───────────────────────────────────────────────────────────────

    @Test
    fun tickAdvancesElapsedMs() {
        val mgr = manager()
        mgr.tick(500L)
        assertEquals(500L, mgr.state.value.elapsedMs)
    }

    @Test
    fun multipleTicksAccumulate() {
        val mgr = manager()
        mgr.tick(1000L)
        mgr.tick(1000L)
        assertEquals(2000L, mgr.state.value.elapsedMs)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun firstEmptyCell(mgr: RealBoardManager): Pair<Int, Int> {
        val b = mgr.state.value
        for (r in 0..8) for (c in 0..8) if (b.cells[r][c].value == 0 && !b.cells[r][c].given) return r to c
        error("No empty cell")
    }

    private fun wrongFor(mgr: RealBoardManager, r: Int, c: Int): Int {
        val correct = mgr.state.value.solution[r][c]
        return (1..9).first { it != correct }
    }
}
