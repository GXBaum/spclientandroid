package de.rafaelbeckmann.hvkclient.features.other.domain

import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.core.domain.Result
import de.rafaelbeckmann.hvkclient.features.other.data.NetworkMigrateAccountDevV1Response
import kotlinx.coroutines.flow.Flow
import okhttp3.Cookie

interface OtherStuffRepository {
    suspend fun clearCache()

    fun observeFeatureFlags(): Flow<FeatureFlag>
    suspend fun refreshFeatureFlags(): EmptyResult<DataError>

    suspend fun getSpTest(): EmptyResult<DataError>

    suspend fun postSpAuthCookie(authCookie: List<Cookie>): EmptyResult<DataError>

    suspend fun devV1Migration(userId: Number, refreshToken: String): Result<NetworkMigrateAccountDevV1Response, DataError>
}