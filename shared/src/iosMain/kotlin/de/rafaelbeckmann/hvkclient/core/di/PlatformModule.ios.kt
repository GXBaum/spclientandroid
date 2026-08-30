package de.rafaelbeckmann.hvkclient.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import de.rafaelbeckmann.hvkclient.UserPreferences
import de.rafaelbeckmann.hvkclient.core.database.AppDatabase
import de.rafaelbeckmann.hvkclient.domain.repository.SpRepositoryTest
import de.rafaelbeckmann.hvkclient.features.other.data.NetworkCookie
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import okio.Path.Companion.toPath
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module
actual class PlatformModule {
    @Single
    fun provideHttpClientEngine(): HttpClientEngine = Darwin.create()

    @Single
    fun provideAppDatabase(): AppDatabase {
        val dbFilePath = documentDirectory() + "/hvk-client-database.db"
        return Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Single
    @Named("preferences")
    fun provideDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            (documentDirectory() + "/local.preferences_pb").toPath()
        }
    )

    // FIXME: PLACEHOLDER
    @Single
    @Named("encrypted")
    fun provideUserPreferencesDataStore(): DataStore<UserPreferences> = object : DataStore<UserPreferences> {
        private val state = MutableStateFlow(UserPreferences())
        override val data: Flow<UserPreferences> = state
        override suspend fun updateData(transform: suspend (t: UserPreferences) -> UserPreferences): UserPreferences {
            return state.updateAndGet { transform(it) }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory?.path)
    }


    // FIXME: PLACEHOLDER
    @Single
    fun provideSpRepositoryTest(): SpRepositoryTest = object : SpRepositoryTest {
        override suspend fun getSpAuthCookiesTest(): List<NetworkCookie> = emptyList()
    }
}