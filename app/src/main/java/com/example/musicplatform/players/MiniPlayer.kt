package com.example.musicplatform.players

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.R
import com.example.musicplatform.model.Track
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(
    track: Track,
    onExpandClick: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    currentTime: Long,
    trackDuration: Long,
    onFavouriteToggle: (Track) -> Unit,
    modifier: Modifier
) {
    val swipeThreshold = 100f
    var hasSwiped by remember { mutableStateOf(false) }

    LaunchedEffect(hasSwiped) {
        if (hasSwiped) {
            delay(300)
            hasSwiped = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 75.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF353D60))
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable(
                    onClick = { onExpandClick() }
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { hasSwiped = false },
                        onDragEnd = { hasSwiped = false },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!hasSwiped) {
                                if (dragAmount > swipeThreshold) {
                                    onPrevTrack()
                                    hasSwiped = true
                                } else if (dragAmount < -swipeThreshold) {
                                    onNextTrack()
                                    hasSwiped = true
                                }
                            }
                        }
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_music_note),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF0A0E1A)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = Color(0xFFC6CAEB),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = track.artist, color = Color(0xFFC6CAEB), fontSize = 12.sp)
            }
            Row {
                IconButton(onClick = {
                    onPlayPauseClick()
                }) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        Modifier.size(35.dp),
                        tint = Color(0xFF737BA5)
                    )
                }
                IconButton(onClick = {
                    onFavouriteToggle(track)
                }) {
                    Icon(
                        painter = painterResource(if (track.favourite) R.drawable.ic_favourite_true else R.drawable.ic_favorite),
                        contentDescription = if (track.favourite) "Like" else "Not like",
                        Modifier.size(30.dp),
                        tint = if (track.favourite) Color(0xFFA7D1DD) else Color(0xFF737BA5)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { currentTime.toFloat() / trackDuration.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF8589AC)),
        )
    }
}

