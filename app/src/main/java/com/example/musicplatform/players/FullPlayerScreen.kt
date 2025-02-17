package com.example.musicplatform.players

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.musicplatform.tracks.Track
import com.example.musicplatform.Utils

@Composable
fun FullPlayerScreen(
    track: Track,
    onCollapse: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    currentPosition: Long,
    trackDuration: Long,
    onSeekTo: (Int) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    onMixToggle: () -> Unit,
    isRandomMode: Boolean,
    isRepeatTrack: Int,
    onRepeatTrackChange: () -> Unit,
    bufferedPosition: Float
) {
    val swipeThreshold = 60f
    var hasSwiped = false
    var lastDragAmount by remember { mutableFloatStateOf(0f) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D243D))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
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
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 90.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            Log.d("SwipeGesture", "Начало свайпа")
                        },
                        onDragEnd = {
                            Log.d("SwipeGesture", "Завершение свайпа")
                            hasSwiped = false
                            lastDragAmount = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            lastDragAmount += dragAmount
                            Log.d(
                                "SwipeGesture",
                                "dragAmount: $dragAmount, lastDragAmount: $lastDragAmount"
                            )

                            if (!hasSwiped) {
                                if (lastDragAmount > swipeThreshold) {
                                    Log.d(
                                        "SwipeGesture",
                                        "Свайп вправо - переключаем на предыдущий трек"
                                    )
                                    onPrevTrack()
                                    hasSwiped = true
                                } else if (lastDragAmount < -swipeThreshold) {
                                    Log.d(
                                        "SwipeGesture",
                                        "Свайп влево - переключаем на следующий трек"
                                    )
                                    onNextTrack()
                                    hasSwiped = true
                                }
                            }
                            change.consume()
                        }
                    )
                }
            ,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .background(Color(0xFF353D60))
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_music_note),
                    contentDescription = "Music",
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.Center),
                    tint = Color(0xFF0A0E1A)
                )
            }
        }
        Row {
            Column {
                Text(
                    text = track.title,
                    color = Color(0xFFC6CAEB),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp)
                )
                Text(
                    text = track.artist,
                    color = Color(0xFFC6CAEB),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            }
            IconButton(onClick = {
                onFavouriteToggle(track)
            },
                modifier = Modifier.padding(top = 36.dp, start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(if (track.favourite) R.drawable.ic_favourite_true else R.drawable.ic_favorite), // Замените на ваш значок воспроизведения
                    contentDescription = if (track.favourite) "Like" else "Not like",
                    modifier = Modifier
                        .size(30.dp),
                    tint = if (track.favourite) Color(0xFFA7D1DD) else Color(0xFF737BA5)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .padding(horizontal = 16.dp)
        ) {
            // Индикатор загрузки (буферизации)
            LinearProgressIndicator(
                progress = { bufferedPosition },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Gray), // Фон индикатора (фон можно убрать)
                color = Color(0xFF737BA5) // Цвет буферизации
            )

            // Основной ползунок для трека
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { onSeekTo(it.toInt()) },
                valueRange = 0f..maxOf(trackDuration.toFloat(), 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFFA7D1DD),  // Прогресс проигрывания
                    inactiveTrackColor = Color.Transparent // Делаем прозрачным, так как загрузка сверху
                )
            )
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val formattedCurrentPosition = Utils.formatTime(currentPosition)
            val formattedTrackDuration = Utils.formatTime(trackDuration)
            Log.d("loading", "time - $formattedTrackDuration")
            Log.d("loading", "time 2 - $trackDuration")
            Text(
                text = formattedCurrentPosition,
                color = Color(0xFFC6CAEB),
                fontSize = 12.sp
            )
            Text(
                text = formattedTrackDuration,
                color = Color(0xFFC6CAEB),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(90.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    onMixToggle()
                },
                modifier = Modifier.size(50.dp)
            ){
                Icon(
                    painterResource(id = R.drawable.ic_mix),
                    contentDescription = "Mix",
                    modifier = Modifier.size(40.dp),
                    tint = if (isRandomMode) Color(0xFFA7D1DD) else Color(0xFF0A0E1A)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))

            IconButton(
                onClick = onPrevTrack,
                modifier = Modifier.size(50.dp)
            ){
                Icon(
                    painterResource(id = R.drawable.ic_prev),
                    contentDescription = "Prev",
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF0A0E1A)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))

            IconButton (
                onClick = {
                    onPlayPauseClick()
                },
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0xFF8589AC), shape = CircleShape)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.CenterVertically),
                    tint = Color(0xFF0A0E1A)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            IconButton(
                onClick = onNextTrack,
                modifier = Modifier.size(50.dp)
            ){
                Icon(
                    painterResource(id = R.drawable.ic_next),
                    contentDescription = "Next",
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF0A0E1A)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            IconButton(
                onClick = onRepeatTrackChange,
                modifier = Modifier.size(50.dp)
            ){
                Icon(
                    painter = painterResource(
                        when(isRepeatTrack) {
                            0 -> R.drawable.ic_cycle
                            1 -> R.drawable.ic_cycle
                            2 -> R.drawable.ic_cycle_track
                            else -> {
                                R.drawable.ic_cycle
                            }
                        }
                    ),
                    contentDescription =
                    when(isRepeatTrack) {
                        0 -> "Not repeat"
                        1 -> "Repeat artist"
                        2 -> "Repeat track"
                        else -> { "Not repeat" }
                    },
                    modifier = Modifier.size(40.dp),
                    tint =
                    when(isRepeatTrack) {
                        0 -> Color(0xFF0A0E1A)
                        1 -> Color(0xFFA7D1DD)
                        2 -> Color(0xFFA7D1DD)
                        else -> { Color(0xFF0A0E1A) }
                    },
                )
            }
        }
    }
}

//fun formatTime(seconds: Int): String {
//    val minutes = seconds / 60
//    val remainingSeconds = seconds % 60
//    return String.format("%02d:%02d", minutes, remainingSeconds)
//}