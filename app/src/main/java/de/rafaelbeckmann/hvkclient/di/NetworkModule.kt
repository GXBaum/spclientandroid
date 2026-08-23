package de.rafaelbeckmann.hvkclient.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rafaelbeckmann.hvkclient.core.data.HttpClientFactory
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(
        settingsRepository: SettingsRepository
    ): HttpClient {
        return HttpClientFactory.create(
            OkHttp.create(),
            settingsRepository
        )
    }
}