package com.example.musicplatform.api

import com.example.musicplatform.model.ArtistRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ArtistApiService {

    @POST("artists/request/{userId}")
    suspend fun requestArtist(@Path("userId") userId: Long): Response<ResponseBody>

    @GET("artists/requests")
    suspend fun getAllRequests(): Response<List<ArtistRequest>>

    @GET("artists/requests/{userId}")
    suspend fun getRequest(@Path("userId") userId: Long): Response<ArtistRequest>

    @POST("artists/approve/{requestId}")
    suspend fun approveArtist(@Path("requestId") requestId: Long): Response<ResponseBody>

    @POST("artists/reject/{requestId}")
    suspend fun rejectArtist(@Path("requestId") requestId: Long): Response<ResponseBody>
}
