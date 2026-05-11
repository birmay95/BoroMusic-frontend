import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.musicplatform.R
import com.example.musicplatform.settings.getFileName

@Composable
fun UploadTrackDialog(
    uri: Uri,
    context: Context,
    onDismiss: () -> Unit,
    onUpload: (title: String, artist: String, album: String) -> Unit
) {
    val defaultTitle = getFileName(context, uri)?.substringBeforeLast(".") ?: ""

    var title by remember { mutableStateOf(defaultTitle) }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFF0A0E1A), shape = RoundedCornerShape(16.dp))
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
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_roll_up),
                            contentDescription = "Close",
                            tint = Color(0xFF515A82)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Track Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFCACDD2),
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(36.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *", color = Color(0xFF8589AC)) },
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFCACDD2),
                    unfocusedBorderColor = Color(0xFF353D60)
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = artist,
                onValueChange = { artist = it },
                label = { Text("Artist", color = Color(0xFF8589AC)) },
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFCACDD2),
                    unfocusedBorderColor = Color(0xFF353D60)
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = album,
                onValueChange = { album = it },
                label = { Text("Album", color = Color(0xFF8589AC)) },
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFCACDD2),
                    unfocusedBorderColor = Color(0xFF353D60)
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            val isButtonEnabled = title.isNotBlank()
            val buttonColor = if (isButtonEnabled) Color(0xFFCACDD2) else Color(0xFF515A82)

            Button(
                onClick = { onUpload(title.trim(), artist.trim(), album.trim()) },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, buttonColor, CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "Upload Track",
                    style = MaterialTheme.typography.bodySmall,
                    color = buttonColor,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}