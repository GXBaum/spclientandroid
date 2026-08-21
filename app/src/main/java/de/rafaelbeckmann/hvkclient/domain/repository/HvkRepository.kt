package de.rafaelbeckmann.hvkclient.domain.repository

import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCreateAccountResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkLoginResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkMigrateAccountDevV1Response
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkTokenUpdateRequest
import de.rafaelbeckmann.hvkclient.domain.model.FeatureFlag
import kotlinx.coroutines.flow.Flow
import okhttp3.Cookie

/**
 * This interface defines the contract for repository operations.
 * It specifies WHAT operations can be performed but not HOW they are implemented.
 * This allows us to:
 * 1. Easily swap implementations (e.g., for testing)
 * 2. Keep our ViewModel decoupled from specific implementation details
 */
interface HvkRepository {

    fun createAccount(): Flow<Resource<NetworkCreateAccountResponse>>

    fun login(username: String, password: String): Flow<Resource<NetworkLoginResponse>>

    /**
     * Updates the FCM token for a user on the server
     * @param tokenUpdateRequest The request containing the token and username
     */
    suspend fun updateToken(tokenUpdateRequest: NetworkTokenUpdateRequest): Result<Unit>

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