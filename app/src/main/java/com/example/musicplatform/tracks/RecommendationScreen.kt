package com.example.musicplatform.tracks

import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.R
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.main.MyViewModel
import com.example.musicplatform.model.Track

@Composable
fun RecommendationScreen(
    viewModel: MyViewModel,
    track: Track,
    apiClient: ApiClient,
    onBack: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    currentTrack: Track?,
    isPlaying: Boolean,
    isAdding: Boolean,
    isUserPlaylist: Boolean,
    isPlaylist: Boolean,
    onAddToPlaylist: (Track) -> Unit,
    onRemoveFromPlaylist: (Track) -> Unit,
    onShowInfo: (Track) -> Unit,
    onShowRecs: (Track) -> Unit
) {

    LaunchedEffect(Unit) {
        viewModel.fetchRecommendations(apiClient, trackId = track.id.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_roll_up),
                        contentDescription = "Back",
                        tint = Color(0xFF515A82)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Recommendations from ${track.title}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFCACDD2),
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(36.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TrackList(
            tracks = viewModel.recommendations,
            onTrackClick = onTrackClick,
            onFavouriteToggle = onFavouriteToggle,
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            isAdding = isAdding,
            isUserPlaylist = isUserPlaylist,
            isPlaylist = isPlaylist,
            onAddToPlaylist = onAddToPlaylist,
            onRemoveFromPlaylist = onRemoveFromPlaylist,
            onShowInfo = onShowInfo,
            onShowRecs = onShowRecs
        )
    }
}