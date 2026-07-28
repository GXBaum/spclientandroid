package de.rafaelbeckmann.hvkclient.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rafaelbeckmann.hvkclient.data.remote.AuthInterceptor
import de.rafaelbeckmann.hvkclient.data.remote.HvkClientApi
import de.rafaelbeckmann.hvkclient.data.remote.PayloadDecoder
import de.rafaelbeckmann.hvkclient.data.remote.PayloadDecoderImpl
import de.rafaelbeckmann.hvkclient.data.remote.TokenAuthenticator
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.java.net.cookiejar.JavaNetCookieJar
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://hvk-api.rafaelbeckmann.de/v1/"

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(settingsRepository: SettingsRepository): AuthInterceptor {
        return AuthInterceptor(settingsRepository)
    }

    @Provides
    @Singleton
    fun providePayloadDecoder(): PayloadDecoder {
        val test = Json
        return PayloadDecoderImpl(test)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        settingsRepository: SettingsRepository,
        @AuthApi api: HvkClientApi // Use the qualified API to break the cycle
    ): TokenAuthenticator {
        return TokenAuthenticator(settingsRepository, api)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    // TODO: rename this function
    @Provides
    @Singleton
    fun provideMyApi(okHttpClient: OkHttpClient, moshi: Moshi): HvkClientApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(HvkClientApi::class.java)
    }

    // --- Start of cycle-breaking providers ---

    @Provides
    @Singleton
    @AuthApi // Provide the special OkHttpClient for auth
    fun provideAuthOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    @AuthApi // Provide the special HvkClientApi for auth
    fun provideAuthApi(@AuthApi okHttpClient: OkHttpClient, moshi: Moshi): HvkClientApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(HvkClientApi::class.java)
    }

    // --- End of cycle-breaking providers ---


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