package com.example.musicplatform.api

import com.example.musicplatform.model.PageResponse
import com.example.musicplatform.model.ServerTrack
import com.example.musicplatform.model.Track
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.util.UUID

interface TrackApiService {

    @GET("/api/v1/tracks/{trackId}")
    fun getTrack(@Path("trackId") trackId: UUID): Call<Track>

    @GET("/api/v1/tracks")
    fun getTracks(@Query("page") page: Int): Call<PageResponse<ServerTrack>>

    @POST("/api/v1/tracks/upload")
    @Multipart
    fun uploadFile(@Part file: MultipartBody.Part): Call<Track>

    @POST("/api/v1/tracks/upload/files")
    @Multipart
    fun uploadFiles(@Part files: List<MultipartBody.Part>): Call<List<Track>>

    @DELETE("/api/v1/tracks/{trackId}")
    fun deleteTrack(@Path("trackId") trackId: UUID): Call<ResponseBody>

    @GET("/api/v1/tracks/temporary-url")
    fun getTemporaryUrl(@Query("fileName") fileName: String): Call<ResponseBody>

    @GET("/api/v1/tracks/buckets")
    fun listBuckets(): Call<String>

    @GET("/api/v1/tracks/download-and-save")
    fun downloadAndSaveFile(
        @Query("path") path: String,
        @Query("fileName") fileName: String
    ): Call<ResponseBody>

    @GET("/api/v1/tracks/download")
    fun downloadFileAndSave(
        @Query("fileName") fileName: String,
        @Query("path") path: String
    ): Call<String>

    @Streaming
    @GET("/api/v1/tracks/download/{fileName}")
    fun downloadFileStream(@Path("fileName") fileName: String): Call<ResponseBody>
}
