package de.rafaelbeckmann.hvkclient.domain.repository

import de.rafaelbeckmann.hvkclient.UserPreferences
import kotlinx.coroutines.flow.Flow

interface EncryptedUserPreferencesRepository {

    fun getUserPreferences(): Flow<UserPreferences>

    suspend fun setUserPreferences(userPreferences: UserPreferences)

    suspend fun updateSpCredentials(
        username: String? = null,
        password: String? = null
    )
}