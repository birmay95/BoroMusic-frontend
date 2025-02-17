package com.example.musicplatform.tracks

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.R

@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isAdding: Boolean,
    isPlaylist: Boolean,
    onAddToPlaylist: (Track) -> Unit,
    onRemoveFromPlaylist: (Track) -> Unit,
    onShowInfo: (Track) -> Unit
) {

    var isMenuExpanded by remember { mutableStateOf(false) }

    val rotation = remember { Animatable(track.rotation?.value ?: 0f) }

    LaunchedEffect(isCurrent, isPlaying) {
        if (isCurrent) {
            if (isPlaying) {
                if (!rotation.isRunning) {  // Запускаем, только если не работает
                    Log.d("rotationnn", "track rotation 1: ${rotation.value}")
                    rotation.animateTo(
                        targetValue = rotation.value + 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )
                }
            } else {
                rotation.stop()  // Останавливаем
            }
        } else {
            track.rotation = Animatable(0f)
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(500)
            )
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isCurrent) 1.3f else 1f,
        animationSpec = if (isCurrent) {
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(500)
        }, label = ""
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { isMenuExpanded = !isMenuExpanded }
                    )
                }
                .background(Color(0xFF1D243D), shape = RoundedCornerShape(8.dp))
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
                    .rotate(rotation.value)
                    .scale(scale),
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
            if(!isAdding){
                IconButton(onClick = {
                    track.rotation = rotation
                    Log.d("rotationnn", "track rotation 2: ${track.rotation.value}")
                    Log.d("rotationnn", "track rotation 3: ${rotation.value}")
                    onFavouriteToggle(track)
                }) {
                    Icon(
                        painter = painterResource(if (track.favourite) R.drawable.ic_favourite_true else R.drawable.ic_favorite),
                        contentDescription = if (track.favourite) "Like" else "Not like",
                        modifier = Modifier.size(35.dp),
                        tint = if (track.favourite) Color(0xFFA7D1DD) else Color(0xFF737BA5)
                    )
                }
                IconButton(
                    onClick = { isMenuExpanded = !isMenuExpanded }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_threedots),
                        contentDescription = "Menu bar",
                        modifier = Modifier.size(30.dp),
                        tint = Color(0xFF737BA5)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .border(3.dp, Color(0xFFCACDD2), CircleShape)
                        .clickable(onClick = {
                            onAddToPlaylist(track)
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Add track to playlist",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFCACDD2)
                    )
                }
            }
        }
        if(isMenuExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(
                        Color(0xFF1D243D),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                TextButton(
                    onClick = {
                        onFavouriteToggle(track)
                        isMenuExpanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .background(
                            Color(0xFF353D60),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        if(!track.favourite) "Add to favourites" else "Remove from favourite",
                        color = Color(0xFFCACDD2),
                        fontSize = 16.sp
                    )
                }
                TextButton(
                    onClick = {
                        onAddToPlaylist(track)
                        isMenuExpanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .background(
                            Color(0xFF353D60),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        if(!isPlaylist) "Add to playlist" else "Add to another playlist",
                        color = Color(0xFFCACDD2),
                        fontSize = 16.sp
                    )
                }
                if(isPlaylist) {
                    TextButton(
                        onClick = {
                            onRemoveFromPlaylist(track)
                            isMenuExpanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                            .background(
                                Color(0xFF353D60),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            "Remove from playlist",
                            color = Color(0xFFCACDD2),
                            fontSize = 16.sp
                        )
                    }
                }
                TextButton(
                    onClick = {
                        onShowInfo(track)
                        isMenuExpanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .background(
                            Color(0xFF353D60),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        "Get info",
                        color = Color(0xFFCACDD2),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text(text = "Search...", color = Color(0xFFC6CAEB)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFF303147), shape = RoundedCornerShape(8.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF515A82),
            unfocusedContainerColor = Color(0xFF353D60),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFFC6CAEB)
        )
    )
}