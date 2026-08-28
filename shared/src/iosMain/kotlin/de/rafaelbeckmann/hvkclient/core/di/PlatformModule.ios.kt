package de.rafaelbeckmann.hvkclient.core.di

import androidx.room.Room
import de.rafaelbeckmann.hvkclient.core.database.AppDatabase
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Module
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
            .fallbackToDestructiveMigration(true)
            .build()
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
}