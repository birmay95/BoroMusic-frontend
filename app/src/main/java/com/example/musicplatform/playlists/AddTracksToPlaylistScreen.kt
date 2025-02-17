package com.example.musicplatform.playlists

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import com.example.musicplatform.MyViewModel
import com.example.musicplatform.tracks.Playlist
import com.example.musicplatform.R
import com.example.musicplatform.tracks.Track
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
    onShowInfo: (Track) -> Unit
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
        modifier = Modifier.fillMaxSize()
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
            }

            Text(
                text = "Add tracks",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFCACDD2),
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )
        }

        Log.d("Adding", "tracks in playlist: $playlistTracks")
        TrackList(
            tracks = allTracks,
            onTrackClick = onTrackClick,
            onFavouriteToggle = {},
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            isAdding = true,
            isPlaylist = false,
            onAddToPlaylist = { track: Track ->
                Log.d("AddTrackToPlaylist", "Attempting to add track with ID: ${track.id} to playlist with ID: ${playlist.id}")

                // Запускаем корутину для добавления трека в плейлист на сервере
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Проверяем ID плейлиста и трека перед выполнением запроса
                        if (playlist.id == null || track.id == null) {
                            Log.e("AddTrackToPlaylist", "Playlist ID or Track ID is null, cannot proceed with request")
                            return@launch
                        }

                        // Отправляем запрос на добавление трека в плейлист
                        Log.d("AddTrackToPlaylist", "Sending request to add track to playlist...")
                        val response = apiClient.playlistApiService.addTrackToPlaylist(
                            playlist.id, track.id
                        ).execute()

                        Log.d("AddTrackToPlaylist", "Received response for adding track to playlist")

                        if (response.isSuccessful) {
                            // Если запрос успешен, обновляем локальный плейлист
                            response.body()?.let { updatedPlaylist ->
                                Log.d("AddTrackToPlaylist", "Track successfully added to playlist on server")
                                val index = viewModel.samplePlaylists.indexOfFirst { it.id == playlist.id }
                                if (index != -1) {
                                    viewModel.samplePlaylists[index] = updatedPlaylist
                                    Log.d("AddTrackToPlaylist", "Updated local playlist by adding track")
                                } else {
                                    Log.e("AddTrackToPlaylist", "Failed to find playlist with ID ${playlist.id} in samplePlaylists")
                                }
                            } ?: Log.e("AddTrackToPlaylist", "Response body is null after successful addition")
                        } else {
                            // Обработка ошибки, если добавление трека не удалось
                            Log.e("AddTrackToPlaylist", "Error adding track to playlist: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e("AddTrackToPlaylist", "Error occurred while adding track to playlist: ${e.message}")
                    }
                }

                // Локальное добавление трека в плейлист для мгновенного обновления
                playlistTracks.add(track)
            },
            onRemoveFromPlaylist = {},
            playlistTracks = playlistTracks,
            onShowInfo = onShowInfo
        )

    }
}