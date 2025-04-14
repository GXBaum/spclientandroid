package com.example.hvkclientmitbenachrichtigungen.data.repository

import android.app.Application
import com.example.UserCourse
import com.example.hvkclientmitbenachrichtigungen.R
import com.example.hvkclientmitbenachrichtigungen.data.remote.MyApi
import com.example.hvkclientmitbenachrichtigungen.domaIn.repository.MyRepository
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
     * Implementation of the doNetworkCall method defined in MyRepository interface.
     * Currently it's a TODO, but this is where you would make API calls using the api property.
     */
    override suspend fun doNetworkCall() {
        TODO("Not yet implemented")
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
}
