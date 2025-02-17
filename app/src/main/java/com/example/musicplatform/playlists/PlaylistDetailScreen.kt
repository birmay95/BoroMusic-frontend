package com.example.musicplatform.playlists

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.MyViewModel
import com.example.musicplatform.tracks.Playlist
import com.example.musicplatform.R
import com.example.musicplatform.tracks.Track
import com.example.musicplatform.tracks.TrackList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    onTrackClick: (Track) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    currentTrack: Track?,
    isPlaying: Boolean,
    onCollapse: () -> Unit,
    allTracks: List<Track>,
    onAddToPlaylist: (Track) -> Unit,
    viewModel: MyViewModel,
    apiClient: ApiClient,
    onShowInfo: (Track) -> Unit
) {
    var isAddingTracksScreen by remember { mutableStateOf(false) }

    if(!isAddingTracksScreen) {
        Column(
            modifier = Modifier.fillMaxSize()
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        painterResource(id = R.drawable.ic_roll_up),
                        contentDescription = "Roll up",
                        tint = Color(0xFF515A82)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(
                        painterResource(id = R.drawable.ic_share),
                        contentDescription = "Share",
                        Modifier.size(32.dp),
                        tint = Color(0xFF515A82)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFCACDD2),
                    fontSize = 28.sp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                playlist.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFCACDD2),
                        fontSize = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val isPressed = remember { mutableStateOf(false) }
            val scale by animateFloatAsState(targetValue = if (isPressed.value) 0.95f else 1f)
            val buttonColor by animateColorAsState(targetValue = if (isPressed.value) Color.Gray else Color.Transparent)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .width(160.dp)
                        .border(2.dp, Color(0xFFCACDD2), CircleShape)
                        .background(buttonColor, CircleShape)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed.value = true
                                    tryAwaitRelease()
                                    isPressed.value = false
                                },
                                onTap = { isAddingTracksScreen = true }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Add track to playlist",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFFCACDD2)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCACDD2),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            TrackList(
                tracks = playlist.tracks,
                onTrackClick = onTrackClick,
                onFavouriteToggle = onFavouriteToggle,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                isAdding = false,
                isPlaylist = true,
                onAddToPlaylist = onAddToPlaylist,
                onRemoveFromPlaylist = { track: Track ->
                    Log.d("RemoveTrackFromPlaylist", "Attempting to remove track with ID: ${track.id} from playlist with ID: ${playlist.id}")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (playlist.id == null || track.id == null) {
                                Log.e("RemoveTrackFromPlaylist", "Playlist ID or Track ID is null, cannot proceed with request")
                                return@launch
                            }

                            Log.d("RemoveTrackFromPlaylist", "Sending request to remove track...")
                            val response = apiClient.playlistApiService.removeTrackFromPlaylist(
                                playlist.id, track.id
                            ).execute()

                            Log.d("RemoveTrackFromPlaylist", "Received response for removing track from playlist")

                            if (response.isSuccessful) {
                                response.body()?.let { updatedPlaylist ->
                                    Log.d("RemoveTrackFromPlaylist", "Track successfully removed from playlist on server")
                                    val index = viewModel.samplePlaylists.indexOfFirst { it.id == playlist.id }
                                    if (index != -1) {
                                        viewModel.samplePlaylists[index] = updatedPlaylist
                                        Log.d("RemoveTrackFromPlaylist", "Updated local playlist by removing track")
                                    } else {
                                        Log.e("RemoveTrackFromPlaylist", "Failed to find playlist with ID ${playlist.id} in samplePlaylists")
                                    }
                                } ?: Log.e("RemoveTrackFromPlaylist", "Response body is null after successful removal")
                            } else {
                                Log.e("RemoveTrackFromPlaylist", "Error removing track from playlist: ${response.errorBody()?.string()}")
                            }
                        } catch (e: Exception) {
                            Log.e("RemoveTrackFromPlaylist", "Error occurred while removing track from playlist: ${e.message}")
                        }
                    }
                },
                onShowInfo = onShowInfo
            )

        }
    } else {
        AddTracksToPlaylistScreen(
            allTracks = allTracks,
            playlist = playlist,
            onTrackClick = onTrackClick,
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            onCollapse = { isAddingTracksScreen = false },
            viewModel = viewModel,
            apiClient = apiClient,
            onShowInfo = onShowInfo
        )
    }
}