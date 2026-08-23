package de.rafaelbeckmann.hvkclient.domain.repository

import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkMigrateAccountDevV1Response
import de.rafaelbeckmann.hvkclient.domain.model.FeatureFlag
import kotlinx.coroutines.flow.Flow
import okhttp3.Cookie

interface HvkRepository {

    fun getCourseSearch(courseName: String): Flow<Resource<List<String>>>

    fun getFeatureFlags(): Flow<Resource<FeatureFlag>>

    suspend fun postSpAuthCookie(authCookie: List<Cookie>): Result<Unit>

    suspend fun getSpTest(): Unit

    fun devV1Migration(userId: Number, refreshToken: String): Flow<Resource<NetworkMigrateAccountDevV1Response>>

    /**
     * Clears all cached data from the local database.
     */
    suspend fun clearCache()
}