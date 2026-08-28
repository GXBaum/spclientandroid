package de.rafaelbeckmann.hvkclient.features.other.data

import de.rafaelbeckmann.hvkclient.NotificationTokenProvider
import de.rafaelbeckmann.hvkclient.core.database.AppDatabase
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.core.domain.Result
import de.rafaelbeckmann.hvkclient.core.domain.asEmptyDataResult
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.features.auth.domain.AuthRepository
import de.rafaelbeckmann.hvkclient.features.other.domain.FeatureFlag
import de.rafaelbeckmann.hvkclient.features.other.domain.OtherStuffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [OtherStuffRepository::class])
class OtherStuffRepositoryImpl(
    private val dao: OtherDao,
    private val database: AppDatabase,
    private val settingsRepository: SettingsRepository,
    private val remoteDataSource: OtherStuffRemoteDataSource,
    private val authRepository: AuthRepository,
    private val notificationTokenProvider: NotificationTokenProvider
) : OtherStuffRepository {
    override suspend fun clearCache() {
        // TODO: improve this
        dao.clearAllTablesButManualAgainThanksToKmp()
    }

    override fun observeFeatureFlags(): Flow<FeatureFlag> {
        return dao.getFeatureFlags().map { rows ->
            FeatureFlag(rows.associate { it.key to it.value })
        }
    }

    override suspend fun refreshFeatureFlags(): EmptyResult<DataError> {
        return remoteDataSource.getFeatureFlags()
            .onSuccess {
                val rows = it.toEntity()

                dao.clearAndUpsertFeatureFlags(rows)
            }
            .asEmptyDataResult()
    }

    override suspend fun getSpTest(): EmptyResult<DataError> {
        return remoteDataSource.getSpTest()
    }

    override suspend fun postSpAuthCookie(authCookie: List<NetworkCookie>): EmptyResult<DataError> {
        return remoteDataSource.postSpAuthCookie("", authCookie)
    }

    override suspend fun devV1Migration(
        userId: Number,
        refreshToken: String
    ): Result<NetworkMigrateAccountDevV1Response, DataError> {
        return remoteDataSource.getDevV1Migration(userId, refreshToken)
            .onSuccess {
                settingsRepository.setAccessToken(it.token)
                settingsRepository.setUserId(it.id)
                settingsRepository.setOnboardingCompleted(true)

                runCatching {
                    val fcmToken = notificationTokenProvider.getToken().orEmpty() // FIXME: improve this, orEmpty to not have null
                    authRepository.addNotificationToken(fcmToken)
                }
            }
    }
}