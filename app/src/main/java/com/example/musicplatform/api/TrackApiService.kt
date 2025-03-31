package com.example.musicplatform.api

import com.example.musicplatform.model.ServerTrack
import com.example.musicplatform.model.Track
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface TrackApiService {

    @GET("/tracks/{trackId}")
    fun getTrack(@Path("trackId") trackId: Long): Call<Track>

    @GET("/tracks")
    fun getTracks(): Call<List<ServerTrack>>

    @POST("/tracks/upload")
    @Multipart
    fun uploadFile(
        @Part("userId") userId: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<Track>

    @POST("/tracks/upload/files")
    @Multipart
    fun uploadFiles(@Part userId: Long, @Part files: List<MultipartBody.Part>): Call<List<Track>>

    @DELETE("/tracks/{trackId}")
    fun deleteTrack(@Path("trackId") trackId: Long): Call<ResponseBody>

    @GET("/tracks/temporary-url")
    fun getTemporaryUrl(@Query("fileName") fileName: String): Call<String>

    @GET("/tracks/buckets")
    fun listBuckets(): Call<String>

    @GET("/tracks/download-and-save")
    fun downloadAndSaveFile(
        @Query("path") path: String,
        @Query("fileName") fileName: String
    ): Call<ResponseBody>

    @GET("/tracks/download")
    fun downloadFileAndSave(
        @Query("fileName") fileName: String,
        @Query("path") path: String
    ): Call<String>

    @Streaming
    @GET("/tracks/download/{fileName}")
    fun downloadFileStream(@Path("fileName") fileName: String): Call<ResponseBody>
}
