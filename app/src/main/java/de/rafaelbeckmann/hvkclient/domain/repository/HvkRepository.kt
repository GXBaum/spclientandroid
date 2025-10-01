package de.rafaelbeckmann.hvkclient.domain.repository

import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.model.FeatureFlag
import de.rafaelbeckmann.hvkclient.data.model.LoginResponse
import de.rafaelbeckmann.hvkclient.data.model.TokenUpdateRequest
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.data.model.VpInfo
import de.rafaelbeckmann.hvkclient.data.model.VpResponse
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourse
import de.rafaelbeckmann.hvkclient.data.model.createAccountResponse
import kotlinx.coroutines.flow.Flow

/**
 * This interface defines the contract for repository operations.
 * It specifies WHAT operations can be performed but not HOW they are implemented.
 * This allows us to:
 * 1. Easily swap implementations (e.g., for testing)
 * 2. Keep our ViewModel decoupled from specific implementation details
 */
interface HvkRepository {

    fun createAccount(isNotificationEnabled: Int): Flow<Resource<createAccountResponse>>

    fun login(username: String, password: String): Flow<Resource<LoginResponse>>

    /**
     * Fetches user courses from the API
     * @param username The username to fetch courses for
     * @return Flow emitting either a success with list of courses or an error
     */
    fun getUserCourses(userId: Int): Flow<Resource<List<UserCourse>>>

    fun getUserMarksForCourse(userId: Int, courseId: Int): Flow<Resource<List<UserMark>>>

    /**
     * Updates the FCM token for a user on the server
     * @param tokenUpdateRequest The request containing the token and username
     */
    suspend fun updateToken(userId: Int, tokenUpdateRequest: TokenUpdateRequest): Result<Unit>

    /**
     * Fetches the selected courses for a user
     * @param username The username to fetch selected courses for
     * @return Flow emitting either a success with list of selected courses or an error
     */
    fun getVpSelectedCourses(userId: Int): Flow<Resource<List<String>>>

    /**
     * Posts the selected courses for a user
     * @param username The username to post selected courses for
     * @param courseName The name of the course to post
     * @return Flow emitting either a success or an error
     */
    suspend fun postVpSelectedCourses(userId: Int, courseName: VpSelectedCourse): Result<Unit>

    suspend fun deleteVpSelectedCourse(userId: Int, courseName: String): Result<Unit>

    fun getVpSubstitutionsMultipleCourses(courseNames: List<String>): Flow<Resource<VpResponse>>

    fun getCourseSearch(courseName: String): Flow<Resource<List<String>>>

    fun getFeatureFlags(): Flow<Resource<FeatureFlag>>

    fun getVpInfo(): Flow<Resource<VpInfo>>

    /**
     * Clears all cached data from the local database.
     */
    suspend fun clearCache()
}