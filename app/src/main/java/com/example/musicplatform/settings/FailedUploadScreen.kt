package com.example.musicplatform.settings

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.R
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

fun handleApiError(response: Response<*>) {
    val errorMessage = try {
        response.errorBody()?.string() ?: "Unknown error"
    } catch (e: Exception) {
        "Error processing the response"
    }
    Log.e("ArtistRequest", "API error: $errorMessage")
}

fun handleException(exception: Throwable) {
    Log.e("ArtistRequest", "Error: ${exception.message}")
}

@Composable
fun FailedUploadScreen(onCollapse: () -> Unit, user: User, apiClient: ApiClient) {
    var isRequestSent by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        runCatching {
            user.id?.let { apiClient.artistApiService.getRequest(userId = it) }
        }.onSuccess { response ->
            if (response != null && response.isSuccessful) {
                val artistRequest = response.body()!!
                isRequestSent = artistRequest.status
            } else {
                if (response != null) {
                    handleApiError(response)
                    isRequestSent = "NO REQUEST"
                }
            }
        }.onFailure { e ->
            handleException(e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .padding(top = 16.dp)
            .padding(16.dp),
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
                    text = "Upload Track",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFCACDD2),
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(36.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (isRequestSent == "REJECTED") {
            StatusMessage(
                text = "Your request has been rejected.",
                color = Color.Red,
                icon = Icons.Default.Close
            )
        }
        StatusMessage(
            text = "Track downloads are only available for verified artists!",
            color = Color.Yellow,
            icon = Icons.Default.Warning
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isRequestSent == "NO REQUEST" || isRequestSent == "REJECTED") {
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = user.id?.let {
                                apiClient.artistApiService.requestArtist(
                                    it
                                )
                            }
                            if (response != null) {
                                if (response.isSuccessful) {
                                    isRequestSent = "PENDING"
                                } else {
                                    Log.e(
                                        "ArtistRequest",
                                        "Error: ${response.errorBody()?.string()}"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ArtistRequest", "Error: ${e.message}")
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
        } else if (isRequestSent == "PENDING") {
            StatusMessage(
                text = "The application has been sent! Wait for confirmation.",
                color = Color.Green,
                icon = Icons.Default.CheckCircle
            )
        }
    }
}

@Composable
fun StatusMessage(text: String, color: Color, icon: ImageVector) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}