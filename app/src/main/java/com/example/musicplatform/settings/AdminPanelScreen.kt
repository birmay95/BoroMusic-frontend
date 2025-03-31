package com.example.musicplatform.settings

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.musicplatform.R
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.model.ArtistRequest
import com.example.musicplatform.tracks.SearchBar

@Composable
fun AdminPanelScreen(apiClient: ApiClient, onCollapse: () -> Unit) {
    val requests = remember { mutableStateListOf<ArtistRequest>() }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiClient.artistApiService.getAllRequests()
                if (response.isSuccessful) {
                    response.body()?.let { fetchedRequests ->
                        requests.clear()
                        requests.addAll(fetchedRequests)
                    }
                } else {
                    Log.e("AdminPanel", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("AdminPanel", "Error: ${e.message}")
            }
        }
    }

    val filteredRequests = requests.filter {
        it.user.username.contains(searchQuery, ignoreCase = true) ||
                it.user.email.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
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
                    text = "Admin Panel",
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

        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(filteredRequests) { request ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val y = size.height - strokeWidth
                            drawLine(
                                color = Color.Gray,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                        .animateContentSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "User: ${request.user.username}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Email: ${request.user.email}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (request.status == "PENDING") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            request.id?.let {
                                                apiClient.artistApiService.approveArtist(it)
                                            }
                                        }
                                        requests.remove(request)
                                        request.status = "APPROVE"
                                        requests.add(request)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "Approve",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.weight(0.3f))
                                Button(
                                    onClick = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            request.id?.let {
                                                apiClient.artistApiService.rejectArtist(it)
                                            }
                                        }
                                        requests.remove(request)
                                        request.status = "REJECT"
                                        requests.add(request)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "Reject",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = request.status,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

