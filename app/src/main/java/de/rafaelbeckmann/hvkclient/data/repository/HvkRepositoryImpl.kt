package de.rafaelbeckmann.hvkclient.data.repository

import android.app.Application
import android.util.Log
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.data.model.FeatureFlag
import de.rafaelbeckmann.hvkclient.data.model.FeatureFlagEntity
import de.rafaelbeckmann.hvkclient.data.model.LoginRequest
import de.rafaelbeckmann.hvkclient.data.model.LoginResponse
import de.rafaelbeckmann.hvkclient.data.model.TokenUpdateRequest
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.data.model.VpInfo
import de.rafaelbeckmann.hvkclient.data.model.VpResponse
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourse
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionsCache
import de.rafaelbeckmann.hvkclient.data.model.createAccountRequest
import de.rafaelbeckmann.hvkclient.data.model.createAccountResponse
import de.rafaelbeckmann.hvkclient.data.remote.HvkClientApi
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.net.URLEncoder

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

    override fun createAccount(isNotificationEnabled: Int): Flow<Resource<createAccountResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.createAccount(createAccountRequest(isNotificationEnabled))
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error("Account creation failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
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

    override fun getUserCourses(userId: Int): Flow<Resource<List<UserCourse>>> = networkBoundResource(
        query = { cacheDao.getUserCourses() },
        fetch = { api.getUserCourses(userId) },
        saveFetchResult = { cacheDao.insertUserCourses(it.courses) }
    )

    override suspend fun updateToken(userId: Int, tokenUpdateRequest: TokenUpdateRequest): Result<Unit> {
        return try {
            val response = api.updateToken(userId, tokenUpdateRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to update token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserMarksForCourse(userId: Int, courseId: Int): Flow<Resource<List<UserMark>>> = networkBoundResource(
        query = { cacheDao.getUserMarksForCourse(courseId) },
        fetch = { api.getUserMarksForCourse(userId, courseId) },
        saveFetchResult = {
            cacheDao.deleteUserMarksForCourse(courseId)
            cacheDao.insertUserMarks(it.marks)
        }
    )

    override suspend fun postVpSelectedCourses(userId: Int, courseName: VpSelectedCourse): Result<Unit> {
        return try {
            val response = api.postVpSelectedCourses(userId, courseName)
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

    override fun getVpSelectedCourses(userId: Int): Flow<Resource<List<String>>> = networkBoundResource(
        query = {
            cacheDao.getVpSelectedCourses().map { courses ->
                courses.map { it.courseName }
            }
        },
        fetch = { api.getVpSelectedCourses(userId) },
        saveFetchResult = { response ->
            cacheDao.clearVpSelectedCourses()
            val courses = response.courses.map { VpSelectedCourse(it.course) }
            cacheDao.insertVpSelectedCourses(courses)
        }
    )

    override suspend fun deleteVpSelectedCourse(userId: Int, courseName: String): Result<Unit> {
        return try {
            // TODO: ist es vlt doch besser, es im body zu schicken, dann wäre das nicht nötig
            // TODO: das ist gottlos dumm, aber er macht es zu + und nicht %20
            val encodedCourseName = URLEncoder.encode(courseName, "UTF-8").replace("+", "%20")
            val response = api.deleteVpSelectedCourse(userId, encodedCourseName)
            if (response.isSuccessful) {
                // TODO: Uncomment when cacheDao is implemented
                //cacheDao.deleteVpSelectedCourse(courseName)
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to delete selected course: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getVpSubstitutionsMultipleCourses(courseNames: List<String>): Flow<Resource<VpResponse>> {
        return networkBoundResource(
            query = {
                cacheDao.getVpSubstitutionsForCourses(courseNames).map { cacheEntries ->
                    val substitutionsMap = cacheEntries.associate { it.courseName to it.vpClass }
                    VpResponse(substitutionsMap)
                }
            },
            fetch = {
                val coursesInOneString = courseNames.joinToString(",")
                Log.d("repo", coursesInOneString)
                api.getVpSubstitutionsMultipleCourses(coursesInOneString)
            },
            saveFetchResult = { result ->
                // First delete existing entries for these courses
                cacheDao.deleteVpSubstitutionsForCourses(courseNames)

                // Then save the new data
                result.substitutions.forEach { (courseName, vpClass) ->
                    val cacheEntry = VpSubstitutionsCache(courseName, vpClass)
                    cacheDao.insertVpSubstitutionsCache(cacheEntry)
                }
            }
        )
    }

    // TODO: vielleicht nicht direkt be jedem buchstaben suchen
    override fun getCourseSearch(courseName: String): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getCourseSearch(courseName)
            if (response.isSuccessful) {
                val courses = response.body()?.courses ?: emptyList()
                emit(Resource.Success(courses))
            } else {
                emit(Resource.Error("Course search failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
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
                val rows = result.featureFlags.map { (key, value) ->
                    FeatureFlagEntity(
                        key = key,
                        value = value
                    )
                }
                cacheDao.upsertFeatureFlags(rows)
            }
        )
    }

    override fun getVpInfo(): Flow<Resource<VpInfo>> {
        return networkBoundResource(
            query = {
                cacheDao.getVpInfoItems().map { items ->
                    VpInfo(info = items)
            }
        },
            fetch = { api.getVpInfo() },
            saveFetchResult = { result ->
                val items = result.info
                cacheDao.clearVpInfo()
                cacheDao.insertVpInfo(items)
            }
        )
    }

    override suspend fun clearCache() {
        cacheDao.clearAllCache()
    }
}
