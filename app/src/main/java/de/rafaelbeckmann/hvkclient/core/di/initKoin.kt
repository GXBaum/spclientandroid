package de.rafaelbeckmann.hvkclient.core.di

import de.rafaelbeckmann.hvkclient.di.DataStoreModule
import de.rafaelbeckmann.hvkclient.di.DatabaseModule
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(
    modules = [
        AppModule::class,
        DataStoreModule::class,
        DatabaseModule::class
    ]
)
@ComponentScan("de.rafaelbeckmann.hvkclient")
class AppConfig

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin<AppConfig> {
        config?.invoke(this)
        workManagerFactory()
    }
}