package com.example.musicplatform.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.R
import com.example.musicplatform.api.ApiClient
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun ChangeEmailScreen(
    onBack: () -> Unit,
    apiClient: ApiClient,
    userId: Long,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var newEmail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .padding(top = 32.dp)
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
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_roll_up),
                        contentDescription = "Roll up",
                        tint = Color(0xFF515A82)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Change Email",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFCACDD2),
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(36.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

        var isEmailValid by remember { mutableStateOf(true) }

        OutlinedTextField(
            value = newEmail,
            onValueChange = {
                if (it.length <= 100) {
                    newEmail = it
                }
                isEmailValid = emailPattern.matches(it)
            },
            label = { Text("New Email", color = Color.White) },
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (!isEmailValid && newEmail.isNotEmpty()) {
            Text(
                text = "Неверный формат email",
                color = Color.Red,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(text = it, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (newEmail.isBlank()) {
                    errorMessage = "Email input is required"
                    return@Button
                }
                isLoading = true
                coroutineScope.launch {
                    val result = changeEmail(userId, newEmail, apiClient)
                    isLoading = false
                    result.onSuccess {
                        Toast.makeText(context, "Email changed successfully", Toast.LENGTH_SHORT)
                            .show()
                        newEmail = ""
                        errorMessage = null
                        isLoading = false
                        onSuccess()
                    }.onFailure {
                        Toast.makeText(context, it.message ?: "Unknown error", Toast.LENGTH_SHORT)
                            .show()
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .border(2.dp, Color(0xFFCACDD2), CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Update Email",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCACDD2),
                        fontSize = 14.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

suspend fun changeEmail(userId: Long, newEmail: String, apiClient: ApiClient): Result<String> {
    return try {
        val response = apiClient.userApiService.changeEmail(
            userId,
            newEmail
        )

        if (response.isSuccessful) {
            Result.success("Email changed successfully")
        } else {
            val errorBody = response.errorBody()?.string().orEmpty()
            Log.e("ChangePassword", "Error response: $errorBody")

            val errorMessage = try {
                JSONObject(errorBody).optString("message", "Unknown error")
            } catch (e: Exception) {
                "Unknown error"
            }

            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}