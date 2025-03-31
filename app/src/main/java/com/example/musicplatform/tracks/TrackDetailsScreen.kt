package com.example.musicplatform.tracks

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.R
import com.example.musicplatform.model.Track

@SuppressLint("DefaultLocale")
@Composable
fun TrackDetailsScreen(
    track: Track,
    onBack: () -> Unit
) {
    val fileSizeMb =
        remember(track.fileSize) { String.format("%.2f MB", track.fileSize / (1024.0 * 1024.0)) }
    val durationFormatted = remember(track.duration) {
        val minutes = track.duration / 60
        val seconds = track.duration % 60
        String.format("%d:%02d", minutes, seconds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                text = "Track Details",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFCACDD2),
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(36.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D243D)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF353D60)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_music_note),
                        contentDescription = "Album Art",
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = track.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = track.artist,
                    color = Color(0xFF8589AC),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF353D60), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                DetailItem(label = "Album", value = track.album)
                DetailItem(label = "Content Type", value = track.contentType)
                DetailItem(label = "File Size", value = fileSizeMb)
                DetailItem(label = "Duration", value = durationFormatted)
                if (track.genres.isNotEmpty()) {
                    DetailItem(label = "Genres", value = track.genres.joinToString { it.name })
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 12.sp,
            color = Color(0xFF8589AC)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFF353D60), thickness = 0.5.dp)
    }
}
