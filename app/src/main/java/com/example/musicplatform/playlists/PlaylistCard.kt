package com.example.musicplatform.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.musicplatform.model.Playlist
import com.example.musicplatform.R

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    isAdding: Boolean,
    onRemovePlaylist: (Playlist) -> Unit,
    isUserPlaylist: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF5D84A5)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_music_note),
                contentDescription = "Playlist Icon",
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF293A65)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF020F17)
                )
                Text(
                    text = "${playlist.tracks.size} треков",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF282B32)
                )
            }
            if (!isAdding && isUserPlaylist) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemovePlaylist(playlist) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Delete playlist",
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFF293A65)
                    )
                }
            }
        }
    }
}