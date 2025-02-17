package com.example.musicplatform.playlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.MyViewModel
import com.example.musicplatform.tracks.Playlist
import com.example.musicplatform.R
import com.example.musicplatform.tracks.Track
import com.example.musicplatform.tracks.SearchBar

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
    onShowInfo: (Track) -> Unit
) {
    var currentScreen by remember { mutableIntStateOf(0) }

    if (currentScreen == 0) {
        var searchQuery by remember { mutableStateOf("") }

        val filteredPlaylists = playlists.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }

        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
        )

        Box(modifier = Modifier
            .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {


                LazyColumn {
                    items(filteredPlaylists) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = {
                                currentScreen = 1
                                onChangeCurrentPlaylist(playlists.indexOf(playlist))
                            },
                            isAdding = false,
                            onRemovePlaylist = onRemovePlaylist
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { onAddPlaylist() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(50.dp),
                containerColor = Color(0xFF3F4F82)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Добавить плейлист",
                    tint = Color(0xFF020F17)
                )
            }
        }
    } else {
        PlaylistDetailScreen(
            playlist = playlists[currentPlaylist],
            onTrackClick = onTrackClick,
            onFavouriteToggle = onFavouriteToggle,
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            onCollapse = {
                currentScreen = 0
            },
            allTracks = allTracks,
            onAddToPlaylist = onAddToPlaylist,
            viewModel = viewModel,
            apiClient = apiClient,
            onShowInfo = onShowInfo
        )
    }
}