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

interface UserApiService {

    @GET("/users/{userId}")
    fun getUser(@Path("userId") userId: Long): Call<User>

    @POST("users/{userId}/favourites/{trackId}")
    suspend fun addToFavourites(
        @Path("userId") userId: Long,
        @Path("trackId") trackId: Long
    ): Response<ResponseBody>

    @DELETE("users/{userId}/favourites/{trackId}")
    suspend fun removeFromFavourites(
        @Path("userId") userId: Long,
        @Path("trackId") trackId: Long
    ): Response<ResponseBody>

    @GET("users/{userId}/favourites")
    suspend fun getFavourites(@Path("userId") userId: Long): Response<List<Track>>

    @GET("users/{userId}/playlists")
    suspend fun getPlaylists(@Path("userId") userId: Long): Response<List<Playlist>>

    @POST("users/{userId}/change-password")
    suspend fun changePassword(
        @Path("userId") userId: Long,
        @Body request: ChangePasswordRequest
    ): Response<ResponseBody>

    @POST("users/{userId}/change-email")
    suspend fun changeEmail(
        @Path("userId") userId: Long,
        @Body newEmail: String
    ): Response<ResponseBody>

    @DELETE("users/{userId}/delete")
    suspend fun deleteAccount(@Path("userId") userId: Long): Response<ResponseBody>
}