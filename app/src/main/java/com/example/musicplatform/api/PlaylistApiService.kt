package com.example.musicplatform.api

import com.example.musicplatform.model.Playlist
import retrofit2.Call
import retrofit2.http.*

interface PlaylistApiService {

    @GET("playlists/{playlistId}")
    fun getPlaylist(
        @Path("playlistId") playlistId: Long
    ): Call<Playlist>

    @GET("playlists")
    fun getPlaylists(): Call<List<Playlist>>

    @POST("playlists")
    fun createPlaylist(
        @Query("name") name: String,
        @Query("description") description: String,
        @Query("userId") userId: Long
    ): Call<Playlist>

    @POST("playlists/{playlistId}/tracks/{trackId}")
    fun addTrackToPlaylist(
        @Path("playlistId") playlistId: Long,
        @Path("trackId") trackId: Long
    ): Call<Playlist>

    @DELETE("playlists/{playlistId}/tracks/{trackId}")
    fun removeTrackFromPlaylist(
        @Path("playlistId") playlistId: Long,
        @Path("trackId") trackId: Long
    ): Call<Playlist>

    @DELETE("playlists/{playlistId}")
    fun deletePlaylist(
        @Path("playlistId") playlistId: Long
    ): Call<Void>
}

