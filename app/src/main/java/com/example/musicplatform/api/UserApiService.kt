package com.example.musicplatform.api

import com.example.musicplatform.tracks.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApiService {
    @GET("/users/{userId}")
    fun getUser(@Path("userId") userId: Long): Call<User>
}