package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.remote

import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.TokenUpdateRequest
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.UserCourses
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.UserMarks
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MyApi {
    @PUT("api/users/{username}/notification-token")
    suspend fun updateToken(
        @Path("username") username: String,
        @Body tokenUpdate: TokenUpdateRequest
    ): Response<Any>

    @GET("api/users/{username}/courses")
    suspend fun getUserCourses(
        @Path("username") username: String
    ): Response<UserCourses>

    @GET("api/users/{username}/{courseId}/marks")
    suspend fun getUserMarksForCourse(
        @Path("username") username: String,
        @Path("courseId") courseId: Int
    ): Response<UserMarks>
}