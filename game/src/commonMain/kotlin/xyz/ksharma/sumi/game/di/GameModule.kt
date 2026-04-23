package xyz.ksharma.sumi.game.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import xyz.ksharma.sumi.game.puzzle.PuzzleRepository
import xyz.ksharma.sumi.game.puzzle.RealPuzzleRepository

val gameModule = module {
    factoryOf(::RealPuzzleRepository) bind PuzzleRepository::class
}
