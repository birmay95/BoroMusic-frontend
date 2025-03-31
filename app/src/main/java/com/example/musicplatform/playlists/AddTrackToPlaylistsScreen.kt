package com.example.musicplatform.playlists

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.main.MyViewModel
import com.example.musicplatform.model.Playlist
import com.example.musicplatform.R
import com.example.musicplatform.tracks.SearchBar
import com.example.musicplatform.model.Track
import com.example.musicplatform.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AddTrackToPlaylistsScreen(
    track: Track?,
    playlists: List<Playlist>,
    onCollapse: () -> Unit,
    viewModel: MyViewModel,
    apiClient: ApiClient,
    user: User
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .padding(8.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
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
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add track to",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFCACDD2),
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "playlist",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFCACDD2),
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(36.dp))

            }
        }
        var searchQuery by remember { mutableStateOf("") }

        val filteredPlaylists = playlists.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
        Spacer(modifier = Modifier.height(16.dp))
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(filteredPlaylists) { playlist ->
                val isUserPlaylist =
                    viewModel.sampleUserPlaylists.any { it.id == playlist.id } || user.roles == "ADMIN"
                if (isUserPlaylist) {
                    PlaylistCard(
                        playlist = playlist,
                        onClick = {
                            if (track != null) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        if (playlist.id == null || track.id == null) {
                                            Log.e(
                                                "AddTrackToPlaylist",
                                                "Playlist ID or Track ID is null, cannot proceed with request"
                                            )
                                            return@launch
                                        }
                                        val response =
                                            apiClient.playlistApiService.addTrackToPlaylist(
                                                playlist.id, track.id
                                            ).execute()
                                        if (response.isSuccessful) {
                                            response.body()?.let { updatedPlaylist ->
                                                val index =
                                                    viewModel.samplePlaylists.indexOfFirst { it.id == playlist.id }
                                                if (index != -1) {
                                                    viewModel.samplePlaylists[index] =
                                                        updatedPlaylist
                                                } else {
                                                    Log.e(
                                                        "AddTrackToPlaylist",
                                                        "Failed to find playlist with ID ${playlist.id} in samplePlaylists"
                                                    )
                                                }
                                            } ?: Log.e(
                                                "AddTrackToPlaylist",
                                                "Response body is null after successful request"
                                            )
                                        } else {
                                            Log.e(
                                                "AddTrackToPlaylist",
                                                "Error adding track to playlist: ${
                                                    response.errorBody()?.string()
                                                }"
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e(
                                            "AddTrackToPlaylist",
                                            "Error occurred while adding track to playlist: ${e.message}"
                                        )
                                    }
                                }
                            } else {
                                Log.e("AddTrackToPlaylist", "Track is null, cannot add to playlist")
                            }
                            onCollapse()
                        },
                        isAdding = true,
                        onRemovePlaylist = {},
                        isUserPlaylist = isUserPlaylist
                    )
                }
            }
        }


    }
}