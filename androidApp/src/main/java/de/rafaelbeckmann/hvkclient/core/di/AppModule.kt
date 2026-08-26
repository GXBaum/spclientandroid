package de.rafaelbeckmann.hvkclient.core.di

import de.rafaelbeckmann.hvkclient.core.data.HttpClientFactory
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.java.net.cookiejar.JavaNetCookieJar
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.net.CookieManager
import java.net.CookiePolicy

@Module
@ComponentScan("de.rafaelbeckmann.hvkclient")
class AppModule {

    @Single
    fun httpClient(
        settingsRepository: SettingsRepository
    ): HttpClient {
        return HttpClientFactory.create(
            OkHttp.create(),
            settingsRepository
        )
    }

    @Single
    fun json(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Single
    @Named("spAuthTest")
    fun provideSpAuthOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val cookieManager = CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        }

        return OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addInterceptor(loggingInterceptor)
            .build()
    }
}