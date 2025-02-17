package com.example.musicplatform

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        containerColor = Color(0xFF353D60)
    ) {
        NavigationBarItem(
            selected = selectedItem == 0,
            onClick = { onItemSelected(0) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp),
                )
            },
//            label = { Text(text = "Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFA7D1DD),
                unselectedIconColor = Color(0xFF737BA5),
                selectedTextColor = Color(0xFFC6CAEB),
                unselectedTextColor = Color(0xFF0A0E1A),
            )
        )
        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = { onItemSelected(1) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_favourite_true),
                    contentDescription = "Favorites",
                    modifier = Modifier.size(24.dp) // Задаем размер значка
                )
            },
//            label = { Text(text = "Favorites") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFA7D1DD),
                unselectedIconColor = Color(0xFF737BA5),
                selectedTextColor = Color(0xFFC6CAEB),
                unselectedTextColor = Color(0xFF0A0E1A)
            )
        )
        NavigationBarItem(
            selected = selectedItem == 2,
            onClick = { onItemSelected(2) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_playlist_play),
                    contentDescription = "Playlists",
                    modifier = Modifier.size(24.dp) // Задаем размер значка
                )
            },
//            label = { Text(text = "Playlists") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFA7D1DD),
                unselectedIconColor = Color(0xFF737BA5),
                selectedTextColor = Color(0xFFC6CAEB),
                unselectedTextColor = Color(0xFF0A0E1A)
            )
        )
    }
}