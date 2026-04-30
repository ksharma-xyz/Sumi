package xyz.ksharma.sumi

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import xyz.ksharma.sumi.di.androidModule
import xyz.ksharma.sumi.di.appModule
import xyz.ksharma.sumi.platform.ads.initMobileAds
import xyz.ksharma.sumi.share.di.shareModule

class SumiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SumiApp)
            modules(appModule, androidModule, shareModule)
        }
        // Must run before any BannerAd / InterstitialAd composes — otherwise
        // AdView.loadAd() silently fails on Android. iOS init is handled by
        // basic-ads' BasicAds.Initialize() in App.kt; Android needs this
        // explicit Application-context call.
        initMobileAds(this)
    }
}
