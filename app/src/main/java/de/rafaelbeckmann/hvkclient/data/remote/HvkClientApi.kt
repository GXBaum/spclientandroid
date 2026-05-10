package de.rafaelbeckmann.hvkclient.data.remote

import de.rafaelbeckmann.hvkclient.data.model.CourseSearchResponse
import de.rafaelbeckmann.hvkclient.data.model.FeatureFlag
import de.rafaelbeckmann.hvkclient.data.model.LoginRequest
import de.rafaelbeckmann.hvkclient.data.model.LoginResponse
import de.rafaelbeckmann.hvkclient.data.model.RefreshTokenRequest
import de.rafaelbeckmann.hvkclient.data.model.SingleCourseResponse
import de.rafaelbeckmann.hvkclient.data.model.TokenRefreshResponse
import de.rafaelbeckmann.hvkclient.data.model.TokenUpdateRequest
import de.rafaelbeckmann.hvkclient.data.model.UserCourses
import de.rafaelbeckmann.hvkclient.data.model.UserMarks
import de.rafaelbeckmann.hvkclient.data.model.VpInfo
import de.rafaelbeckmann.hvkclient.data.model.VpResponse
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourseRequest
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCoursesResponse
import de.rafaelbeckmann.hvkclient.data.model.createAccountRequest
import de.rafaelbeckmann.hvkclient.data.model.createAccountResponse
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

    @PUT("users/{userId}/notification-token")
    suspend fun updateToken(
        @Path("userId") userId: Int,
        @Body tokenUpdate: TokenUpdateRequest
    ): Response<Any>


    @POST("users")
    suspend fun createAccount(
        @Body request: createAccountRequest
    ): Response<createAccountResponse>


    @GET("featureFlags")
    suspend fun getFeatureFlags(
    ): Response<FeatureFlag>

    @GET("courseSearch")
    suspend fun getCourseSearch(
        @Query("courseName") courseName: String
    ): Response<CourseSearchResponse>

    @GET("users/{userId}/courses")
    suspend fun getUserCourses(
        @Path("userId") userId: Int
    ): Response<UserCourses>

    @GET("users/{userId}/courses/{courseId}")
    suspend fun getUserCourseById(
        @Path("userId") userId: Int,
        @Path("courseId") courseId: Int
    ): Response<SingleCourseResponse>

    @GET("users/{userId}/{courseId}/marks")
    suspend fun getUserMarksForCourse(
        @Path("userId") userId: Int,
        @Path("courseId") courseId: Int
    ): Response<UserMarks>

    @POST("users/{userId}/vpSelectedCourses")
    suspend fun postVpSelectedCourses(
        @Path("userId") userId: Int,
        @Body courseName: VpSelectedCourseRequest
    ): Response<Any>

    @GET("users/{userId}/vpSelectedCourses")
    suspend fun getVpSelectedCourses(
        @Path("userId") userId: Int
    ): Response<VpSelectedCoursesResponse>

    @DELETE("users/{userId}/vpSelectedCourses/{courseName}")
    suspend fun deleteVpSelectedCourse(
        @Path("userId") userId: Int,
        @Path("courseName") courseName: String
    ): Response<Any>

    @GET("vpSubstitutions")
    suspend fun getVpSubstitutionsMultipleCourses(
        @Query("courses") courses: String
    ): Response<VpResponse>

    @GET("vp/info")
    suspend fun getVpInfo(
    ): Response<VpInfo>
}