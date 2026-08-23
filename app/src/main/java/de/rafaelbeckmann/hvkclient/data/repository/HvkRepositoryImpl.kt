package de.rafaelbeckmann.hvkclient.data.repository

import de.rafaelbeckmann.hvkclient.SnackbarController
import de.rafaelbeckmann.hvkclient.SnackbarEvent
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.Resource.Error
import de.rafaelbeckmann.hvkclient.data.Resource.Loading
import de.rafaelbeckmann.hvkclient.data.Resource.Success
import de.rafaelbeckmann.hvkclient.data.local.AppDatabase
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.data.remote.HvkClientApi
import de.rafaelbeckmann.hvkclient.data.remote.PayloadDecoder
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkMigrateAccountDevV1Response
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkTokenUpdateRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.SpAuthCookieRequest
import de.rafaelbeckmann.hvkclient.data.toDomain
import de.rafaelbeckmann.hvkclient.data.toEntity
import de.rafaelbeckmann.hvkclient.domain.model.FeatureFlag
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class HvkRepositoryImpl @Inject constructor(
    private val api: HvkClientApi,
    private val cacheDao: CacheDao,
    private val database: AppDatabase,
    private val payloadDecoder: PayloadDecoder
) : HvkRepository {

    /**
     * A generic function that provides a resource from network and/or cache.
     * @param query The function to query data from the local cache.
     * @param fetch The suspend function to fetch data from the network.
     * @param saveFetchResult The suspend function to save the network response to the cache.
     * @param shouldFetch A lambda to decide whether to fetch new data from the network.
     */
    private inline fun <ResultType, RequestType> networkBoundResource(
        crossinline query: () -> Flow<ResultType>,
        crossinline fetch: suspend () -> Response<RequestType>,
        crossinline saveFetchResult: suspend (RequestType) -> Unit,
        crossinline shouldFetch: (ResultType?) -> Boolean = { true }
    ): Flow<Resource<ResultType>> = flow {
        val data = query().first()
        emit(Loading(data))

        if (!shouldFetch(data)) {
            emitAll(query().map { Success(it) })
            return@flow
        }

        try {
            val response = fetch()
            if (response.isSuccessful) {
                response.body()?.let { saveFetchResult(it) }
                // Query again after saving to get the updated data
                emitAll(query().map { Success(it) })
            } else {
                SnackbarController.sendEvent(
                    event = SnackbarEvent(
                        message = "Fehler beim Laden (${response.code()}): ${response.message()}"
                    )
                )
                val error = "API Error: ${response.code()} ${response.message()}"
                // On error, emit the error but continue listening to the cache
                emitAll(query().map { Error(error, it) })
            }
        } catch (ce: CancellationException) {
            // cancellation is not an error; propagate it.
            throw ce
        } catch (e: Exception) {
            SnackbarController.sendEvent(
                event = SnackbarEvent(
                    message = "Fehler beim Laden: ${e.message}"
                )
            )
            emitAll(query().map { Error(e.message ?: "Unknown error", it) })
        }
    }

    override suspend fun updateToken(tokenUpdateRequest: NetworkTokenUpdateRequest): Result<Unit> {
        return try {
            val response = api.updateToken(tokenUpdateRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to update token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // TODO: vielleicht nicht direkt be jedem buchstaben suchen
    override fun getCourseSearch(courseName: String): Flow<Resource<List<String>>> = flow {
        emit(Loading())
        try {
            val response = api.getCourseSearch(courseName)
            if (response.isSuccessful) {
                val courses = response.body()?.courses ?: emptyList()
                emit(Success(courses.map { course -> course.name }))
            } else {
                emit(Error("Course search failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Error(e.message ?: "Unknown error"))
        }
    }

    override fun getFeatureFlags(): Flow<Resource<FeatureFlag>> {
        return networkBoundResource(
            query = {
                cacheDao.getFeatureFlags().map { rows ->
                    FeatureFlag(rows.associate { it.key to it.value })
                }
            },
            fetch = { api.getFeatureFlags() },
            saveFetchResult = { result ->
                cacheDao.clearFeatureFlags()

                val rows = result.toEntity()
                cacheDao.upsertFeatureFlags(rows)
            }
        )
    }

    override suspend fun postSpAuthCookie(authCookie: List<Cookie>): Result<Unit> {
        return try {
            val response = api.postSpAuthCookie(
                SpAuthCookieRequest(
                    "",
                    authCookie.map { it.toDomain() }
                )
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to post cookie: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSpTest() {
        try {
            val test = api.getSpTest()
        } catch (e: Exception) {
        }
    }

    override fun devV1Migration(userId: Number, refreshToken: String): Flow<Resource<NetworkMigrateAccountDevV1Response>> = flow {
        emit(Loading())
        try {
            val response = api.getDevV1Migration(userId, refreshToken)
            if (response.isSuccessful && response.body() != null) {
                emit(Success(response.body()!!))
            } else {
                emit(Error("Account migration failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Error(e.message ?: "An unknown error occurred"))
        }
    }

    override suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }
}
