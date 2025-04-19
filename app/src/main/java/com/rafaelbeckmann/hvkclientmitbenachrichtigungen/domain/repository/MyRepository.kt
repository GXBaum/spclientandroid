package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.domaIn.repository

import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.TokenUpdateRequest
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.UserCourse
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.UserMark
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.VpSelectedCourse
import kotlinx.coroutines.flow.Flow

/**
 * This interface defines the contract for repository operations.
 * It specifies WHAT operations can be performed but not HOW they are implemented.
 * This allows us to:
 * 1. Easily swap implementations (e.g., for testing)
 * 2. Keep our ViewModel decoupled from specific implementation details
 */
interface MyRepository {

    /**
     * Fetches user courses from the API
     * @param username The username to fetch courses for
     * @return Flow emitting either a success with list of courses or an error
     */
    fun getUserCourses(username: String): Flow<Result<List<UserCourse>>>

    fun getUserMarksForCourse(username: String, courseId: Int): Flow<Result<List<UserMark>>>

    /**
     * Updates the FCM token for a user on the server
     * @param tokenUpdateRequest The request containing the token and username
     */
    suspend fun updateToken(username: String, tokenUpdateRequest: TokenUpdateRequest)

    /**
     * Fetches the selected courses for a user
     * @param username The username to fetch selected courses for
     * @return Flow emitting either a success with list of selected courses or an error
     */
    fun getVpSelectedCourses(username: String): Flow<Result<VpSelectedCourse>>

    /**
     * Posts the selected courses for a user
     * @param username The username to post selected courses for
     * @param courseName The name of the course to post
     * @return Flow emitting either a success or an error
     */
    suspend fun postVpSelectedCourses(username: String, courseName: VpSelectedCourse)

}
