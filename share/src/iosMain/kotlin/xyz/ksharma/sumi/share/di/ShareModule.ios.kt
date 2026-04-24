package xyz.ksharma.sumi.share.di

import org.koin.dsl.module
import xyz.ksharma.sumi.share.IosShareManager
import xyz.ksharma.sumi.share.ShareManager

actual val shareModule = module {
    single<ShareManager> { IosShareManager() }
}
