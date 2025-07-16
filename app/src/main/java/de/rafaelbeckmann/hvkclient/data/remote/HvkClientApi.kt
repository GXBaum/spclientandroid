package de.rafaelbeckmann.hvkclient.data.remote

import de.rafaelbeckmann.hvkclient.data.model.LoginRequest
import de.rafaelbeckmann.hvkclient.data.model.LoginResponse
import de.rafaelbeckmann.hvkclient.data.model.RefreshTokenRequest
import de.rafaelbeckmann.hvkclient.data.model.TokenRefreshResponse
import de.rafaelbeckmann.hvkclient.data.model.TokenUpdateRequest
import de.rafaelbeckmann.hvkclient.data.model.UserCourses
import de.rafaelbeckmann.hvkclient.data.model.UserMarks
import de.rafaelbeckmann.hvkclient.data.model.VpResponse
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourse
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCoursesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HvkClientApi {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/token")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<TokenRefreshResponse>

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
    ): Response<VpSelectedCoursesResponse>

    @DELETE("users/{username}/vpSelectedCourses/{courseName}")
    suspend fun deleteVpSelectedCourse(
        @Path("username") username: String,
        @Path("courseName") courseName: String
    ): Response<Any>

    @GET("vpSubstitutions")
    suspend fun getVpSubstitutionsMultipleCourses(
        @Query("courses") courses: String
    ): Response<VpResponse>
}