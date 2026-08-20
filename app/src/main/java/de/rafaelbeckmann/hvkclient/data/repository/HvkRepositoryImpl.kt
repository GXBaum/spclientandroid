package de.rafaelbeckmann.hvkclient.data.repository

import android.app.Application
import androidx.room.withTransaction
import de.rafaelbeckmann.hvkclient.SnackbarController
import de.rafaelbeckmann.hvkclient.SnackbarEvent
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.Resource.Error
import de.rafaelbeckmann.hvkclient.data.Resource.Loading
import de.rafaelbeckmann.hvkclient.data.Resource.Success
import de.rafaelbeckmann.hvkclient.data.local.AppDatabase
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.data.mapEntitiesToVpDays
import de.rafaelbeckmann.hvkclient.data.model.FeatureFlag
import de.rafaelbeckmann.hvkclient.data.model.SelectedCourse
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.data.model.VpDays
import de.rafaelbeckmann.hvkclient.data.remote.HvkClientApi
import de.rafaelbeckmann.hvkclient.data.remote.PayloadDecoder
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCreateAccountRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCreateAccountResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkLoginRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkLoginResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkMigrateAccountDevV1Response
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkTokenUpdateRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpSelectedCourseRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.SpAuthCookieRequest
import de.rafaelbeckmann.hvkclient.data.toDomain
import de.rafaelbeckmann.hvkclient.data.toEntity
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    private val appContext: Application,
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

    override fun createAccount(): Flow<Resource<NetworkCreateAccountResponse>> = flow {
        emit(Loading())
        try {
            val response = api.createAccount(NetworkCreateAccountRequest())
            if (response.isSuccessful && response.body() != null) {
                emit(Success(response.body()!!))
            } else {
                emit(Error("Account creation failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Error(e.message ?: "An unknown error occurred"))
        }
    }

    override fun login(username: String, password: String): Flow<Resource<NetworkLoginResponse>> = flow {
        emit(Loading())
        try {
            val response = api.login(NetworkLoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                emit(Success(response.body()!!))
            } else {
                emit(Error("Login failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Error(e.message ?: "An unknown error occurred"))
        }
    }

    override fun getUserCourses(): Flow<Resource<List<UserCourse>>> = networkBoundResource(
        query = {
            cacheDao.getUserCourses().map { courses ->
                courses.map { it.toDomain() }
            }
        },
        fetch = { api.getUserCourses() },
        saveFetchResult = {
            val networkUserCourses = payloadDecoder.decodeUserCourses(it.courses)

            cacheDao.insertUserCourses(
                networkUserCourses.map { course -> course.toEntity() }
            )
        }
    )

    override fun getUserCourseById(courseId: Int): Flow<Resource<UserCourse>> = networkBoundResource(
        query = { cacheDao.getUserCourseById(courseId).map { it.toDomain() } },
        fetch = { api.getUserCourseById(courseId) },
        saveFetchResult = {
            cacheDao.insertUserCourses(
                listOf(it.course.toEntity())
            )
        }
    )

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

    override fun getUserMarksForCourse(courseId: Int): Flow<Resource<List<UserMark>>> = networkBoundResource(
        query = {
            cacheDao.getUserMarksForCourse(courseId).map { marks ->
                marks.map { it.toDomain() }
            }
        },
        fetch = { api.getUserMarksForCourse(courseId) },
        saveFetchResult = {
            cacheDao.deleteUserMarksForCourse(courseId)
            cacheDao.insertUserMarks(
                it.marks.map { mark -> mark.toEntity() }
            )
        }
    )

    override suspend fun postVpSelectedCourses(courseName: NetworkVpSelectedCourseRequest): Result<Unit> {
        return try {
            val response = api.postVpSelectedCourses(courseName)
            if (response.isSuccessful) {
                //cacheDao.insertVpSelectedCourses(listOf(courseName)) // aktuell nicht nötig, im Settings VM wird so oder so neu gefetcht // TODO: funktioniert mit Android 16 aber nicht 10?
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to post selected courses: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getVpSelectedCourses(): Flow<Resource<List<SelectedCourse>>> = networkBoundResource(
        query = {
            cacheDao.getVpSelectedCourses().map { courses ->
                courses.map {
                    it.toDomain()
                }
            }
        },
        fetch = { api.getVpSelectedCourses() },
        saveFetchResult = { response ->
            cacheDao.clearVpSelectedCourses()

            val courses = response.courses.map { it.toEntity() }
            cacheDao.insertVpSelectedCourses(courses)
        }
    )

    override suspend fun deleteVpSelectedCourse(courseId: String): Result<Unit> {
        return try {
            // TODO: ist es vlt doch besser, es im body zu schicken, dann wäre das nicht nötig
            // TODO: das ist gottlos dumm, aber er macht es zu + und nicht %20
            //val encodedCourseName = URLEncoder.encode(courseName, "UTF-8").replace("+", "%20")
            //val response = api.deleteVpSelectedCourse(encodedCourseName)
            val response = api.deleteVpSelectedCourse(courseId)

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

    override fun getVpSubstitutions(courseNames: List<String>): Flow<Resource<VpDays>> {
        return networkBoundResource(
            query = {
                cacheDao.getVpSubstitutionsForCourses(courseNames).combine(cacheDao.getVpDay()) { subs, days ->
                    mapEntitiesToVpDays(days, subs)
                }
            },
            fetch = {
                api.getVpSubstitutionsMultipleCourses(courseNames)
            },
            saveFetchResult = { result ->
                database.withTransaction {
                    // First delete existing entries for these courses
                    // this is dumb, but nothing lasts longer than a temporary fix
                    cacheDao.deleteVpSubstitutionsForCourses(courseNames)
                    cacheDao.deleteVpDayInfo()
                    cacheDao.clearVpDay()


                    result.substitutions.let { data ->
                        cacheDao.insertVpDay(
                            listOfNotNull(data.today, data.tomorrow)
                                .map { it.toEntity() }
                        )

                        val substitutions = listOfNotNull(data.today?.substitutions, data.tomorrow?.substitutions)
                            .flatten()
                            .map { it.toEntity() }

                        cacheDao.insertVpSubstitutions(substitutions)


                        val infos = listOfNotNull(data.today?.info, data.tomorrow?.info)
                            .flatten()
                            .map { it.toEntity() }

                        cacheDao.insertVpDayInfoItems(infos)
                    }
                }
            }
        )
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
