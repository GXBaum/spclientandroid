package de.rafaelbeckmann.hvkclient.data.repository

import androidx.datastore.core.DataStore
import de.rafaelbeckmann.hvkclient.UserPreferences
import de.rafaelbeckmann.hvkclient.domain.repository.EncryptedUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [EncryptedUserPreferencesRepository::class])
class EncryptedUserPreferencesRepositoryImpl(
    @Named("encrypted") private val dataStore: DataStore<UserPreferences>
) : EncryptedUserPreferencesRepository {

    override fun getUserPreferences(): Flow<UserPreferences> {
        return dataStore.data
    }

    override suspend fun setUserPreferences(userPreferences: UserPreferences)   {
        dataStore.updateData {
            userPreferences // TODO: will override the unchanged vals
        }
    }

    override suspend fun updateSpCredentials(username: String?, password: String?) {
        dataStore.updateData { oldData ->
            oldData.copy(
                spUsername = username?: oldData.spUsername,
                spPassword = password?: oldData.spPassword
            )
        }
    }
}