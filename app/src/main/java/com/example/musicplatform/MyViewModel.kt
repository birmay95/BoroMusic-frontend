package com.example.musicplatform

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModel
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.tracks.Playlist
import com.example.musicplatform.tracks.Track
import com.example.musicplatform.tracks.mapServerTrackToTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyViewModel : ViewModel() {
    var samplePlaylists = mutableStateListOf<Playlist>()

    var sampleTracks = mutableStateListOf<Track>()

    fun loadSampleData(apiClient: ApiClient) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("LoadData", "Sending requests to server...")
                val tracksResponse = apiClient.trackApiService.getTracks().execute()
                Log.d("LoadData", "Tracks response: $tracksResponse")
                val playlistsResponse = apiClient.playlistApiService.getPlaylists().execute()
                Log.d("LoadData", "Playlists response: $playlistsResponse")

                if (tracksResponse.isSuccessful && playlistsResponse.isSuccessful) {
                    Log.d("LoadData", "Tracks and playlists loaded successfully.")
                    val serverTracks = tracksResponse.body() ?: emptyList()
                    val playlistsFromServer = playlistsResponse.body() ?: emptyList()

                    Log.d("LoadData", "Tracks from server: $serverTracks")

                    val tracksFromServer = serverTracks.map {
                        mapServerTrackToTrack(it, favourite = false, track = 0)
                    }

                    withContext(Dispatchers.Main) {
                        sampleTracks.clear()
                        sampleTracks.addAll(tracksFromServer)
                        sampleTracks.forEach { track ->
                            if(track.title == "Superman") track.track = R.raw.eminem_feat_dina_rae_superman
                            if(track.title == "Mockingbird") track.track = R.raw.eminem_mockingbird
                            if(track.title == "Sweater Weather") track.track = R.raw.the_neighbourhood_sweater_weather
                            if(track.title == "Можно я с тобой") track.track = R.raw.apsent_maybe_i_am_with_you
                        }
                        Log.d("LoadData", "Tracks: $tracksFromServer")
                        Log.d("LoadData", "Sample Tracks: $sampleTracks")
                        samplePlaylists.clear()
                        samplePlaylists.addAll(playlistsFromServer)
                        samplePlaylists.forEach { playlist ->
                            playlist.tracks.fastForEach { track ->
                                if(track.title == "Superman") track.track = R.raw.eminem_feat_dina_rae_superman
                                if(track.title == "Mockingbird") track.track = R.raw.eminem_mockingbird
                                if(track.title == "Sweater Weather") track.track = R.raw.the_neighbourhood_sweater_weather
                                if(track.title == "Можно я с тобой") track.track = R.raw.apsent_maybe_i_am_with_you
                            }
                        }
                    }
                } else {
                    Log.e("LoadData", "Failed to load data: ${tracksResponse.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("LoadData", "Error occurred: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
