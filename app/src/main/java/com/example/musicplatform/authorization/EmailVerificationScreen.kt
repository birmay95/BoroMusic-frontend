package com.example.musicplatform.authorization

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun EmailVerificationScreen(
    userId: Long,
    apiClient: ApiClient,
    onVerificationSuccess: () -> Unit
) {
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val isPressed = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed.value) 0.95f else 1f, label = "")
    val buttonColor by animateColorAsState(
        targetValue = if (isPressed.value) Color.Gray else Color.Transparent,
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Enter the 6-digit verification code sent to your email:",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            SixDigitCodeInput(
                code = code,
                onValueChange = { newCode -> code = newCode }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .width(200.dp)
                        .border(3.dp, Color(0xFFCACDD2), CircleShape)
                        .background(buttonColor, CircleShape)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed.value = true
                                    tryAwaitRelease()
                                    isPressed.value = false
                                },
                                onTap = {
                                    if (code.length == 6) {
                                        isLoading = true
                                        apiClient.verifyEmail(code, userId, {
                                            isLoading = false
                                            CoroutineScope(Dispatchers.Main).launch {
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Email verified successfully",
                                                        Toast.LENGTH_LONG
                                                    )
                                                    .show()
                                                onVerificationSuccess()
                                            }
                                        }, { error ->
                                            isLoading = false
                                            CoroutineScope(Dispatchers.Main).launch {
                                                Toast
                                                    .makeText(context, error, Toast.LENGTH_LONG)
                                                    .show()
                                            }
                                        })
                                    } else {
                                        Toast
                                            .makeText(
                                                context,
                                                "Please enter a valid 6-digit code",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLoading) "Verifying..." else "Verify Email",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCACDD2),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Skip",
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onVerificationSuccess() }
                )
            }
        }
    }
}

@Composable
fun SixDigitCodeInput(
    code: String,
    onValueChange: (String) -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                        .background(Color(0xFF0A0E1A), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < code.length) {
                        Text(
                            text = code[index].toString(),
                            color = Color.White,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        BasicTextField(
            value = code,
            onValueChange = { newText ->
                if (newText.length <= 6 && newText.all { it.isDigit() }) {
                    onValueChange(newText)
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            textStyle = TextStyle(color = Color.Transparent),
            modifier = Modifier
                .size(300.dp, 50.dp)
                .background(Color.Transparent)
        )
    }
}

