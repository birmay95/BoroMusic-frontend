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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.musicplatform.model.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isAdding: Boolean,
    isPlaylist: Boolean,
    isUserPlaylist: Boolean,
    onAddToPlaylist: (Track) -> Unit,
    onRemoveFromPlaylist: (Track) -> Unit,
    onShowInfo: (Track) -> Unit,
    onShowRecs: (Track) -> Unit
) {

    var isSheetOpen by remember { mutableStateOf(false) }

    val rotation = remember { Animatable(track.rotation?.value ?: 0f) }

    LaunchedEffect(isCurrent, isPlaying) {
        if (isCurrent) {
            if (isPlaying) {
                if (!rotation.isRunning) {
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
                rotation.stop()
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
                        onLongPress = { isSheetOpen = true }
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
            if (!isAdding) {
                IconButton(onClick = {
                    track.rotation = rotation
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
                    onClick = { isSheetOpen = true }
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
        if (isSheetOpen) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { isSheetOpen = false },
                sheetState = sheetState,
                containerColor = Color(0xFF1D243D)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_music_note),
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFF353D60), shape = CircleShape)
                                .padding(8.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = track.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = track.artist,
                                color = Color(0xFF8589AC),
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFF353D60))
                    Spacer(modifier = Modifier.height(12.dp))
                    MenuItem(if (!track.favourite) "Add to favourites" else "Remove from favourite") {
                        onFavouriteToggle(track)
                        isSheetOpen = false
                    }
                    MenuItem(if (!isPlaylist) "Add to playlist" else "Add to another playlist") {
                        onAddToPlaylist(track)
                        isSheetOpen = false
                    }
                    if (isPlaylist && isUserPlaylist) {
                        MenuItem("Remove from playlist") {
                            onRemoveFromPlaylist(track)
                            isSheetOpen = false
                        }
                    }
                    MenuItem("More detailed") {
                        onShowInfo(track)
                        isSheetOpen = false
                    }
                    MenuItem("Recommendations") {
                        onShowRecs(track)
                        isSheetOpen = false
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0xFF353D60), shape = RoundedCornerShape(8.dp))
    ) {
        Text(text, color = Color(0xFFCACDD2), fontSize = 16.sp)
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