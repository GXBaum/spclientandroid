package com.example

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FcmApi {
    @POST("/send")
    suspend fun sendMessage(
        @Body body: SendMessageDto
    )

    @POST("api/updateToken")
    suspend fun updateToken(
        @Body tokenUpdate: TokenUpdateRequest
    ): retrofit2.Response<Any>


    @GET("api/getUserCourses")
    suspend fun getUserCourses(
        @Query("spUsername") spUsername: String
    ): retrofit2.Response<UserCourses>
}