package de.rafaelbeckmann.hvkclient.domain.repository

import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.model.FeatureFlag
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.data.model.VpDays
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCreateAccountResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkLoginResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkMigrateAccountDevV1Response
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkTokenUpdateRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpSelectedCourseRequest
import de.rafaelbeckmann.hvkclient.ui.settings.SelectedCourse
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
     * Fetches user courses from the API
     * @param userId The userId to fetch courses for
     * @return Flow emitting either a success with list of courses or an error
     */
    fun getUserCourses(): Flow<Resource<List<UserCourse>>>

    fun getUserCourseById(courseId: Int): Flow<Resource<UserCourse>>

    fun getUserMarksForCourse(courseId: Int): Flow<Resource<List<UserMark>>>

    /**
     * Updates the FCM token for a user on the server
     * @param tokenUpdateRequest The request containing the token and username
     */
    suspend fun updateToken(tokenUpdateRequest: NetworkTokenUpdateRequest): Result<Unit>

    /**
     * Fetches the selected courses for a user
     * @param userId The userId to fetch selected courses for
     * @return Flow emitting either a success with list of selected courses or an error
     */
    fun getVpSelectedCourses(): Flow<Resource<List<SelectedCourse>>>

    /**
     * Posts the selected courses for a user
     * @param courseName The name of the course to post
     * @return Flow emitting either a success or an error
     */
    suspend fun postVpSelectedCourses(courseName: NetworkVpSelectedCourseRequest): Result<Unit>

    suspend fun deleteVpSelectedCourse(courseId: String): Result<Unit>

    fun getVpSubstitutions(courseNames: List<String>): Flow<Resource<VpDays>>

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