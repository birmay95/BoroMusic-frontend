package com.example.musicplatform.main

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.model.Playlist
import com.example.musicplatform.model.Track
import com.example.musicplatform.model.mapServerTrackToTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyViewModel() : ViewModel() {
    var samplePlaylists = mutableStateListOf<Playlist>()

    var sampleTracks = mutableStateListOf<Track>()

    var favouriteTracks = mutableStateListOf<Track>()

    var sampleUserPlaylists = mutableStateListOf<Playlist>()

    var recommendations = mutableStateListOf<Track>()

    fun fetchRecommendations(apiClient: ApiClient, trackId: String) {
        viewModelScope.launch {
            try {
                val result = apiClient.recommendationApiService.getRecommendations(trackId)

                val recommendedTracks = result.mapNotNull { recommendation ->
                    sampleTracks.find { it.id == recommendation.trackId }
                }

                recommendations.clear()
                recommendations.addAll(recommendedTracks)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun updateFavourite(apiClient: ApiClient, userId: Long, playlist: Playlist): Playlist {
        val favouriteResponse = apiClient.userApiService.getFavourites(userId)

        if (favouriteResponse.isSuccessful) {
            val favouriteFromUser = favouriteResponse.body() ?: emptyList()

            val favouriteTracksSet = favouriteFromUser.map { it.id }.toSet()

            playlist.tracks.forEach { track ->
                track.favourite = track.id in favouriteTracksSet
            }
        }
        return playlist
    }

    fun loadSampleData(apiClient: ApiClient, userId: Long, onLoadDataFailed: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tracksResponse = apiClient.trackApiService.getTracks().execute()
                val playlistsResponse = apiClient.playlistApiService.getPlaylists().execute()
                val favouriteResponse = apiClient.userApiService.getFavourites(userId)
                val userPlaylistsResponse = apiClient.userApiService.getPlaylists(userId)

                if (tracksResponse.isSuccessful && playlistsResponse.isSuccessful && favouriteResponse.isSuccessful && userPlaylistsResponse.isSuccessful) {
                    val serverTracks = tracksResponse.body() ?: emptyList()
                    val playlistsFromServer = playlistsResponse.body() ?: emptyList()
                    val favouriteFromUser = favouriteResponse.body() ?: emptyList()
                    val userPlaylists = userPlaylistsResponse.body() ?: emptyList()

                    val favouriteTracksSet = favouriteFromUser.map { it.id }.toSet()

                    val tracksFromServer = serverTracks.map {
                        mapServerTrackToTrack(
                            it,
                            favourite = it.id in favouriteTracksSet,
                            track = 0
                        )
                    }

                    withContext(Dispatchers.Main) {
                        sampleTracks.clear()
                        sampleTracks.addAll(tracksFromServer)
                        samplePlaylists.clear()
                        samplePlaylists.addAll(playlistsFromServer)
                        samplePlaylists.forEach { playlist ->
                            playlist.tracks.fastForEach { track ->
                                track.favourite = track.id in favouriteTracksSet
                            }
                        }
                        favouriteTracks.clear()
                        favouriteTracks.addAll(favouriteFromUser)
                        favouriteTracks.forEach { track ->
                            track.favourite = track.id in favouriteTracksSet
                        }

                        sampleUserPlaylists.clear()
                        sampleUserPlaylists.addAll(userPlaylists)
                        sampleUserPlaylists.forEach { playlist: Playlist ->
                            playlist.tracks.fastForEach { track ->
                                track.favourite = track.id in favouriteTracksSet
                                Log.d("Favourite", "${track.title} - ${track.favourite}")
                            }
                        }
                    }
                } else if (tracksResponse.code() == 403 && playlistsResponse.code() == 403 && favouriteResponse.code() == 403 && userPlaylistsResponse.code() == 403) {
                    Log.e(
                        "LoadData",
                        "Failed to load data: ${tracksResponse.errorBody()?.string()}"
                    )
                    withContext(Dispatchers.Main) {
                        onLoadDataFailed()
                    }
                }
            } catch (e: Exception) {
                Log.e("LoadData", "Error occurred: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
