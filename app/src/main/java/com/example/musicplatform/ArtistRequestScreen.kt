package com.example.musicplatform

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.musicplatform.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ArtistRequestScreen(userId: Long, apiClient: ApiClient, onCollapse: () -> Unit) {
    var isRequestSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_roll_up),
                        contentDescription = "Roll up",
                        tint = Color(0xFF515A82)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Submit the request",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFCACDD2),
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(36.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isRequestSent) {
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = apiClient.artistApiService.requestArtist(userId)
                            if (response.isSuccessful) {
                                isRequestSent = true
                            } else {
                                Log.e("ArtistRequest", "Ошибка: ${response.errorBody()?.string()}")
                            }
                        } catch (e: Exception) {
                            Log.e("ArtistRequest", "Ошибка: ${e.message}")
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
                    .animateContentSize(),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContentColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Submit a request",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        } else {
            Text("Заявка отправлена! Ожидайте подтверждения.", color = Color.Green)
            onCollapse()
        }
    }
}
