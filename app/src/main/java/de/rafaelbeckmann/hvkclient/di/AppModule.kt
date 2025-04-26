package de.rafaelbeckmann.hvkclient.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rafaelbeckmann.hvkclient.data.remote.MyApi
import de.rafaelbeckmann.hvkclient.data.repository.MyRepositoryImpl
import de.rafaelbeckmann.hvkclient.domain.repository.MyRepository
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
    fun provideMyApi(): MyApi {
        return Retrofit.Builder()
            .baseUrl("https://rafaelbeckmann.de/api/dev/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(MyApi::class.java)
    }

    /**
     * This function is crucial for understanding how interfaces and implementations work together.
     * 
     * It tells Hilt (dependency injection framework):
     * "Whenever something needs a MyRepository, give it an instance of MyRepositoryImpl"
     * 
     * The return type is MyRepository (interface) but we're actually returning 
     * MyRepositoryImpl (the concrete implementation of that interface).
     * 
     * This is key to dependency injection - classes depend on interfaces, not concrete implementations.
     */
    @Provides
    @Singleton
    fun provideMyRepository(api: MyApi, app: Application): MyRepository {
        return MyRepositoryImpl(api, app)
    }
}
