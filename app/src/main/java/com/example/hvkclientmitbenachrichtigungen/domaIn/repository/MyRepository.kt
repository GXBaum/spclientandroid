package com.example.hvkclientmitbenachrichtigungen.domaIn.repository

import com.example.UserCourse
import kotlinx.coroutines.flow.Flow

/**
 * This interface defines the contract for repository operations.
 * It specifies WHAT operations can be performed but not HOW they are implemented.
 * This allows us to:
 * 1. Easily swap implementations (e.g., for testing)
 * 2. Keep our ViewModel decoupled from specific implementation details
 */
interface MyRepository {
    suspend fun doNetworkCall()
    
    /**
     * Fetches user courses from the API
     * @param username The username to fetch courses for
     * @return Flow emitting either a success with list of courses or an error
     */
    fun getUserCourses(username: String): Flow<Result<List<UserCourse>>>
}
