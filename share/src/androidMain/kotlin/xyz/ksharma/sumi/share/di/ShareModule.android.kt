package xyz.ksharma.sumi.share.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import xyz.ksharma.sumi.share.AndroidPuzzleBookExporter
import xyz.ksharma.sumi.share.AndroidShareManager
import xyz.ksharma.sumi.share.PuzzleBookExporter
import xyz.ksharma.sumi.share.ShareManager

actual val shareModule = module {
    single<ShareManager> { AndroidShareManager(context = androidContext()) }
    single<PuzzleBookExporter> { AndroidPuzzleBookExporter(context = androidContext()) }
}
