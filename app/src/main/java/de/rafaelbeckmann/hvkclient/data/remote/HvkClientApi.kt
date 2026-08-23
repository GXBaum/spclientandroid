package de.rafaelbeckmann.hvkclient.data.remote

import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCourseSearchResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkFeatureFlag
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkMigrateAccountDevV1Response
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkRefreshTokenRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkRefreshTokenResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.SpAuthCookieRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HvkClientApi {

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: NetworkRefreshTokenRequest
    ): Response<NetworkRefreshTokenResponse>

    @GET("featureFlags")
    suspend fun getFeatureFlags(
    ): Response<NetworkFeatureFlag>

    @GET("vp/courses")
    suspend fun getCourseSearch(
        @Query("search") search: String
    ): Response<NetworkCourseSearchResponse>

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