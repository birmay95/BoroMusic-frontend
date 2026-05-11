import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PersonalFeedCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF3F4F82),
                        Color(0xFF5D84A5)
                    )
                )
            )
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "For you",
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF293A65)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "For you",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF020F17),
                    fontSize = 18.sp
                )
                Text(
                    text = "Tracks are just for you",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF282B32),
                    fontSize = 14.sp
                )
            }
        }
    }
}