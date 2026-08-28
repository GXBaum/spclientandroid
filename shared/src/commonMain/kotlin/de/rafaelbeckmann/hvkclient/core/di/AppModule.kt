package de.rafaelbeckmann.hvkclient.core.di

import de.rafaelbeckmann.hvkclient.core.data.HttpClientFactory
import de.rafaelbeckmann.hvkclient.core.database.AppDatabase
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.features.courses.data.CourseDao
import de.rafaelbeckmann.hvkclient.features.other.data.OtherDao
import de.rafaelbeckmann.hvkclient.features.vp.data.VpDao
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("de.rafaelbeckmann.hvkclient")
class AppModule {

    @Single
    fun httpClient(
        engine: HttpClientEngine,
        settingsRepository: SettingsRepository
    ): HttpClient {
        return HttpClientFactory.create(
            engine,
            settingsRepository
        )
    }

    @Single
    fun json(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Single
    fun provideVpDao(appDatabase: AppDatabase): VpDao {
        return appDatabase.vpDao()
    }

    @Single
    fun provideCourseDao(appDatabase: AppDatabase): CourseDao {
        return appDatabase.courseDao()
    }

    @Single
    fun provideOtherDao(appDatabase: AppDatabase): OtherDao {
        return appDatabase.otherDao()
    }
}