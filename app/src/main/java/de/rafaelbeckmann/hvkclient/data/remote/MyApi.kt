package de.rafaelbeckmann.hvkclient.data.remote

import de.rafaelbeckmann.hvkclient.data.model.TokenUpdateRequest
import de.rafaelbeckmann.hvkclient.data.model.UserCourses
import de.rafaelbeckmann.hvkclient.data.model.UserMarks
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourse
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutions
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

    @GET("vpSubstitutions/{courseName}/{day}")
    suspend fun getVpSubstitutions(
        @Path("courseName") courseName: String,
        @Path("day") day: String
    ): Response<VpSubstitutions>
}