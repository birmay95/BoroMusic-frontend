package com.example.musicplatform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.musicplatform.ui.theme.MusicPlatformTheme
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContent {
            MusicPlatformTheme {
                MusicAppScreen(
                    tracks = sampleTracks,
                    onTrackClickMain = { track, currentTrack -> playTrack(track, currentTrack) },
                    mediaPlayer = mediaPlayer
                )
            }
        }

    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
    }

    private fun playTrack(track: Track, currentTrack: Track?) {
        // Останавливаем текущий трек, если он отличается от нового
        if (currentTrack != null && currentTrack.track != track.track) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        if (currentTrack?.track == track.track) {
            mediaPlayer?.seekTo(0)        // Если тот же трек, перематываем на начало
            mediaPlayer?.start()          // И воспроизводим
        } else {
            if(mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, track.track)
            }
        }

        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // Освобождаем ресурсы MediaPlayer при завершении Activity
        mediaPlayer = null
    }
}

// Модель данных для трека
data class Track(
    val title: String,
    val artist: String,
    val track: Int,
    var favourite: Boolean
)

// Фиктивные данные для тестирования
val sampleTracks = listOf(
    Track("Mockingbird", "Eminem", R.raw.eminem_mockingbird, false),
    Track("Sweater Weather", "The Neighbourhood", R.raw.the_neighbourhood_sweater_weather, false),
    Track("Superman", "Eminem feat. Dina Rae", R.raw.eminem_feat_dina_rae_superman, false),
    Track("Можно я с тобой", "APSENT", R.raw.apsent_maybe_i_am_with_you, false)
)

@Composable
fun MusicAppScreen(
    tracks: List<Track>,
    onTrackClickMain: (Track, Track?) -> Unit,
    mediaPlayer: MediaPlayer?
) {
    val allTracks = remember { mutableStateListOf(*tracks.toTypedArray()) }
    var favouriteTracks by remember { mutableStateOf(listOf<Track>()) }
    val trackHistory = mutableListOf<Int>()
    var isRandomMode by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentTime by remember { mutableIntStateOf(0) }

    val playedTracks = remember { mutableStateListOf<Track>() }
    var currentTrack by remember { mutableStateOf<Track?>(null) }
    var isPlayerExpanded by remember { mutableStateOf(false) }
    var playingTrackIndex by remember { mutableIntStateOf(0) }

    // Добавление или удаление трека из избранного
    val onFavouriteToggle: (Track) -> Unit = { track ->
        val index = allTracks.indexOf(track)
        if (index != -1) {
            allTracks[index] = track.copy(favourite = !track.favourite)
            favouriteTracks = allTracks.filter { it.favourite }
        }
    }

    LaunchedEffect(mediaPlayer) {
        while (true) {
            delay(1000L) // Обновляем каждую секунду
            if (mediaPlayer?.isPlaying == true) {
                currentTime = mediaPlayer.currentPosition.div(1000)
            }
        }
    }

    // Обработка клика по треку
    val onTrackClick: (Track) -> Unit = { track ->
        onTrackClickMain(track, currentTrack)
        currentTrack = track              // Обновляем текущий трек
        playingTrackIndex = tracks.indexOfFirst { it.track == track.track }

        if (!playedTracks.contains(track)) {
            playedTracks.clear()
            playedTracks.add(track)
        }
    }

    val onSeekTo: (Int) -> Unit = { newPosition ->
        mediaPlayer?.seekTo(newPosition * 1000) // Переводим позицию в миллисекунды
        currentTime = newPosition // Обновляем состояние текущего времени
    }

    // Функция для переключения на следующий трек
    val onNextTrack: () -> Unit = {
        val nextIndex: Int
        val currentTrackList = if (selectedItem == 0) {
            allTracks
        } else {
            favouriteTracks
        }
        if(!isRandomMode) {
            nextIndex = (playingTrackIndex + 1) % currentTrackList.size
        } else {
            if (playedTracks.size == currentTrackList.size) {
                playedTracks.clear()
            }

            val remainingTracks = currentTrackList.filter { it !in playedTracks }
            val randomTrack = remainingTracks.random()

            playedTracks.add(randomTrack)
            nextIndex = currentTrackList.indexOf(randomTrack)
        }
        // Сохраняем текущий трек в истории перед переключением
        if (trackHistory.isEmpty() || trackHistory.last() != playingTrackIndex) {
            trackHistory.add(playingTrackIndex)
        }
        playingTrackIndex = nextIndex
        if (!isPlaying) {
            isPlaying = true
        }
        onTrackClick(currentTrackList[nextIndex])
    }

// Функция для переключения на предыдущий трек
//    val onPrevTrack: () -> Unit = {
//        if (trackHistory.isNotEmpty()) {
//            // Убираем последний трек из истории (он уже проигран)
//            trackHistory.removeAt(trackHistory.size - 1)
//
//            // Получаем индекс предыдущего трека из истории
//            val prevIndex = if (trackHistory.isEmpty()) {
//                // Если история пуста, то возвращаемся к последнему треку
//                playingTrackIndex
//            } else {
//                trackHistory.last()
//            }
//
//            onPlayingTrackIndexChange(prevIndex)
//            if (!isPlaying) {
//                isPlaying = true
//            }
//            onTrackClick(tracks[prevIndex])
//        }
//    }
    val onPrevTrack: () -> Unit = {
        val prevIndex = if (playingTrackIndex - 1 < 0) allTracks.size - 1 else (playingTrackIndex - 1)
        playingTrackIndex = prevIndex
        if (!isPlaying) {
            isPlaying = true
        }
        onTrackClick(allTracks[prevIndex])
//        playTrack(context, tracks[prevIndex])
    }

    // Устанавливаем слушатель завершения трека
    LaunchedEffect(mediaPlayer) {
        mediaPlayer?.setOnCompletionListener {
            onNextTrack() // Переключаем на следующий трек
        }
    }

    val onMixToggle: () -> Unit = {
        isRandomMode = !isRandomMode
    }

    val onExpandPlayer: () -> Unit = {
        isPlayerExpanded = !isPlayerExpanded
    }

    // Функция для паузы или воспроизведения
    val onPlayPauseClick: () -> Unit = {
        if (isPlaying) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
        isPlaying = !isPlaying
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101115))
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        if (!isPlayerExpanded) {
            Column(modifier = Modifier.weight(1f)) {
                when (selectedItem) {
                    0 -> TrackList(tracks = allTracks, onTrackClick = onTrackClick, onFavouriteToggle = onFavouriteToggle)
                    1 -> TrackList(tracks = favouriteTracks, onTrackClick = onTrackClick, onFavouriteToggle = onFavouriteToggle)
                    2 -> Text("Playlists Screen", color = Color.White, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Мини-плеер
        if (currentTrack != null) {
            MiniPlayer(
                track = allTracks[playingTrackIndex],
                isExpanded = isPlayerExpanded,
                onExpandClick = onExpandPlayer,
                onNextTrack = onNextTrack,
                onPrevTrack = onPrevTrack,
                isPlaying = isPlaying,
                onPlayPauseClick = onPlayPauseClick,
                currentTime = currentTime,
                trackDuration = mediaPlayer?.duration?.div(1000) ?: 0,
                onSeekTo = onSeekTo,
                onFavouriteToggle = onFavouriteToggle,
                onMixToggle = onMixToggle,
                isRandomMode = isRandomMode
            )
        }

        // Навигационная панель
        BottomNavigationBar(
            selectedItem = selectedItem,
            onItemSelected = { selectedItem = it }
        )
    }
}


@Composable
fun MiniPlayer(
    track: Track,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    currentTime: Int,
    trackDuration: Int,
    onSeekTo: (Int) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    onMixToggle: () -> Unit,
    isRandomMode: Boolean
) {
    if (isExpanded) {
        // Полноэкранный плеер
        FullPlayerScreen(
            track = track,
            onCollapse = onExpandClick,
            onNextTrack = onNextTrack,
            onPrevTrack = onPrevTrack,
            isPlaying = isPlaying,
            onPlayPauseClick = onPlayPauseClick,
            currentPosition = currentTime,
            trackDuration = trackDuration,
            onSeekTo = onSeekTo,
            onFavouriteToggle = onFavouriteToggle,
            onMixToggle = onMixToggle,
            isRandomMode = isRandomMode
            )
    } else {
        val swipeThreshold = 100f
        var hasSwiped = false
        // Мини-плеер
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF303147))
                .padding(8.dp)
                .clickable { onExpandClick() }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // Сбрасываем флаг, когда жест завершен
                            hasSwiped = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!hasSwiped) { // Проверяем, был ли уже выполнен свайп
                                if (dragAmount > swipeThreshold) {
                                    // Свайп вправо (переключение на следующий трек)
                                    onPrevTrack()
                                    hasSwiped =
                                        true // Устанавливаем флаг, чтобы избежать повторных переключений
                                } else if (dragAmount < -swipeThreshold) {
                                    // Свайп влево (переключение на предыдущий трек)
                                    onNextTrack()
                                    hasSwiped =
                                        true // Устанавливаем флаг, чтобы избежать повторных переключений
                                }
                            }
                        }
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_music_note),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = track.title, color = Color(0xFF8589AC), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = track.artist, color = Color(0xFF8589AC), fontSize = 12.sp)
            }
            Row {
                IconButton(onClick = {
                    onPlayPauseClick() // Логика паузы/воспроизведения
                }) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        Modifier.size(35.dp)
                    )
                }
                IconButton(onClick = {
                    onFavouriteToggle(track)
                }) {
                    Icon(
                        painter = painterResource(if (track.favourite) R.drawable.ic_favourite_true else R.drawable.ic_favorite), // Замените на ваш значок воспроизведения
                        contentDescription = if (track.favourite) "Like" else "Not like",
                        Modifier.size(30.dp)
                    )
                }
            }
        }
        // Добавляем индикатор прогресса внизу мини-плеера
        LinearProgressIndicator(
            progress = { currentTime.toFloat() / trackDuration.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp) // Уменьшаем высоту для создания маленькой полосы прогресса
                .background(Color(0xFF8589AC)), // Цвет фона
        )
    }
}

@Composable
fun FullPlayerScreen(
    track: Track,
    onCollapse: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    currentPosition: Int, // Текущее время воспроизведения в секундах
    trackDuration: Int, // Общая продолжительность трека в секундах
    onSeekTo: (Int) -> Unit, // Функция для изменения позиции воспроизведения
    onFavouriteToggle: (Track) -> Unit,
    onMixToggle: () -> Unit,
    isRandomMode: Boolean
) {
    val swipeThreshold = 150f
    var hasSwiped = false
    Column(
        modifier = Modifier
            .background(Color(0xFF303147))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(
                    painterResource(id = R.drawable.ic_roll_up),
                    contentDescription = "Roll up",
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {}) {
                Icon(
                    painterResource(id = R.drawable.ic_share),
                    contentDescription = "Share",
                    Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 90.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            hasSwiped = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!hasSwiped) { // Проверяем, был ли уже выполнен свайп
                                if (dragAmount > swipeThreshold) {
                                    // Свайп вправо (переключение на следующий трек)
                                    onPrevTrack()
                                    hasSwiped =
                                        true // Устанавливаем флаг, чтобы избежать повторных переключений
                                } else if (dragAmount < -swipeThreshold) {
                                    // Свайп влево (переключение на предыдущий трек)
                                    onNextTrack()
                                    hasSwiped =
                                        true // Устанавливаем флаг, чтобы избежать повторных переключений
                                }
                            }
                        }
                    )
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .background(Color(0xFF596F9A))
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_music_note),
                    contentDescription = "Music",
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.Center),
                    tint = Color(0xFF303147)
                )
            }
        }
        Row {
            Column {
                Text(
                    text = track.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp)
                )
                Text(
                    text = track.artist,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            }
            IconButton(onClick = {
                onFavouriteToggle(track)
            },
                modifier = Modifier.padding(top = 36.dp, start = 8.dp)
                ) {
                Icon(
                    painter = painterResource(if (track.favourite) R.drawable.ic_favourite_true else R.drawable.ic_favorite), // Замените на ваш значок воспроизведения
                    contentDescription = if (track.favourite) "Like" else "Not like",
                    modifier = Modifier
                        .size(30.dp),
                    tint = Color(0xFF8589AC)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Полоса прогресса
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { onSeekTo(it.toInt()) },
            valueRange = 0f..trackDuration.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF3F51B5),
                inactiveTrackColor = Color(0xFF8589AC)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                color = Color.White,
                fontSize = 12.sp
            )
            Text(
                text = formatTime(trackDuration),
                color = Color.White,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(90.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    onMixToggle()
                },
                modifier = Modifier.size(50.dp)
            ){
                Icon(
                    painterResource(id = R.drawable.ic_mix),
                    contentDescription = "Mix",
                    modifier = Modifier.size(40.dp),
                    tint = if (isRandomMode) Color(0xFF8589AC) else Color.Black
                )
            }
            IconButton(
                onClick = onPrevTrack,
                modifier = Modifier.size(50.dp)
            ){
                Icon(
                    painterResource(id = R.drawable.ic_prev),
                    contentDescription = "Prev",
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))

            IconButton (
                onClick = {
                    onPlayPauseClick()
                },
                modifier = Modifier
                    .size(90.dp)
                    .background(Color(0xFF8589AC), shape = CircleShape)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            IconButton(
                onClick = onNextTrack,
                modifier = Modifier.size(50.dp)
            ){
                Icon(
                    painterResource(id = R.drawable.ic_next),
                    contentDescription = "Next",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.size(16.dp))
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}



@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text(text = "Search...", color = Color(0xFF8589AC)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFF303147), shape = RoundedCornerShape(8.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF303147),
            unfocusedContainerColor = Color(0xFF303147),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White
        )
    )
}

@Composable
fun TrackList(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onFavouriteToggle: (Track) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Фильтрация треков на основе текста поиска
    val filteredTracks = tracks.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
    }

    Column {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101115))
        ) {
            items(filteredTracks) { track ->
                TrackItem(track = track, onClick = { onTrackClick(track) }, onFavouriteToggle = onFavouriteToggle)
            }
        }
    }
}

@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    onFavouriteToggle: (Track) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
            .background(Color(0xFF303147), shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_music_note), // Замените на ваш значок
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF405577), shape = CircleShape)
                .padding(8.dp),
//            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = track.artist,
                color = Color(0xFF8589AC),
                fontSize = 12.sp
            )
        }
        IconButton(onClick = {
            onFavouriteToggle(track)
        }) {
            Icon(
                painter = painterResource(if (track.favourite) R.drawable.ic_favourite_true else R.drawable.ic_favorite), // Замените на ваш значок воспроизведения
                contentDescription = if (track.favourite) "Like" else "Not like",
                modifier = Modifier.size(35.dp),
                tint = Color(0xFF8589AC)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp), // Установим высоту панели
        containerColor = Color(0xFF303147) // Цвет фона панели
    ) {
        NavigationBarItem(
            selected = selectedItem == 0,
            onClick = { onItemSelected(0) },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp) // Задаем размер значка
                )
            },
//            label = { Text(text = "Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF3596F9),
                unselectedIconColor = Color(0xFF8589AC),
                selectedTextColor = Color(0xFF3596F9),
                unselectedTextColor = Color(0xFF8589AC)
            )
        )
//        NavigationBarItem(
//            selected = selectedItem == 1,
//            onClick = { onItemSelected(1) },
//            icon = {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_loupe),
//                    contentDescription = "Find",
//                    modifier = Modifier.size(24.dp) // Задаем размер значка
//                )
//            },
////            label = { Text(text = "Home") },
//            colors = NavigationBarItemDefaults.colors(
//                selectedIconColor = Color(0xFF3596F9),
//                unselectedIconColor = Color(0xFF8589AC),
//                selectedTextColor = Color(0xFF3596F9),
//                unselectedTextColor = Color(0xFF8589AC)
//            )
//        )
        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = { onItemSelected(1) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite),
                    contentDescription = "Favorites",
                    modifier = Modifier.size(24.dp) // Задаем размер значка
                )
            },
//            label = { Text(text = "Favorites") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF3596F9),
                unselectedIconColor = Color(0xFF8589AC),
                selectedTextColor = Color(0xFF3596F9),
                unselectedTextColor = Color(0xFF8589AC)
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
                selectedIconColor = Color(0xFF3596F9),
                unselectedIconColor = Color(0xFF8589AC),
                selectedTextColor = Color(0xFF3596F9),
                unselectedTextColor = Color(0xFF8589AC)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MusicPlatformTheme {
        MusicAppScreen(
            tracks = sampleTracks,
            onTrackClickMain = { _, _ -> },
            null
        )
    }
}
