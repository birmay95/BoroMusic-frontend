package com.example.musicplatform.playlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.main.MyViewModel
import com.example.musicplatform.model.Playlist
import com.example.musicplatform.R
import com.example.musicplatform.model.Track
import com.example.musicplatform.tracks.SearchBar
import com.example.musicplatform.model.User

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onAddPlaylist: () -> Unit,
    onRemovePlaylist: (Playlist) -> Unit,
    currentTrack: Track?,
    isPlaying: Boolean,
    onTrackClick: (Track) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    currentPlaylist: Int,
    onChangeCurrentPlaylist: (Int) -> Unit,
    allTracks: List<Track>,
    onAddToPlaylist: (Track) -> Unit,
    viewModel: MyViewModel,
    apiClient: ApiClient,
    onShowInfo: (Track) -> Unit,
    user: User,
    onShowRecs: (Track) -> Unit
) {
    val playlistNavController = rememberNavController()
    NavHost(navController = playlistNavController, startDestination = "playlists") {
        composable("playlists") {
            var searchQuery by remember { mutableStateOf("") }

            val filteredPlaylists = playlists.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "PLAYLISTS",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFCACDD2),
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        LazyColumn {
                            items(filteredPlaylists) { playlist ->
                                val isUserPlaylist =
                                    viewModel.sampleUserPlaylists.any { it.id == playlist.id } || user.roles == "ADMIN"
                                PlaylistCard(
                                    playlist = playlist,
                                    onClick = {
                                        playlistNavController.navigate("playlist_detail")
                                        onChangeCurrentPlaylist(playlists.indexOf(playlist))
                                    },
                                    isAdding = false,
                                    onRemovePlaylist = onRemovePlaylist,
                                    isUserPlaylist = isUserPlaylist
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = { onAddPlaylist() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = if (currentTrack == null) 75.dp else 150.dp)
                            .padding(16.dp)
                            .size(50.dp),
                        containerColor = Color(0xFF3F4F82)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Add a playlist",
                            tint = Color(0xFF020F17)
                        )
                    }
                }
            }
        }
        composable("playlist_detail") {
            PlaylistDetailScreen(
                playlist = playlists[currentPlaylist],
                onTrackClick = onTrackClick,
                onFavouriteToggle = onFavouriteToggle,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                onCollapse = {
                    playlistNavController.popBackStack()
                },
                allTracks = allTracks,
                onAddToPlaylist = onAddToPlaylist,
                viewModel = viewModel,
                apiClient = apiClient,
                onShowInfo = onShowInfo,
                user = user,
                onShowRecs = onShowRecs
            )
        }
    }
}