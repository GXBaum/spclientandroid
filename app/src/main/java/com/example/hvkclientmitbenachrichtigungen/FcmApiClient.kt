package com.example.hvkclientmitbenachrichtigungen

import com.example.FcmApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object FcmApiClient {
    private const val BASE_URL = "https://rafaelbeckmann.de/"


    fun getApi(): FcmApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return retrofit.create(FcmApi::class.java)
    }
}