package com.example.musicplatform.authorization

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicplatform.R
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.model.User

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    apiClient: ApiClient
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val loginNavController = rememberNavController()

    var userRegistered by remember { mutableStateOf<User?>(null) }

    NavHost(navController = loginNavController, startDestination = "main") {
        composable("main") {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0E1A))
                    .padding(top = 16.dp)
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
                        text = "BoroMusic",
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
                        onValueChange = {
                            if (it.length <= 25) {
                                username = it
                            }
                        },
                        label = {
                            Text(
                                "Username",
                                fontSize = if (screenWidth > 600.dp) 24.sp else 16.sp
                            )
                        },
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
                        onValueChange = {
                            if (it.length <= 100) {
                                password = it
                            }
                        },
                        label = {
                            Text(
                                "Password",
                                fontSize = if (screenWidth > 600.dp) 24.sp else 16.sp
                            )
                        },
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

                    val isPressed1 = remember { mutableStateOf(false) }
                    val scale1 by animateFloatAsState(
                        targetValue = if (isPressed1.value) 0.95f else 1f,
                        label = ""
                    )
                    val buttonColor1 by animateColorAsState(
                        targetValue = if (isPressed1.value) Color.Gray else Color.Transparent,
                        label = ""
                    )

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
                                                Handler(Looper.getMainLooper()).post {
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "Welcome to BoroMusic",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                                }
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
                                text = if (isLoading) "Logging in..." else "Login",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCACDD2),
                                fontSize = if (screenWidth > 600.dp) 24.sp else 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val isPressed = remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed.value) 0.95f else 1f,
                        label = ""
                    )
                    val buttonColor by animateColorAsState(
                        targetValue = if (isPressed.value) Color.Gray else Color.Transparent,
                        label = ""
                    )

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
                                )
                                .graphicsLayer(scaleX = scale, scaleY = scale)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isPressed.value = true
                                            tryAwaitRelease()
                                            isPressed.value = false
                                        },
                                        onTap = {
                                            loginNavController.navigate("register")
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
        }
        composable("register") {
            RegisterScreen(
                apiClient = apiClient,
                onCollapse = {
                    loginNavController.popBackStack()
                },
                onNavigateToVerification = { user ->
                    userRegistered = user
                    loginNavController.navigate("email_verification")
                }
            )
        }
        composable("email_verification") {
            userRegistered?.let { it1 ->
                it1.id?.let { it2 ->
                    EmailVerificationScreen(
                        userId = it2,
                        apiClient = apiClient,
                        onVerificationSuccess = {
                            Toast
                                .makeText(
                                    context,
                                    "Welcome to BoroMusic",
                                    Toast.LENGTH_LONG
                                )
                                .show()
                            onLoginSuccess(it1)
                        })
                }
            }
        }
    }
}