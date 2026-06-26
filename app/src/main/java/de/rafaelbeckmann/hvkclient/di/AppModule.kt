package de.rafaelbeckmann.hvkclient.di

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.data.local.AppDatabase
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.data.remote.AuthInterceptor
import de.rafaelbeckmann.hvkclient.data.remote.HvkClientApi
import de.rafaelbeckmann.hvkclient.data.remote.TokenAuthenticator
import de.rafaelbeckmann.hvkclient.data.repository.HvkRepositoryImpl
import de.rafaelbeckmann.hvkclient.data.repository.SettingsRepositoryImpl
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://rafaelbeckmann.de/api/v1/"

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


    // TODO: rename this function
    @Provides
    @Singleton
    fun provideMyRepository(
        api: HvkClientApi,
        cacheDao: CacheDao,
        database: AppDatabase,
        app: Application
    ): HvkRepository {
        return HvkRepositoryImpl(api, cacheDao, database, app)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(prefUtils: PrefUtils): SettingsRepository {
        return SettingsRepositoryImpl(prefUtils)
    }
}