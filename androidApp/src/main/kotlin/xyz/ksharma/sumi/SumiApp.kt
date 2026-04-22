package xyz.ksharma.sumi

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import xyz.ksharma.sumi.di.androidModule
import xyz.ksharma.sumi.di.appModule

class SumiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SumiApp)
            modules(appModule, androidModule)
        }
    }
}
