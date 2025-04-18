package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.repository

import android.app.Application
import android.util.Log
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.R
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.TokenUpdateRequest
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.UserCourse
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.UserMark
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.remote.MyApi
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.domaIn.repository.MyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

/**
 * This is the concrete implementation of the MyRepository interface.
 * It implements HOW the repository operations are performed.
 * Here we can use the API to make network calls and handle the data.
 */
class MyRepositoryImpl(
    private val api: MyApi,
    private val appContext: Application
): MyRepository {
    init {
        val appName = appContext.getString(R.string.app_name)
        println("App name: $appName")
    }

    
    /**
     * Implementation of getUserCourses that properly handles network errors and responses
     */
    override fun getUserCourses(username: String): Flow<Result<List<UserCourse>>> = flow {
        try {
            val response = api.getUserCourses(username)
            if (response.isSuccessful) {
                response.body()?.let { userCourses ->
                    emit(Result.success(userCourses.courses))
                } ?: emit(Result.failure(IOException("Response body is null")))
            } else {
                emit(Result.failure(IOException("Error ${response.code()}: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Implementation of updateToken that sends the FCM token to the server
     */
    override suspend fun updateToken(tokenUpdateRequest: TokenUpdateRequest) {
        try {
            val response = api.updateToken(tokenUpdateRequest)
            if (!response.isSuccessful) {
                Log.e("MyRepositoryImpl", "Failed to update token: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("MyRepositoryImpl", "Exception while updating token", e)
        }
    }



    override fun getUserMarksForCourse(courseId: Int): Flow<Result<List<UserMark>>> = flow {
        try {
            val response = api.getUserMarksForCourse(courseId)
            if (response.isSuccessful) {
                response.body()?.let { userMarks ->
                    emit(Result.success(userMarks.marks))
                } ?: emit(Result.failure(IOException("Response body is null")))
            } else {
                emit(Result.failure(IOException("Error ${response.code()}: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

}
