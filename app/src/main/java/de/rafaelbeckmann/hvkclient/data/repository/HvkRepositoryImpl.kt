package de.rafaelbeckmann.hvkclient.data.repository

import android.app.Application
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.data.model.LoginRequest
import de.rafaelbeckmann.hvkclient.data.model.LoginResponse
import de.rafaelbeckmann.hvkclient.data.model.TokenUpdateRequest
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourse
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionsAll
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionsAllCache
import de.rafaelbeckmann.hvkclient.data.remote.HvkClientApi
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class HvkRepositoryImpl(
    private val api: HvkClientApi,
    private val cacheDao: CacheDao,
    private val appContext: Application
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
        crossinline fetch: suspend () -> retrofit2.Response<RequestType>,
        crossinline saveFetchResult: suspend (RequestType) -> Unit,
        crossinline shouldFetch: (ResultType?) -> Boolean = { true }
    ): Flow<Resource<ResultType>> = flow {
        val data = query().first()
        emit(Resource.Loading(data))

        if (shouldFetch(data)) {
            try {
                val response = fetch()
                if (response.isSuccessful) {
                    response.body()?.let { saveFetchResult(it) }
                    // Query again after saving to get the updated data
                    emitAll(query().map { Resource.Success(it) })
                } else {
                    val error = "API Error: ${response.code()} ${response.message()}"
                    // On error, emit the error but continue listening to the cache
                    emitAll(query().map { Resource.Error(error, it) })
                }
            } catch (e: Exception) {
                // On exception, emit the error but continue listening to the cache
                emitAll(query().map { Resource.Error(e.message ?: "Unknown error", it) })
            }
        } else {
            // Data is fresh, just emit from cache
            emitAll(query().map { Resource.Success(it) })
        }
    }

    override fun login(username: String, password: String): Flow<Resource<LoginResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Login failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    override fun getUserCourses(username: String): Flow<Resource<List<UserCourse>>> = networkBoundResource(
        query = { cacheDao.getUserCourses() },
        fetch = { api.getUserCourses(username) },
        saveFetchResult = { cacheDao.insertUserCourses(it.courses) }
    )

    override suspend fun updateToken(username: String, tokenUpdateRequest: TokenUpdateRequest): Result<Unit> {
        return try {
            val response = api.updateToken(username, tokenUpdateRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to update token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserMarksForCourse(username: String, courseId: Int): Flow<Resource<List<UserMark>>> = networkBoundResource(
        query = { cacheDao.getUserMarksForCourse(courseId) },
        fetch = { api.getUserMarksForCourse(username, courseId) },
        saveFetchResult = {
            cacheDao.deleteUserMarksForCourse(courseId)
            cacheDao.insertUserMarks(it.marks)
        }
    )

    override suspend fun postVpSelectedCourses(username: String, courseName: VpSelectedCourse): Result<Unit> {
        return try {
            val response = api.postVpSelectedCourses(username, courseName)
            if (response.isSuccessful) {
                cacheDao.insertVpSelectedCourses(listOf(courseName))
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to post selected courses: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getVpSelectedCourses(username: String): Flow<Resource<List<String>>> = networkBoundResource(
        query = {
            cacheDao.getVpSelectedCourses().map { courses ->
                courses.map { it.courseName }
            }
        },
        fetch = { api.getVpSelectedCourses(username) },
        saveFetchResult = { response ->
            cacheDao.clearVpSelectedCourses()
            val courses = response.courses.map { VpSelectedCourse(it) }
            cacheDao.insertVpSelectedCourses(courses)
        }
    )

    override fun getVpSubstitutions(courseName: String, day: String): Flow<Resource<List<VpSubstitution>>> = networkBoundResource(
        query = { cacheDao.getVpSubstitutions() },
        fetch = { api.getVpSubstitutions(courseName, day) },
        saveFetchResult = {
            cacheDao.deleteVpSubstitutions()
            cacheDao.insertVpSubstitutions(it.substitutions)
        }
    )

    override fun getVpSubstitutionsAll(courseName: String): Flow<Resource<VpSubstitutionsAll>> {
        return networkBoundResource(
            query = {
                cacheDao.getVpSubstitutionsAll(courseName).map { cacheEntry ->
                    cacheEntry?.let {
                        VpSubstitutionsAll(it.substitutions)
                    } ?: VpSubstitutionsAll(emptyList())
                }
            },
            fetch = { api.getVpSubstitutionsAll(courseName) },
            saveFetchResult = {
                val cacheEntry = VpSubstitutionsAllCache(courseName, it.substitutions)
                cacheDao.insertVpSubstitutionsAll(cacheEntry)
            }
        )
    }

    override suspend fun clearCache() {
        cacheDao.clearAllCache()
    }
}