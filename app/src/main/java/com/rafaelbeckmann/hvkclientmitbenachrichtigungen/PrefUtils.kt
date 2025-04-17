package com.rafaelbeckmann.hvkclientmitbenachrichtigungen

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PrefUtils(val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "local")

    suspend fun saveString(key: String, value: String) {
        context.dataStore.edit {
            it[stringPreferencesKey(key)] = value
        }
    }

    suspend fun getString(key: String): String? {
        return context.dataStore.data.map {
            it[stringPreferencesKey(key)]
        }.first()
    }
}