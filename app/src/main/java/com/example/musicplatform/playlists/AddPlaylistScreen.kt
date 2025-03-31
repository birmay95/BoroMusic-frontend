package com.example.musicplatform.playlists

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.R

@Composable
fun AddPlaylistScreen(
    onCollapse: () -> Unit,
    onCreatePlaylist: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .padding(top = 32.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onCollapse, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_roll_up),
                        contentDescription = "Roll up",
                        tint = Color(0xFF515A82)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Creating a Playlist",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFCACDD2),
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(44.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        var playlistName by remember { mutableStateOf("") }
        TextField(
            value = playlistName,
            onValueChange = {
                if (it.length <= 25) {
                    playlistName = it
                }
            },
            placeholder = { Text("Enter playlist name") },
            modifier = Modifier
                .padding(horizontal = 48.dp)
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0A0E1A),
                unfocusedContainerColor = Color(0xFF0A0E1A),
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        var playlistDescription by remember { mutableStateOf("") }
        TextField(
            value = playlistDescription,
            onValueChange = {
                if (it.length <= 100) {
                    playlistDescription = it
                }
            },
            placeholder = { Text("Enter playlist description") },
            modifier = Modifier
                .padding(horizontal = 48.dp)
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0A0E1A),
                unfocusedContainerColor = Color(0xFF0A0E1A),
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(32.dp))
        val isPressed = remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed.value) 0.95f else 1f,
            label = ""
        )
        val buttonColor by animateColorAsState(
            targetValue = if (isPressed.value) Color.Gray else Color.Transparent,
            label = ""
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .height(45.dp)
                    .width(160.dp)
                    .border(2.dp, Color(0xFFCACDD2), CircleShape)
                    .background(buttonColor, CircleShape)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed.value = true
                                tryAwaitRelease()
                                isPressed.value = false
                            },
                            onTap = {
                                if (playlistName.isNotBlank()) {
                                    onCreatePlaylist(playlistName, playlistDescription)
                                    playlistName = ""
                                    playlistDescription = ""
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Add track to playlist",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFCACDD2)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Create",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCACDD2),
                        fontSize = 18.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}