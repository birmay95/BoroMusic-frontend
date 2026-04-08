package com.example.musicplatform.api

import com.example.musicplatform.model.ArtistRequest
import com.example.musicplatform.model.ArtistRequestCreateDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface ArtistApiService {

    @POST("/api/v1/artists/request")
    suspend fun requestArtist(@Body request: ArtistRequestCreateDto): Response<ResponseBody>

    @GET("/api/v1/artists/requests")
    suspend fun getAllRequests(): Response<List<ArtistRequest>>

    @GET("/api/v1/artists/requests/{userId}")
    suspend fun getRequest(@Path("userId") userId: UUID): Response<ArtistRequest>

    @POST("/api/v1/artists/approve/{requestId}")
    suspend fun approveArtist(@Path("requestId") requestId: UUID): Response<ResponseBody>

    @POST("/api/v1/artists/reject/{requestId}")
    suspend fun rejectArtist(@Path("requestId") requestId: UUID): Response<ResponseBody>
}
