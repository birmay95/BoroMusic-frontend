package com.example.musicplatform.tracks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.musicplatform.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UploadTrackScreen(user: User, navController: NavController, apiClient: ApiClient) {
    var trackTitle by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    if (user.roles != "ARTIST" || !user.isVerified) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Загрузка треков доступна только для верифицированных артистов!", color = Color.Red)
            Button(onClick = { navController.popBackStack() }) {
                Text("Назад")
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Загрузка трека", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = trackTitle,
                onValueChange = { trackTitle = it },
                label = { Text("Название трека") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                isUploading = true
//                CoroutineScope(Dispatchers.IO).launch {
//                    val response = user.id?.let { apiClient.trackApiService.uploadFile(it, Track(trackTitle)) }
//                    message = if (response.isSuccessful) "Трек загружен!" else "Ошибка!"
//                    isUploading = false
//                }
            }) {
                if (isUploading) {
                    CircularProgressIndicator()
                } else {
                    Text("Загрузить")
                }
            }

            if (message.isNotEmpty()) {
                Text(message, color = Color.Green, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
