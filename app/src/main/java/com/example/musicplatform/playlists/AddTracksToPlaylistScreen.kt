package com.example.musicplatform.playlists

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.main.MyViewModel
import com.example.musicplatform.model.Playlist
import com.example.musicplatform.R
import com.example.musicplatform.model.Track
import com.example.musicplatform.tracks.TrackList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AddTracksToPlaylistScreen(
    allTracks: List<Track>,
    playlist: Playlist,
    onTrackClick: (Track) -> Unit,
    currentTrack: Track?,
    isPlaying: Boolean,
    onCollapse: () -> Unit,
    viewModel: MyViewModel,
    apiClient: ApiClient,
    onShowInfo: (Track) -> Unit,
    userId: Long
) {
    val playlistTracks = remember {
        mutableStateListOf<Track>().apply {
            addAll(playlist.tracks.map { track ->
                if (track.playlists == null) {
                    track.copy(playlists = mutableListOf(), rotation = Animatable(0f))
                } else {
                    track
                }
            })
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_roll_up),
                        contentDescription = "Roll up",
                        tint = Color(0xFF515A82)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Add tracks to",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFCACDD2),
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFCACDD2),
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(36.dp))

            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Log.d("Adding", "tracks in playlist: $playlistTracks")
        TrackList(
            tracks = allTracks,
            onTrackClick = onTrackClick,
            onFavouriteToggle = {},
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            isAdding = true,
            isPlaylist = false,
            isUserPlaylist = false,
            onAddToPlaylist = { track: Track ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (playlist.id == null || track.id == null) {
                            Log.e(
                                "AddTracksToPlaylist",
                                "Playlist ID or Track ID is null, cannot proceed with request"
                            )
                            return@launch
                        }

                        val response = apiClient.playlistApiService.addTrackToPlaylist(
                            playlist.id, track.id
                        ).execute()

                        if (response.isSuccessful) {
                            response.body()?.let { updatedPlaylist ->
                                var index =
                                    viewModel.sampleUserPlaylists.indexOfFirst { it.id == playlist.id }
                                if (index != -1) {
                                    viewModel.sampleUserPlaylists[index] =
                                        viewModel.updateFavourite(
                                            apiClient,
                                            userId,
                                            updatedPlaylist
                                        )
                                } else {
                                    Log.e(
                                        "AddTracksToPlaylist",
                                        "Failed to find playlist with ID ${playlist.id} in samplePlaylists"
                                    )
                                }
                                index =
                                    viewModel.samplePlaylists.indexOfFirst { it.id == playlist.id }
                                if (index != -1) {
                                    viewModel.samplePlaylists[index] = viewModel.updateFavourite(
                                        apiClient,
                                        userId,
                                        updatedPlaylist
                                    )
                                } else {
                                    Log.e(
                                        "AddTracksToPlaylist",
                                        "Failed to find playlist with ID ${playlist.id} in samplePlaylists"
                                    )
                                }
                            } ?: Log.e(
                                "AddTracksToPlaylist",
                                "Response body is null after successful addition"
                            )
                        } else {
                            Log.e(
                                "AddTracksToPlaylist",
                                "Error adding track to playlist: ${response.errorBody()?.string()}"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "AddTracksToPlaylist",
                            "Error occurred while adding track to playlist: ${e.message}"
                        )
                    }
                }
                playlistTracks.add(track)
            },
            onRemoveFromPlaylist = {},
            playlistTracks = playlistTracks,
            onShowInfo = onShowInfo,
            onShowRecs = {}
        )
    }
}