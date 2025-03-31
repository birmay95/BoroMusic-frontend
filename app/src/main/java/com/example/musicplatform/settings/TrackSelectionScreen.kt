package com.example.musicplatform.settings

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.main.MyViewModel
import com.example.musicplatform.R
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.model.Playlist
import com.example.musicplatform.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("MutableCollectionMutableState")
@Composable
fun TrackSelectionScreen(apiClient: ApiClient, onCollapse: () -> Unit, viewModel: MyViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var trackList by remember { mutableStateOf(viewModel.sampleTracks.toMutableList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .padding(top = 32.dp)
            .padding(8.dp),
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
                IconButton(onClick = onCollapse, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_roll_up),
                        contentDescription = "Roll up",
                        tint = Color(0xFF515A82)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Select track to remove",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFCACDD2),
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(36.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn {
            items(trackList) { track ->
                TrackItem(track) {
                    coroutineScope.launch {
                        track.id?.let { trackId ->
                            try {
                                val response = withContext(Dispatchers.IO) {
                                    apiClient.trackApiService.deleteTrack(trackId).execute()
                                }
                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful) {
                                        val responseBody =
                                            response.body()?.string() ?: "Unknown response"
                                        Log.d(
                                            "TrackSelectionScreen",
                                            "Server response: $responseBody"
                                        )

                                        trackList =
                                            trackList.filterNot { it.id == trackId }.toMutableList()
                                        viewModel.sampleTracks.removeIf { it.id == track.id }
                                        viewModel.favouriteTracks.removeIf { it.id == track.id }
                                        viewModel.samplePlaylists.forEach { playlist: Playlist ->
                                            playlist.tracks.removeIf { it.id == track.id }
                                        }
                                        viewModel.sampleUserPlaylists.forEach { playlist ->
                                            playlist.tracks.removeIf { it.id == track.id }
                                        }
                                        Toast.makeText(context, "Track removed", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        val errorBody =
                                            response.errorBody()?.string() ?: "Unknown error"
                                        Log.e("TrackSelectionScreen", "Deletion error: $errorBody")
                                        Toast.makeText(
                                            context,
                                            "Failed to remove track",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("TrackSelectionScreen", "Error when deleting: ${e.message}")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun TrackItem(track: Track, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFF1D243D), shape = RoundedCornerShape(8.dp))
            .clickable { onDelete() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_music_note),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF353D60), shape = CircleShape)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                color = Color(0xFFCACDD2),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = track.artist,
                color = Color(0xFF8589AC),
                fontSize = 12.sp
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = "Delete track",
                modifier = Modifier.size(32.dp),
                tint = Color(0xFF293A65)
            )
        }
    }
}