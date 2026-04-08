package com.example.musicplatform.api

import com.example.musicplatform.model.ChangePasswordRequest
import com.example.musicplatform.model.Playlist
import com.example.musicplatform.model.Track
import com.example.musicplatform.model.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface UserApiService {

    @GET("/api/v1/users/{userId}")
    fun getUser(@Path("userId") userId: UUID): Call<User>

    @POST("/api/v1/users/{userId}/favourites/{trackId}")
    suspend fun addToFavourites(
        @Path("userId") userId: UUID,
        @Path("trackId") trackId: UUID
    ): Response<ResponseBody>

    @DELETE("/api/v1/users/{userId}/favourites/{trackId}")
    suspend fun removeFromFavourites(
        @Path("userId") userId: UUID,
        @Path("trackId") trackId: UUID
    ): Response<ResponseBody>

    @GET("/api/v1/tracks/users/{userId}/favourites")
    suspend fun getFavourites(@Path("userId") userId: UUID): Response<List<Track>>

    @GET("/api/v1/playlists/users/{userId}/playlists")
    suspend fun getPlaylists(@Path("userId") userId: UUID): Response<List<Playlist>>

    @POST("/api/v1/users/{userId}/change-password")
    suspend fun changePassword(
        @Path("userId") userId: UUID,
        @Body request: ChangePasswordRequest
    ): Response<ResponseBody>

    @POST("/api/v1/users/{userId}/change-email")
    suspend fun changeEmail(
        @Path("userId") userId: UUID,
        @Body newEmail: String
    ): Response<ResponseBody>

    @DELETE("/api/v1/users/{id}")
    suspend fun deleteAccount(@Path("id") userId: UUID): Response<ResponseBody>
}