package com.example.musicplatform.authorization

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.R
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.model.User

@Composable
fun RegisterScreen(
    apiClient: ApiClient,
    onCollapse: () -> Unit,
    onNavigateToVerification: (User) -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordMatching by remember { mutableStateOf(true) }

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
                .fillMaxSize(),
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
                    text = "BoroMusic",
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

            val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

            var isEmailValid by remember { mutableStateOf(true) }

            TextField(
                value = email,
                onValueChange = {
                    if (it.length <= 100) {
                        email = it
                    }
                    isEmailValid = emailPattern.matches(it)
                },
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

            if (!isEmailValid && email.isNotEmpty()) {
                Text(
                    text = "Invalid email format",
                    color = Color.Red,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = username,
                onValueChange = {
                    if (it.length <= 25) {
                        username = it
                    }
                },
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
                onValueChange = {
                    if (it.length <= 50) {
                        password = it
                    }
                },
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
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = confirmPassword,
                onValueChange = {
                    if (it.length <= 50) {
                        confirmPassword = it
                    }
                    isPasswordMatching = it == password
                },
                label = {
                    Text(
                        "Confirm password",
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

            if (!isPasswordMatching && confirmPassword.isNotEmpty()) {
                Text(
                    text = "Passwords don't match",
                    color = Color.Red,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

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
                                    isLoading = true
                                    apiClient.register(email, username, password, { user ->
                                        Toast
                                            .makeText(
                                                context,
                                                "Check your email for verification",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()
                                        onNavigateToVerification(user)
                                    }, { error ->
                                        isLoading = false
                                        Log.e("Register", "error - $error")
                                        Handler(Looper.getMainLooper()).post {
                                            Toast
                                                .makeText(
                                                    context,
                                                    error,
                                                    Toast.LENGTH_LONG
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