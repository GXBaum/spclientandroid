package com.example.hvkclientmitbenachrichtigungen.domaIn.repository

import com.example.hvkclientmitbenachrichtigungen.data.model.UserCourse
import com.example.hvkclientmitbenachrichtigungen.data.model.TokenUpdateRequest
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
    
    /**
     * Updates the FCM token for a user on the server
     * @param tokenUpdateRequest The request containing the token and username
     */
    suspend fun updateToken(tokenUpdateRequest: TokenUpdateRequest)
}
