package xyz.ksharma.sumi.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import xyz.ksharma.sumi.game.model.Difficulty

class DataStoreGameSaveRepository(private val store: DataStore<Preferences>) : GameSaveRepository {

    override suspend fun loadSave(difficulty: Difficulty): GameSave? {
        val prefs = store.data.first()
        val slot = difficulty.slot
        // An in-progress game persists until it's finished or replaced via "New Puzzle" —
        // no date comparison, so returning after several days resumes the same board.
        val day = prefs[slot.dayKey] ?: return null
        return GameSave(
            epochDay = day,
            cells = prefs[slot.cellsKey] ?: return null,
            elapsedMs = prefs[slot.elapsedKey] ?: 0L,
            mistakeCount = prefs[slot.mistakesKey] ?: 0,
            moveCount = prefs[slot.movesKey] ?: 0,
            hintsRemaining = prefs[slot.hintsKey] ?: 3,
            puzzleSeed = prefs[slot.seedKey] ?: 0L,
            notes = prefs[slot.notesKey] ?: "",
            selectedRow = prefs[slot.selectedRowKey] ?: -1,
            selectedCol = prefs[slot.selectedColKey] ?: -1,
        )
    }

    override suspend fun writeSave(difficulty: Difficulty, save: GameSave) {
        val slot = difficulty.slot
        store.edit { prefs ->
            prefs[slot.dayKey] = save.epochDay
            prefs[slot.cellsKey] = save.cells
            prefs[slot.elapsedKey] = save.elapsedMs
            prefs[slot.mistakesKey] = save.mistakeCount
            prefs[slot.movesKey] = save.moveCount
            prefs[slot.hintsKey] = save.hintsRemaining
            prefs[slot.seedKey] = save.puzzleSeed
            prefs[slot.notesKey] = save.notes
            prefs[slot.selectedRowKey] = save.selectedRow
            prefs[slot.selectedColKey] = save.selectedCol
        }
    }

    override suspend fun clearSave(difficulty: Difficulty) {
        val slot = difficulty.slot
        store.edit { prefs ->
            prefs.remove(slot.dayKey)
            prefs.remove(slot.cellsKey)
            prefs.remove(slot.elapsedKey)
            prefs.remove(slot.mistakesKey)
            prefs.remove(slot.movesKey)
            prefs.remove(slot.hintsKey)
            prefs.remove(slot.seedKey)
            prefs.remove(slot.notesKey)
            prefs.remove(slot.selectedRowKey)
            prefs.remove(slot.selectedColKey)
        }
    }

    override suspend fun clearAllSaves() {
        Difficulty.entries.forEach { clearSave(it) }
    }
}

private val Difficulty.slot: SaveSlot
    get() = SaveSlot(name.lowercase())

private class SaveSlot(private val diff: String) {
    val dayKey = longPreferencesKey("save_${diff}_day")
    val cellsKey = stringPreferencesKey("save_${diff}_cells")
    val elapsedKey = longPreferencesKey("save_${diff}_elapsed")
    val mistakesKey = intPreferencesKey("save_${diff}_mistakes")
    val movesKey = intPreferencesKey("save_${diff}_moves")
    val hintsKey = intPreferencesKey("save_${diff}_hints")
    val seedKey = longPreferencesKey("save_${diff}_seed")
    val notesKey = stringPreferencesKey("save_${diff}_notes")
    val selectedRowKey = intPreferencesKey("save_${diff}_sel_row")
    val selectedColKey = intPreferencesKey("save_${diff}_sel_col")
}
