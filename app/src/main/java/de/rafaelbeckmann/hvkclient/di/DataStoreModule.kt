package de.rafaelbeckmann.hvkclient.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import de.rafaelbeckmann.hvkclient.UserPreferences
import de.rafaelbeckmann.hvkclient.UserPreferencesSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
object DataStoreModule {
    private const val DATASTORE_NAME = "local"

    @Single
    @Named("preferences")
    fun provideDataStore(context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO),
            produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
        )
    }


    private const val ENCRYPTED_DATASTORE_NAME = "encrypted"

    @Single
    @Named("encrypted")
    fun provideUserPreferencesDataStore(context: Context): DataStore<UserPreferences> {
        return DataStoreFactory.create(
            produceFile = { context.dataStoreFile(ENCRYPTED_DATASTORE_NAME) },
            serializer = UserPreferencesSerializer
        )
    }
}