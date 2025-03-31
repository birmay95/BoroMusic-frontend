package com.example.musicplatform.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.musicplatform.R
import com.example.musicplatform.model.Track
import com.example.musicplatform.tracks.TrackList

@Composable
fun HomeScreen(
    navController: NavController,
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    currentTrack: Track?,
    isPlaying: Boolean,
    onShowInfo: (Track) -> Unit,
    changeTrackForAddToPlaylist: (Track) -> Unit,
    text: String,
    onShowRecs: (Track) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Spacer(modifier = Modifier.width(30.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFCACDD2),
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { navController.navigate("settings_screen") },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
        }
        TrackList(
            tracks = tracks,
            onTrackClick = onTrackClick,
            onFavouriteToggle = onFavouriteToggle,
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            isAdding = false,
            isPlaylist = false,
            isUserPlaylist = false,
            onAddToPlaylist = { track ->
                changeTrackForAddToPlaylist(track)
                navController.navigate("add_track_to_playlist_screen")
            },
            onRemoveFromPlaylist = {},
            onShowInfo = onShowInfo,
            onShowRecs = onShowRecs
        )
    }
}