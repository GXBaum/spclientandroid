package de.rafaelbeckmann.hvkclient.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rafaelbeckmann.hvkclient.data.remote.PayloadDecoder
import de.rafaelbeckmann.hvkclient.data.remote.PayloadDecoderImpl
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.java.net.cookiejar.JavaNetCookieJar
import okhttp3.logging.HttpLoggingInterceptor
import java.net.CookieManager
import java.net.CookiePolicy
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePayloadDecoder(): PayloadDecoder {
        val test = Json { ignoreUnknownKeys = true }
        return PayloadDecoderImpl(test)
    }

    @Qualifier
    annotation class UsingThisToNotHaveTheProvidesAnnotationDuplicationError

    @Provides
    @Singleton
    @UsingThisToNotHaveTheProvidesAnnotationDuplicationError
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