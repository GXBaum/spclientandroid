package de.rafaelbeckmann.hvkclient

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefUtils @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    suspend fun saveString(key: String, value: String) {
        dataStore.edit {
            it[stringPreferencesKey(key)] = value
        }
    }

    suspend fun getString(key: String): String? {
        return dataStore.data.map {
            it[stringPreferencesKey(key)]
        }.first()
    }

    suspend fun removeString(key: String) {
        dataStore.edit {
            it.remove(stringPreferencesKey(key))
        }
    }

    fun stringFlow(key: String): Flow<String?> {
        return dataStore.data.map {
            it[stringPreferencesKey(key)]
        }
    }

    fun booleanFlow(key: String, default: Boolean): Flow<Boolean> {
        return stringFlow(key).map {
            it?.toBooleanStrictOrNull() ?: default
        }
    }
}
