package xyz.ksharma.sumi.preferences

import xyz.ksharma.sumi.game.model.Difficulty

interface GameSaveRepository {
    /** Returns the save for [difficulty] if it exists and is for today's puzzle, null otherwise. */
    suspend fun loadSave(difficulty: Difficulty): GameSave?
    suspend fun writeSave(difficulty: Difficulty, save: GameSave)
    suspend fun clearSave(difficulty: Difficulty)
}
