package de.rafaelbeckmann.hvkclient

import android.app.Application
import de.rafaelbeckmann.hvkclient.core.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MyApp)
            workManagerFactory()
        }
    }
}