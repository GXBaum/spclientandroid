package de.rafaelbeckmann.hvkclient.core.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(
    modules = [
        AppModule::class,
        PlatformModule::class
    ]
)
@ComponentScan("de.rafaelbeckmann.hvkclient")
class AppConfig

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin<AppConfig> {
        config?.invoke(this)
    }
}