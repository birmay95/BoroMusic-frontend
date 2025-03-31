package com.example.musicplatform.main

import androidx.compose.foundation.layout.Column
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
import com.example.musicplatform.R

@Composable
fun BottomNavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(75.dp)
    ) {
        NavigationBar(
            modifier = modifier
                .fillMaxWidth(),
            containerColor = Color(0xFF353D60)
        ) {
            listOf(
                Pair(R.drawable.ic_home, "Home"),
                Pair(R.drawable.ic_favourite_true, "Favorites"),
                Pair(R.drawable.ic_playlist_play, "Playlists")
            ).forEachIndexed { index, (iconRes, contentDesc) ->
                NavigationBarItem(
                    selected = selectedItem == index,
                    onClick = { onItemSelected(index) },
                    icon = {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = contentDesc,
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFA7D1DD),
                        unselectedIconColor = Color(0xFF737BA5),
                        selectedTextColor = Color(0xFFC6CAEB),
                        unselectedTextColor = Color(0xFF0A0E1A)
                    )
                )
            }
        }
    }
}
