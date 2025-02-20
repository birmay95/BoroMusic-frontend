package com.example.musicplatform.api

import com.example.musicplatform.tracks.ArtistRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ArtistApiService {
    @POST("api/artists/request/{userId}")
    suspend fun requestArtist(@Path("userId") userId: Long): Response<ResponseBody>

    @GET("api/artists/requests")
    suspend fun getAllRequests(): Response<List<ArtistRequest>>

    @POST("api/artists/approve/{requestId}")
    suspend fun approveArtist(@Path("requestId") requestId: Long): Response<ResponseBody>

    @POST("api/artists/reject/{requestId}")
    suspend fun rejectArtist(@Path("requestId") requestId: Long): Response<ResponseBody>
}
