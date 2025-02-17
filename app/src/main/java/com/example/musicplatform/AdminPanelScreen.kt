package com.example.musicplatform

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.tracks.ArtistRequest

@Composable
fun AdminPanelScreen(apiClient: ApiClient, onCollapse: () -> Unit) {
    val requests = remember { mutableStateListOf<ArtistRequest>() }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiClient.artistApiService.getAllRequests()
                Log.d("AdminPanel", "$response")
                if (response.isSuccessful) {
                    response.body()?.let { fetchedRequests ->
                        requests.clear()
                        requests.addAll(fetchedRequests)
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminPanel", "Ошибка: ${e.message}")
            }
        }
    }

    Column {
        Button(onClick = { onCollapse() }) {
            Text("Назад")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(requests) { request ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("User: ${request.user.username}", fontWeight = FontWeight.Bold)
                        Text("Status: ${request.status}")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    request.id?.let { apiClient.artistApiService.approveArtist(it) }
                                }
                            }) {
                                Text("Одобрить")
                            }

                            Button(onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    request.id?.let { apiClient.artistApiService.rejectArtist(it) }
                                }
                            }) {
                                Text("Отклонить")
                            }
                        }
                    }
                }
            }
        }
    }
}
