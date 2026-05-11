package com.example.musicplatform.main

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.model.ExcludedTracksRequest
import com.example.musicplatform.model.Playlist
import com.example.musicplatform.model.Track
import com.example.musicplatform.model.User
import com.example.musicplatform.model.mapServerTrackToTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MyViewModel() : ViewModel() {
    var samplePlaylists = mutableStateListOf<Playlist>()

    var sampleTracks = mutableStateListOf<Track>()

    var favouriteTracks = mutableStateListOf<Track>()

    var sampleUserPlaylists = mutableStateListOf<Playlist>()

    var recommendations = mutableStateListOf<Track>()

    private val listeningHistory = mutableListOf<UUID>()
    private val MAX_HISTORY_SIZE = 50

    var currentPage = 0
    var isLastPage = false
    var isLoadingNextPage = false

    var currentPlaylistPage = 0
    var isLastPlaylistPage = false
    var isLoadingNextPlaylistPage = false

    fun addTrackToHistory(trackId: UUID) {
        listeningHistory.remove(trackId)
        listeningHistory.add(trackId)

        if (listeningHistory.size > MAX_HISTORY_SIZE) {
            listeningHistory.removeAt(0)
        }
        Log.d("HistoryDebug", "Track added to history. Current history size: ${listeningHistory.size}")
    }

    fun fetchRecommendations(apiClient: ApiClient, trackId: UUID) {
        viewModelScope.launch {
            try {
                val request = ExcludedTracksRequest(listeningHistory.toList())
                val result = apiClient.recommendationApiService.getRecommendations(trackId, request)

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

    suspend fun updateFavourite(apiClient: ApiClient, userId: UUID, playlist: Playlist): Playlist {
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

    fun loadNextTracksPage(apiClient: ApiClient, userId: UUID) {
        Log.d("PaginationDebug", "loadNextTracksPage called. currentPage=$currentPage, isLastPage=$isLastPage, isLoadingNextPage=$isLoadingNextPage")

        if (isLastPage || isLoadingNextPage) {
            Log.d("PaginationDebug", "Skipping load: either last page reached or already loading.")
            return
        }

        isLoadingNextPage = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("PaginationDebug", "Executing getTracks API call for page: $currentPage")
                val response = apiClient.trackApiService.getTracks(currentPage).execute()
                Log.d("PaginationDebug", "getTracks response code: ${response.code()}")

                val favouriteResponse = apiClient.userApiService.getFavourites(userId)
                Log.d("PaginationDebug", "getFavourites response code: ${favouriteResponse.code()}")

                if (response.isSuccessful && favouriteResponse.isSuccessful) {
                    val pageData = response.body()
                    Log.d("PaginationDebug", "Page data received: isNull=${pageData == null}")

                    if (pageData != null) {
                        Log.d("PaginationDebug", "Page elements count: ${pageData.content.size}, isLast=${pageData.last}")

                        val favouriteFromUser = favouriteResponse.body() ?: emptyList()
                        val favouriteTracksSet = favouriteFromUser.mapNotNull { it.id }.toSet()

                        val tracksFromServer = pageData.content.map {
                            mapServerTrackToTrack(
                                serverTrack = it,
                                favourite = it.id in favouriteTracksSet,
                                track = 0
                            )
                        }

                        withContext(Dispatchers.Main) {
                            sampleTracks.addAll(tracksFromServer)
                            currentPage++
                            isLastPage = pageData.last
                            Log.d("PaginationDebug", "Successfully added to sampleTracks. New size: ${sampleTracks.size}")
                        }
                    } else {
                        Log.e("PaginationDebug", "Page data is NULL. Check Retrofit mapping for PageResponse!")
                    }
                } else {
                    Log.e("PaginationDebug", "API call failed. Tracks Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("PaginationDebug", "Exception during loadNextTracksPage: ${e.message}", e)
                e.printStackTrace()
            } finally {
                isLoadingNextPage = false
            }
        }
    }

    fun loadNextPlaylistsPage(apiClient: ApiClient, userId: UUID) {
        if (isLastPlaylistPage || isLoadingNextPlaylistPage) return

        isLoadingNextPlaylistPage = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiClient.playlistApiService.getPlaylists(currentPlaylistPage).execute()
                val favouriteResponse = apiClient.userApiService.getFavourites(userId)

                if (response.isSuccessful && favouriteResponse.isSuccessful) {
                    val pageData = response.body()
                    val favouriteFromUser = favouriteResponse.body() ?: emptyList()
                    val favouriteTracksSet = favouriteFromUser.mapNotNull { it.id }.toSet()

                    if (pageData != null) {
                        val newPlaylists = pageData.content
                        newPlaylists.forEach { playlist ->
                            playlist.tracks?.forEach { track ->
                                track.favourite = track.id in favouriteTracksSet
                            }
                        }

                        withContext(Dispatchers.Main) {
                            samplePlaylists.addAll(newPlaylists)
                            currentPlaylistPage++
                            isLastPlaylistPage = pageData.last
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingNextPlaylistPage = false
            }
        }
    }

    fun loadSampleData(apiClient: ApiClient, userId: UUID, onLoadDataFailed: () -> Unit) {
        Log.d("PaginationDebug", "loadSampleData started for userId: $userId")
        currentPage = 0
        isLastPage = false
        sampleTracks.clear()

        currentPlaylistPage = 0
        isLastPlaylistPage = false
        samplePlaylists.clear()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val favouriteResponse = apiClient.userApiService.getFavourites(userId)
                val userPlaylistsResponse = apiClient.userApiService.getPlaylists(userId)

                Log.d("PaginationDebug", "Initial data: Favourites Code=${favouriteResponse.code()}, UserPlaylists Code=${userPlaylistsResponse.code()}")

                if (favouriteResponse.isSuccessful && userPlaylistsResponse.isSuccessful) {
                    val favouriteFromUser = favouriteResponse.body() ?: emptyList()
                    val userPlaylists = userPlaylistsResponse.body() ?: emptyList()
                    val favouriteTracksSet = favouriteFromUser.mapNotNull { it.id }.toSet()

                    withContext(Dispatchers.Main) {
                        favouriteTracks.clear()
                        favouriteTracks.addAll(favouriteFromUser)
                        favouriteTracks.forEach { track ->
                            track.favourite = track.id in favouriteTracksSet
                        }

                        sampleUserPlaylists.clear()
                        sampleUserPlaylists.addAll(userPlaylists)
                        sampleUserPlaylists.forEach { playlist: Playlist ->
                            playlist.tracks?.fastForEach { track ->
                                track.favourite = track.id in favouriteTracksSet
                            }
                        }

                        Log.d("PaginationDebug", "Initial data processed. Triggering loadNextTracksPage and loadNextPlaylistsPage")
                        loadNextTracksPage(apiClient, userId)
                        loadNextPlaylistsPage(apiClient, userId)
                    }
                } else if (favouriteResponse.code() == 403 || userPlaylistsResponse.code() == 403) {
                    Log.e("PaginationDebug", "403 Forbidden. Invoking onLoadDataFailed")
                    withContext(Dispatchers.Main) { onLoadDataFailed() }
                }
            } catch (e: Exception) {
                Log.e("PaginationDebug", "Exception in loadSampleData: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    fun loadPersonalFeedAndNavigate(apiClient: ApiClient, user: User, onSuccess: (Playlist) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = ExcludedTracksRequest(listeningHistory.toList())
                val response = apiClient.recommendationApiService.getPersonalRecommendations(
                    request
                ).execute()

                if (response.isSuccessful) {
                    val recommendations = response.body() ?: emptyList()

                    val personalTracks = recommendations.mapNotNull { rec ->
                        sampleTracks.find { it.id == rec.trackId }
                    }.toMutableList()

                    val personalPlaylist = Playlist(
                        id = UUID.randomUUID(),
                        name = "Personal Feed",
                        description = "Specially selected tracks based on your taste.",
                        tracks = personalTracks,
                    )

                    withContext(Dispatchers.Main) {
                        onSuccess(personalPlaylist)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
