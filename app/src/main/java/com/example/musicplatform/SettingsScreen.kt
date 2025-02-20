package com.example.musicplatform

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.tracks.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            fileName = cursor.getString(nameIndex)
        }
    }
    return fileName
}


fun uploadTrack(context: Context, uri: Uri, apiClient: ApiClient, viewModel: MyViewModel, userId: Long) {
    val contentResolver = context.contentResolver
    val fileName = getFileName(context, uri) ?: "unknown.mp3"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Открываем поток и читаем файл
            val inputStream = contentResolver.openInputStream(uri) ?: throw IOException("Не удалось открыть файл")
            val requestBody = inputStream.readBytes().toRequestBody("audio/mpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

            // Выполняем запрос
            val response = apiClient.trackApiService.uploadFile(userId, part).execute()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    response.body()?.let { uploadedTrack ->
                        // Добавляем загруженный трек в список
                        viewModel.sampleTracks.add(uploadedTrack)
                        Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("UploadTrack", "Ошибка загрузки: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("UploadTrack", "Ошибка при загрузке трека: ${e.message}")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onLogoutSuccess: () -> Unit,
    apiClient: ApiClient,
    onCollapse: () -> Unit,
    viewModel: MyViewModel,
    user: User
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { user.id?.let { it1 -> uploadTrack(context, it, apiClient, viewModel, it1) } }
    }
    var isFailedUploadScreen by remember { mutableStateOf(false) }
    var isAdminPanelScreen by remember { mutableStateOf(false) }
    if(!isAdminPanelScreen){
        if (!isFailedUploadScreen) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0E1A))
                    .padding(8.dp),
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
                            text = "Settings",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFCACDD2),
                            fontSize = 26.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        apiClient.logout(
                            onSuccess = {
                                onLogoutSuccess()
                            },
                            onError = { error ->
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        context,
                                        "Logout failed: $error",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                        )
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
                            text = "Logout from the account",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Кнопка для загрузки трека
//        Button(
//            onClick = { launcher.launch("audio/*") }, // Открываем файловый менеджер
//            shape = RoundedCornerShape(8.dp),
//            modifier = Modifier.fillMaxWidth(),
//            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
//        ) {
//            Text(text = "Upload Track", fontSize = 16.sp)
//        }
                Button(
                    onClick = {
                        if (!(user.roles == "ARTIST" || user.roles == "ADMIN")) {
                            isFailedUploadScreen = true
                        } else {
                            launcher.launch("audio/*")
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
                            text = "Upload Track",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                if(user.roles == "ADMIN") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isAdminPanelScreen = true
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
                                text = "Check requests",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            FailedUploadScreen(
                onCollapse = { isFailedUploadScreen = false },
                user = user,
                apiClient = apiClient
            )
        }
    } else {
        AdminPanelScreen(apiClient = apiClient, onCollapse = { isAdminPanelScreen = false })
    }
}

@Composable
fun FailedUploadScreen(onCollapse: () -> Unit, user: User, apiClient: ApiClient) {
    var isRequestSent by remember { mutableStateOf(apiClient.getArtistRequest()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
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
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Загрузка треков доступна только для верифицированных артистов!",
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (!isRequestSent) {
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
                                    isRequestSent = true
                                    apiClient.saveArtistRequest(true)
                                } else {
                                    Log.e(
                                        "ArtistRequest",
                                        "Ошибка: ${response.errorBody()?.string()}"
                                    )
                                }
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
        }
    }
}