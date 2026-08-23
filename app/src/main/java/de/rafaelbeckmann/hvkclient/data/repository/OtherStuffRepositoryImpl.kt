package de.rafaelbeckmann.hvkclient.data.repository

import androidx.room.withTransaction
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.core.domain.Result
import de.rafaelbeckmann.hvkclient.core.domain.asEmptyDataResult
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.data.local.AppDatabase
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.data.remote.OtherStuffRemoteDataSource
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkMigrateAccountDevV1Response
import de.rafaelbeckmann.hvkclient.data.toDomain
import de.rafaelbeckmann.hvkclient.data.toEntity
import de.rafaelbeckmann.hvkclient.domain.model.FeatureFlag
import de.rafaelbeckmann.hvkclient.domain.repository.AuthRepository
import de.rafaelbeckmann.hvkclient.domain.repository.OtherStuffRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import javax.inject.Inject

class OtherStuffRepositoryImpl @Inject constructor(
    private val cacheDao: CacheDao,
    private val database: AppDatabase,
    private val settingsRepository: SettingsRepository,
    private val remoteDataSource: OtherStuffRemoteDataSource,
    private val authRepository: AuthRepository
) : OtherStuffRepository {
    override suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }

    override fun observeFeatureFlags(): Flow<FeatureFlag> {
        return cacheDao.getFeatureFlags().map { rows ->
            FeatureFlag(rows.associate { it.key to it.value })
        }
    }

    override suspend fun refreshFeatureFlags(): EmptyResult<DataError> {
        return remoteDataSource.getFeatureFlags()
            .onSuccess {
                database.withTransaction {
                    cacheDao.clearFeatureFlags()

                    val rows = it.toEntity()
                    cacheDao.upsertFeatureFlags(rows)
                }
            }
            .asEmptyDataResult()
    }

    override suspend fun getSpTest(): EmptyResult<DataError> {
        return remoteDataSource.getSpTest()
    }

    override suspend fun postSpAuthCookie(authCookie: List<Cookie>): EmptyResult<DataError> {
        return remoteDataSource.postSpAuthCookie("", authCookie.map { it.toDomain() })
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
                    val fcmToken = Firebase.messaging.token.await()
                    authRepository.addNotificationToken(fcmToken)
                }
            }
    }
}