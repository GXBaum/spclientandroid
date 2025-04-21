package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.remote

import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.TokenUpdateRequest
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.UserCourses
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.UserMarks
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.VpSelectedCourse
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.VpSubstitution
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.VpSubstitutions
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MyApi {
    @PUT("users/{username}/notification-token")
    suspend fun updateToken(
        @Path("username") username: String,
        @Body tokenUpdate: TokenUpdateRequest
    ): Response<Any>

    @GET("users/{username}/courses")
    suspend fun getUserCourses(
        @Path("username") username: String
    ): Response<UserCourses>

    @GET("users/{username}/{courseId}/marks")
    suspend fun getUserMarksForCourse(
        @Path("username") username: String,
        @Path("courseId") courseId: Int
    ): Response<UserMarks>

    @POST("users/{username}/vpSelectedCourses")
    suspend fun postVpSelectedCourses(
        @Path("username") username: String,
        @Body courseName: VpSelectedCourse
    ): Response<Any>

    @GET("users/{username}/vpSelectedCourses")
    suspend fun getVpSelectedCourses(
        @Path("username") username: String
    ): Response<VpSelectedCourse>

    @GET("vpSubstitutions/{courseName}")
    suspend fun getVpSubstitutions(
        @Path("courseName") courseName: String
    ): Response<VpSubstitutions>
}