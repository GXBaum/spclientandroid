package de.rafaelbeckmann.hvkclient.data.remote

import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCourseSearchResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCreateAccountRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCreateAccountResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkFeatureFlag
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkLoginRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkLoginResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkMigrateAccountDevV1Response
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkRefreshTokenRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkRefreshTokenResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkSingleCourseResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkTokenUpdateRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserCoursesResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserMarks
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpSelectedCourseRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpSelectedCoursesResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.SpAuthCookieRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HvkClientApi {
    @POST("auth/login")
    suspend fun login(
        @Body request: NetworkLoginRequest
    ): Response<NetworkLoginResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: NetworkRefreshTokenRequest
    ): Response<NetworkRefreshTokenResponse>

    @POST("users/me/notification-token")
    suspend fun updateToken(
        @Body tokenUpdate: NetworkTokenUpdateRequest
    ): Response<Any>


    @POST("auth/register")
    suspend fun createAccount(
        @Body request: NetworkCreateAccountRequest
    ): Response<NetworkCreateAccountResponse>


    @GET("featureFlags")
    suspend fun getFeatureFlags(
    ): Response<NetworkFeatureFlag>

    @GET("vp/courses")
    suspend fun getCourseSearch(
        @Query("search") search: String
    ): Response<NetworkCourseSearchResponse>

    @GET("sp/courses")
    suspend fun getUserCourses(
    ): Response<NetworkUserCoursesResponse>

    @GET("sp/courses/{courseId}")
    suspend fun getUserCourseById(
        @Path("courseId") courseId: Int
    ): Response<NetworkSingleCourseResponse>

    @GET("sp/courses/{courseId}/marks")
    suspend fun getUserMarksForCourse(
        @Path("courseId") courseId: Int
    ): Response<NetworkUserMarks>

    @POST("vp/enrolled")
    suspend fun postVpSelectedCourses(
        @Body course: NetworkVpSelectedCourseRequest
    ): Response<Any>

    @GET("vp/enrolled")
    suspend fun getVpSelectedCourses(
    ): Response<NetworkVpSelectedCoursesResponse>

    @DELETE("vp/enrolled/{courseId}")
    suspend fun deleteVpSelectedCourse(
        @Path("courseId") courseId: String
    ): Response<Any>

    @GET("vp/substitutions")
    suspend fun getVpSubstitutionsMultipleCourses(
        @Query("courses") courses: List<String>
    ): Response<NetworkVpResponse>

    @POST("sp/authCookie")
    suspend fun postSpAuthCookie(
        @Body request: SpAuthCookieRequest
    ): Response<Any> // this is goofy as hell

    @GET("sp/test")
    suspend fun getSpTest(
    ): Response<Any>

    @GET("migrations/dev-v1/{userId}")
    suspend fun getDevV1Migration(
        @Path("userId") userId: Number,
        @Query("refreshTokenInRequest") refreshToken: String
    ): Response<NetworkMigrateAccountDevV1Response>
}