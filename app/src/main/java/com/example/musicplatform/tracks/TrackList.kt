package com.example.musicplatform.tracks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun TrackList(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    currentTrack: Track?,
    isPlaying: Boolean,
    isAdding: Boolean,
    isPlaylist: Boolean,
    onAddToPlaylist: (Track) -> Unit,
    onRemoveFromPlaylist: (Track) -> Unit,
    playlistTracks: List<Track> = emptyList(),
    onShowInfo: (Track) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredTracks by remember(tracks, searchQuery, isAdding, playlistTracks) {
        derivedStateOf {
            tracks.filter { track ->
                val matchesSearch = track.title.contains(searchQuery, ignoreCase = true) ||
                        track.artist.contains(searchQuery, ignoreCase = true)
                val notInPlaylist = playlistTracks.none { it.id == track.id }
                matchesSearch && (!isAdding || notInPlaylist)
            }
        }
    }

    Column {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(filteredTracks) { track ->
                TrackItem(
                    track = track,
                    onClick = { onTrackClick(track) },
                    onFavouriteToggle = onFavouriteToggle,
                    isCurrent = currentTrack == track,
                    isPlaying = isPlaying,
                    isAdding = isAdding,
                    isPlaylist = isPlaylist,
                    onAddToPlaylist = onAddToPlaylist,
                    onRemoveFromPlaylist = onRemoveFromPlaylist,
                    onShowInfo = onShowInfo
                )
            }
        }
    }
}