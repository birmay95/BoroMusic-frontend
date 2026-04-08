package com.example.musicplatform.api

import com.example.musicplatform.model.PageResponse
import com.example.musicplatform.model.Playlist
import com.example.musicplatform.model.PlaylistRequestDto
import retrofit2.Call
import retrofit2.http.*
import java.util.UUID

interface PlaylistApiService {

    @GET("/api/v1/playlists/{playlistId}")
    fun getPlaylist(
        @Path("playlistId") playlistId: UUID
    ): Call<Playlist>

    @GET("/api/v1/playlists")
    fun getPlaylists(@Query("page") page: Int): Call<PageResponse<Playlist>>

    @POST("/api/v1/playlists")
    fun createPlaylist(@Body request: PlaylistRequestDto): Call<Playlist>

    @POST("/api/v1/playlists/{playlistId}/tracks/{trackId}")
    fun addTrackToPlaylist(
        @Path("playlistId") playlistId: UUID,
        @Path("trackId") trackId: UUID
    ): Call<Playlist>

    @DELETE("/api/v1/playlists/{playlistId}/tracks/{trackId}")
    fun removeTrackFromPlaylist(
        @Path("playlistId") playlistId: UUID,
        @Path("trackId") trackId: UUID
    ): Call<Playlist>

    @DELETE("/api/v1/playlists/{playlistId}")
    fun deletePlaylist(
        @Path("playlistId") playlistId: UUID
    ): Call<Void>
}

