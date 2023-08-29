package com.secureseco.trustsecoandroid.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class TrustScoreResponse(val trustScore: Int)

interface TrustScoreApiService {

    @GET("api/dlt/package/{packageName}/trust-score/{version}")
    suspend fun getTrustScore(
        @Path("packageName") packageName: String,
        @Path("version") version: String
    ): Response<TrustScoreResponse>

    companion object {
        fun create(): TrustScoreApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://localhost:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(TrustScoreApiService::class.java)
        }
    }
}