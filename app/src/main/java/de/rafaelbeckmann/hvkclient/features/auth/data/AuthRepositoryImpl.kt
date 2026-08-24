package de.rafaelbeckmann.hvkclient.features.auth.data

import de.rafaelbeckmann.hvkclient.core.database.AppDatabase
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.core.domain.asEmptyDataResult
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.features.auth.domain.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val database: AppDatabase,
    private val settingsRepository: SettingsRepository
) : AuthRepository {
    override suspend fun createAccount(): EmptyResult<DataError> {
        return remoteDataSource.postAccount()
            .onSuccess {
                // yup this code sucks
                settingsRepository.setAccessToken(it.token)
                settingsRepository.setRefreshToken(it.refreshToken)
                settingsRepository.setUserId(it.id)
                settingsRepository.setOnboardingCompleted(true)
            }
            .asEmptyDataResult()
    }

    override suspend fun login(username: String, password: String): EmptyResult<DataError> {
        return remoteDataSource.login(username, password)
            .onSuccess {
                // ...und... NOCH EIN FATALER CODE BLOCK
                settingsRepository.setAccessToken(it.accessToken)
                settingsRepository.setRefreshToken(it.refreshToken)
                settingsRepository.setUserId(it.userId)
                settingsRepository.setOnboardingCompleted(true)
            }
            .asEmptyDataResult()
    }

    override suspend fun addNotificationToken(token: String): EmptyResult<DataError> {
        return remoteDataSource.postFcmToken(token)
    }

}