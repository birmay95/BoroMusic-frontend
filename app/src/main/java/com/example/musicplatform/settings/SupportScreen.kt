package com.example.musicplatform.settings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplatform.R

@Composable
fun SupportScreen(onCollapse: () -> Unit) {
    val context = LocalContext.current
    val supportEmail = "boromusic.supp@gmail.com"

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
                IconButton(onClick = onCollapse, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_roll_up),
                        contentDescription = "Roll up",
                        tint = Color(0xFF515A82)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Support",
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
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$supportEmail")
                    putExtra(Intent.EXTRA_SUBJECT, "Support Request")
                }
                context.startActivity(intent)
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
                    text = "Write to Support",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Frequently Asked Questions", color = Color.White, fontSize = 20.sp)

        Spacer(modifier = Modifier.height(32.dp))

        val faqList = listOf(
            "How to upload a track?" to "Only artists and admins can upload tracks. Go to Settings > Upload Track and select an audio file from your device.",
            "How to delete a track?" to "Only admins can remove tracks.",
            "How to change my password?" to "Go to Settings > Security & Privacy > Change Password. You'll need to enter your current password and then the new one twice.",
            "How to verify my email?" to "After changing your email, you'll receive a verification code. You can also request a new verification email in Security & Privacy section.",
            "How to clear app cache?" to "Go to Settings > Storage Information > Remove Cache. This will free up space without deleting your personal data.",
            "Why can't I upload tracks?" to "Track uploading is restricted to artists and admins. Regular users can't upload content. Contact support to request artist status.",
            "How to contact support?" to "Use the Support section in Settings to send us a message. We typically respond within 24-48 hours.",
            "What happens when I delete my account?" to "All your data will be permanently erased. This action cannot be undone. You'll need to create a new account if you want to use the service again.",
            "How to report a bug?" to "Please use the Support section to describe the issue you encountered. Include details like device model and app version.",
            "How to log out?" to "Go to Settings and tap 'Logout from the account'. You'll be returned to the login screen.",
            "How to request artist status?" to "Admins can grant artist status. Contact support with information about your musical work to request this privilege."
        )

        LazyColumn {
            items(faqList) { (question, answer) ->
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = question, color = Color(0xFFCACDD2), fontWeight = FontWeight.Bold)
                    Text(text = answer, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
