package com.example.musicplatform

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
fun ArtistRequestScreen(userId: Long, apiClient: ApiClient, onCollapse: () -> Unit) {
    var isRequestSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Подать заявку на роль артиста", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        if (!isRequestSent) {
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = apiClient.artistApiService.requestArtist(userId)
                        if (response.isSuccessful) {
                            isRequestSent = true
                        }
                    } catch (e: Exception) {
                        Log.e("ArtistRequest", "Ошибка: ${e.message}")
                    }
                }
            }) {
                Text("Отправить заявку")
            }
        } else {
            Text("Заявка отправлена! Ожидайте подтверждения.", color = Color.Green)
            onCollapse()
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onCollapse() }) {
            Text("Назад")
        }
    }
}
