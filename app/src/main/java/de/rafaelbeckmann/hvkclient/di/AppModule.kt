package de.rafaelbeckmann.hvkclient.di

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.data.remote.HvkClientApi
import de.rafaelbeckmann.hvkclient.data.repository.HvkRepositoryImpl
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // TODO: rename this function
    @Provides
    @Singleton
    fun provideMyApi(moshi: Moshi): HvkClientApi {
        return Retrofit.Builder()
            .baseUrl("https://rafaelbeckmann.de/api/dev/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(HvkClientApi::class.java)
    }

    /**
     * This function is crucial for understanding how interfaces and implementations work together.
     *
     * It tells Hilt (dependency injection framework):
     * "Whenever something needs a HvkRepository, give it an instance of MyRepositoryImpl"
     *
     * The return type is HvkRepository (interface) but we're actually returning
     * MyRepositoryImpl (the concrete implementation of that interface).
     *
     * This is key to dependency injection - classes depend on interfaces, not concrete implementations.
     */
    @Provides
    @Singleton
    fun provideMyRepository(api: HvkClientApi, cacheDao: CacheDao, app: Application): HvkRepository {
        return HvkRepositoryImpl(api, cacheDao, app)
    }
}