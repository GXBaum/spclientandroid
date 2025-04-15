package com.example.hvkclientmitbenachrichtigungen.data.remote

import com.example.hvkclientmitbenachrichtigungen.data.model.TokenUpdateRequest
import com.example.hvkclientmitbenachrichtigungen.data.model.UserCourses
import com.example.hvkclientmitbenachrichtigungen.data.model.UserMarks
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MyApi {

    @POST("api/updateToken")
    suspend fun updateToken(
        @Body tokenUpdate: TokenUpdateRequest
    ): Response<Any>


    @GET("api/getUserCourses")
    suspend fun getUserCourses(
        @Query("spUsername") spUsername: String
    ): Response<UserCourses>

    @GET("api/getUserMarksForCourse")
    suspend fun getUserMarksForCourse(
        @Query("courseId") courseId: Int
    ): Response<UserMarks>
}