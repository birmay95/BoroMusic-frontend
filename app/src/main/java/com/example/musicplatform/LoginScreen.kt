package com.example.musicplatform

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.tracks.User

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    apiClient: ApiClient
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isRegister by remember { mutableStateOf(false) }

    if(!isRegister) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0E1A))
                .padding(16.dp)
        ) {
            val screenWidth = maxWidth

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "BMusic",
                    fontSize = if (screenWidth > 600.dp) 40.sp else 36.sp,
                    color = Color.White
                )
                Text(
                    text = "Registration",
                    fontSize = if (screenWidth > 600.dp) 20.sp else 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(0.2f))
                Image(
                    painter = painterResource(id = R.drawable.ic_icon),
                    contentDescription = "Icon of the app",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(
                            "Username",
                            fontSize = if (screenWidth > 600.dp) 24.sp else 16.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = if (screenWidth > 600.dp) 48.dp else 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0A0E1A),
                        unfocusedContainerColor = Color(0xFF0A0E1A),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            "Password",
                            fontSize = if (screenWidth > 600.dp) 24.sp else 16.sp
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = if (screenWidth > 600.dp) 48.dp else 8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0A0E1A),
                        unfocusedContainerColor = Color(0xFF0A0E1A),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка входа
                val isPressed1 = remember { mutableStateOf(false) }
                val scale1 by animateFloatAsState(targetValue = if (isPressed1.value) 0.95f else 1f)
                val buttonColor1 by animateColorAsState(targetValue = if (isPressed1.value) Color.Gray else Color.Transparent)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .width(if (screenWidth > 600.dp) 450.dp else 200.dp)
                            .border(3.dp, Color(0xFFCACDD2), CircleShape)
                            .background(buttonColor1, CircleShape)
                            .graphicsLayer(scaleX = scale1, scaleY = scale1)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPressed1.value = true
                                        tryAwaitRelease()
                                        isPressed1.value = false
                                    },
                                    onTap = {
                                        isLoading = true
                                        apiClient.login(username, password, { user ->
                                            onLoginSuccess(user)
                                        }, { error ->
                                            isLoading = false
                                            Handler(Looper.getMainLooper()).post {
                                                Toast.makeText(
                                                    context,
                                                    "Login failed: $error",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }, context)
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLoading) "Logging in..." else "Login",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCACDD2),
                            fontSize = if (screenWidth > 600.dp) 24.sp else 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val isPressed = remember { mutableStateOf(false) }
                val scale by animateFloatAsState(targetValue = if (isPressed.value) 0.95f else 1f)
                val buttonColor by animateColorAsState(targetValue = if (isPressed.value) Color.Gray else Color.Transparent) // Цвет меняется на серый при нажатии

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .width(if (screenWidth > 600.dp) 450.dp else 200.dp)
                            .border(3.dp, Color(0xFFCACDD2), CircleShape)
                            .background(
                                buttonColor,
                                CircleShape
                            ) // Устанавливаем цвет фона, который анимируется
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPressed.value = true
                                        tryAwaitRelease() // Ждем завершения нажатия
                                        isPressed.value = false
                                    },
                                    onTap = {
                                        isRegister = true
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLoading) "Logging in..." else "Register",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCACDD2),
                            fontSize = if (screenWidth > 600.dp) 24.sp else 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    } else {
        RegisterScreen(
            onLoginSuccess = onLoginSuccess,
            apiClient = apiClient,
            onCollapse = {
                isRegister = false
            }
        )
    }
}

@Composable
fun RegisterScreen(
    onLoginSuccess: (User) -> Unit,
    apiClient: ApiClient,
    onCollapse: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .padding(16.dp)
    ) {
        val screenWidth = maxWidth

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse, modifier = Modifier.size(30.dp)) {
                    Icon(
                        painterResource(id = R.drawable.ic_roll_up),
                        contentDescription = "Roll up",
                        tint = Color(0xFF515A82)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "BMusic",
                    fontSize = if (screenWidth > 600.dp) 40.sp else 36.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(30.dp))
            }
            Text(
                text = "Registration",
                fontSize = if (screenWidth > 600.dp) 20.sp else 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(0.2f))
            Image(
                painter = painterResource(id = R.drawable.ic_icon),
                contentDescription = "Icon of the app",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.weight(0.4f))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontSize = if (screenWidth > 600.dp) 24.sp else 16.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (screenWidth > 600.dp) 48.dp else 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0A0E1A),
                    unfocusedContainerColor = Color(0xFF0A0E1A),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", fontSize = if (screenWidth > 600.dp) 24.sp else 16.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (screenWidth > 600.dp) 48.dp else 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0A0E1A),
                    unfocusedContainerColor = Color(0xFF0A0E1A),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontSize = if (screenWidth > 600.dp) 24.sp else 16.sp) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (screenWidth > 600.dp) 48.dp else 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0A0E1A),
                    unfocusedContainerColor = Color(0xFF0A0E1A),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            val isPressed = remember { mutableStateOf(false) }
            val scale by animateFloatAsState(targetValue = if (isPressed.value) 0.95f else 1f)
            val buttonColor by animateColorAsState(targetValue = if (isPressed.value) Color.Gray else Color.Transparent) // Цвет меняется на серый при нажатии

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .width(if (screenWidth > 600.dp) 450.dp else 200.dp)
                        .border(3.dp, Color(0xFFCACDD2), CircleShape)
                        .background(
                            buttonColor,
                            CircleShape
                        ) // Устанавливаем цвет фона, который анимируется
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed.value = true
                                    tryAwaitRelease() // Ждем завершения нажатия
                                    isPressed.value = false
                                },
                                onTap = {
                                    isLoading = true
                                    apiClient.register(email, username, password, { user ->
                                        onLoginSuccess(user)
                                    }, { error ->
                                        isLoading = false
                                        Handler(Looper.getMainLooper()).post {
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Login failed: $error",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        }
                                    }, context)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLoading) "Register in..." else "Register",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCACDD2),
                        fontSize = if (screenWidth > 600.dp) 24.sp else 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
