package com.example.musicplatform.settings

import android.app.usage.StorageStatsManager
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.musicplatform.main.MyViewModel
import com.example.musicplatform.R
import com.example.musicplatform.authorization.EmailVerificationScreen
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.io.File
import java.util.UUID

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

fun uploadTrack(
    context: Context,
    uri: Uri,
    apiClient: ApiClient,
    viewModel: MyViewModel,
    userId: Long
) {
    val contentResolver = context.contentResolver
    val fileName = getFileName(context, uri) ?: "unknown.mp3"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw IOException("Couldn't open the file")
            val requestBody =
                inputStream.readBytes().toRequestBody("audio/mpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

            val userIdPart = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiClient.trackApiService.uploadFile(userIdPart, part).execute()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    response.body()?.let { uploadedTrack ->
                        viewModel.sampleTracks.add(uploadedTrack)
                        Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(
                        "UploadTrack",
                        "Download error: Code ${response.code()}, Response: $errorMessage"
                    )
                    Toast.makeText(context, "Upload failed: $errorMessage", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: Exception) {
            Log.e("UploadTrack", "Error when uploading a track: ${e.message}", e)
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

    val settingsNavController = rememberNavController()
    NavHost(navController = settingsNavController, startDestination = "settings_menu") {
        composable("settings_menu") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0E1A))
                    .padding(top = 16.dp)
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
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
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(36.dp))
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
                Button(
                    onClick = {
                        if (!(user.roles == "ARTIST" || user.roles == "ADMIN")) {
                            settingsNavController.navigate("failed_upload_screen")
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
                            text = "Upload track",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                if (user.roles == "ADMIN") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            settingsNavController.navigate("remove_track_screen")
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
                                text = "Remove track",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                if (user.roles == "ADMIN") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            settingsNavController.navigate("admin_panel")
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
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { settingsNavController.navigate("security_screen") },
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
                            text = "Security & Privacy",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                val appSize = remember { mutableStateOf("Loading...") }
                var cacheSize by remember { mutableLongStateOf(getCacheSize(context)) }
                val dataSize = remember { mutableStateOf("Loading...") }

                LaunchedEffect(Unit) {
                    appSize.value = formatSize(getAppSize(context))
                    dataSize.value = formatSize(getDataSize(context))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Storage information:",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                StorageInfoItem(
                    icon = Icons.Default.Info,
                    title = "Total App Size",
                    size = appSize.value
                )
                StorageInfoItem(
                    icon = Icons.Default.Info,
                    title = "Cache Size",
                    size = formatSize(cacheSize)
                )
                StorageInfoItem(
                    icon = Icons.Default.Info,
                    title = "User Data Size",
                    size = dataSize.value
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        clearCache(context)
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1000)
                            cacheSize = getCacheSize(context)
                            Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
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
                            text = "Remove cache",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        clearUserData(context)
                        Toast.makeText(context, "User data removed", Toast.LENGTH_SHORT).show()
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
                            text = "Remove user data",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                var showDialog by remember { mutableStateOf(false) }
                var isLoading by remember { mutableStateOf(false) }
                Button(
                    onClick = { settingsNavController.navigate("support_screen") },
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
                            text = "Support",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        showDialog = true
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
                    ),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (isLoading) "Deleting..." else "Delete account",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Confirm Deletion") },
                        text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDialog = false
                                    isLoading = true
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val result = user.id?.let { it1 ->
                                            deleteAccount(
                                                it1,
                                                apiClient,
                                                context,
                                                onLogoutSuccess
                                            )
                                        }
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, result, Toast.LENGTH_SHORT)
                                                .show()
                                            isLoading = false
                                        }
                                    }
                                }
                            ) {
                                Text("Delete", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showDialog = false
                            }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
        composable("failed_upload_screen") {
            FailedUploadScreen(
                onCollapse = {
                    settingsNavController.popBackStack()
                },
                user = user,
                apiClient = apiClient
            )
        }
        composable("admin_panel") {
            AdminPanelScreen(
                apiClient = apiClient,
                onCollapse = {
                    settingsNavController.popBackStack()
                })
        }
        composable("remove_track_screen") {
            TrackSelectionScreen(
                apiClient = apiClient,
                onCollapse = { settingsNavController.popBackStack() },
                viewModel = viewModel
            )
        }
        composable("support_screen") {
            SupportScreen(onCollapse = { settingsNavController.popBackStack() })
        }
        composable("security_screen") {
            SecurityScreen(
                user,
                onBack = { settingsNavController.popBackStack() },
                settingsNavController,
                apiClient
            )
        }
        composable("change_password") {
            user.id?.let { it1 ->
                ChangePasswordScreen(
                    onBack = { settingsNavController.popBackStack() },
                    apiClient,
                    userId = it1
                )
            }
        }
        composable("verify_email") {
            user.id?.let { it1 ->
                EmailVerificationScreen(
                    userId = it1,
                    apiClient = apiClient,
                    onVerificationSuccess = {
                        settingsNavController.popBackStack()
                    }
                )
            }
        }
        composable("change_email") {
            user.id?.let { it1 ->
                ChangeEmailScreen(
                    onBack = {
                        settingsNavController.popBackStack()
                    },
                    apiClient = apiClient,
                    userId = it1,
                    onSuccess = {
                        settingsNavController.navigate("verify_email") {
                            popUpTo("security_screen")
                        }
                    }
                )
            }
        }
    }
}

fun clearCache(context: Context) {
    try {
        context.cacheDir.deleteRecursively()
        context.cacheDir.mkdirs()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun clearUserData(context: Context) {
    try {
        val appDir = context.filesDir.parentFile
        appDir?.list()?.forEach { fileName ->
            if (fileName != "lib") {
                File(appDir, fileName).deleteRecursively()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getCacheSize(context: Context): Long {
    return context.cacheDir.walk().sumOf { it.length() }
}

fun getDataSize(context: Context): Long {
    val filesDir = context.filesDir
    val cacheDir = context.cacheDir
    val externalCacheDir = context.externalCacheDir
    val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")

    return getFolderSize(filesDir) + getFolderSize(cacheDir) + getFolderSize(externalCacheDir) + getFolderSize(
        sharedPrefsDir
    )
}

fun getAppSize(context: Context): Long {
    return try {
        val storageStatsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        val uuid = storageManager.primaryStorageVolume.uuid?.let { UUID.fromString(it) }
            ?: StorageManager.UUID_DEFAULT
        val userHandle = android.os.UserHandle.getUserHandleForUid(android.os.Process.myUid())

        val storageStats =
            storageStatsManager.queryStatsForPackage(uuid, context.packageName, userHandle)

        storageStats.appBytes + storageStats.dataBytes + storageStats.cacheBytes
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }
}

fun getFolderSize(dir: File?): Long {
    if (dir == null || !dir.exists()) return 0
    return dir.walk().map { it.length() }.sum()
}

fun formatSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.2f KB", kb)
        else -> "$size B"
    }
}

@Composable
fun StorageInfoItem(icon: ImageVector, title: String, size: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Text(
                text = size,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

suspend fun deleteAccount(
    userId: Long,
    apiClient: ApiClient,
    context: Context,
    onLogoutSuccess: () -> Unit
): String {
    return try {
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
        val response = apiClient.userApiService.deleteAccount(userId)
        if (response.isSuccessful) {
            return "Account deleted successfully"
        } else {
            val errorMessage = response.errorBody()?.string()?.let {
                JSONObject(it).optString("message", "Unknown error")
            } ?: "Unknown error"
            return errorMessage
        }
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
