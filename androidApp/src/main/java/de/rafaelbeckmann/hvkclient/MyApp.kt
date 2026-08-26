package de.rafaelbeckmann.hvkclient

import android.app.Application
import de.rafaelbeckmann.hvkclient.core.di.initKoin
import org.koin.android.ext.koin.androidContext

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MyApp)
        }
    }
}