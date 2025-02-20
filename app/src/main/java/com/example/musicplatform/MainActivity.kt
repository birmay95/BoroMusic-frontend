package com.example.musicplatform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.players.MiniPlayer
import com.example.musicplatform.playlists.AddPlaylistScreen
import com.example.musicplatform.playlists.AddTrackToPlaylistsScreen
import com.example.musicplatform.playlists.PlaylistsScreen
import com.example.musicplatform.tracks.Playlist
import com.example.musicplatform.tracks.Track
import com.example.musicplatform.tracks.TrackDetailsScreen
import com.example.musicplatform.tracks.TrackList
import com.example.musicplatform.tracks.User
import com.example.musicplatform.ui.theme.MusicPlatformTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//import com.google.android.exoplayer2.C


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContent {
            MusicPlatformTheme {
                var currentTrack by remember { mutableStateOf<Track?>(null) }
                var playingTrackIndex by remember { mutableIntStateOf(0) }
                var currentTrackList by remember { mutableStateOf<List<Track>>(emptyList())}

                val context = applicationContext
                val apiClient = ApiClient(context)
                var token = apiClient.getJwtToken(context)
                var isLoggedIn by remember { mutableStateOf(token != null) }
                var user: User = apiClient.getUser(context)

                val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                Log.d("Permissions", "POST_NOTIFICATIONS разрешение: $granted")



                if (!isLoggedIn) {
                    LoginScreen(onLoginSuccess = { userLogged ->
                        isLoggedIn = true
                        user = userLogged
                        Log.d("USER", "$user")
                    },
                        apiClient = apiClient
                    )
                } else {
                    MusicAppScreen(
                        onTrackClickMain = { track ->
                            currentTrack = track
                            playingTrackIndex = currentTrackList.indexOfFirst { it.track == track.track }
                        },
                        currentTrack = currentTrack,
                        playingTrackIndex = playingTrackIndex,
                        currentTrackList = currentTrackList,
                        onChangeCurTrackList = { curList ->
                            currentTrackList = curList
                            Log.d("current playlist", "playlist: $currentTrackList")
                        },
                        onLogoutSuccess = {
                            isLoggedIn = false
                            token = null
                        },
                        apiClient = apiClient,
                        context = context,
                        user = user
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun MusicAppScreen(
    onTrackClickMain: (Track) -> Unit,
    currentTrack: Track?,
    playingTrackIndex: Int,
    currentTrackList: List<Track>,
    onChangeCurTrackList: (List<Track>) -> Unit,
    onLogoutSuccess: () -> Unit,
    apiClient: ApiClient,
    context: Context,
    user: User?
) {
    val viewModel: MyViewModel = viewModel()
    LaunchedEffect(Unit) {
        viewModel.loadSampleData(apiClient = apiClient) // Загружаем данные при старте
    }

    Log.d("LoadData", "all tracks: ${viewModel.sampleTracks}")
    var favouriteTracks by remember { mutableStateOf(viewModel.sampleTracks.filter { it.favourite }) }
    val trackHistory = remember { mutableListOf<Int>() }
    var isRandomMode by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableIntStateOf(0)}
    var isRepeatTrack by remember { mutableIntStateOf(0) }
//    var isPlaying by remember { mutableStateOf(true) }
//    var currentTime by remember { mutableLongStateOf(0) }

    var isPlayerExpanded by remember { mutableStateOf(false) }
    val playedTracks = remember { mutableStateListOf<Track>() }

    var currentPlaylist by remember { mutableIntStateOf(1) }
    var isAddingTrackScreen by remember { mutableStateOf(false) }
    var isAddingPlaylistScreen by remember { mutableStateOf(false) }
    var trackForAddToPlaylist by remember { mutableStateOf<Track?>(null) }

    var isSettingsScreen by remember { mutableStateOf(false) }
    var isTrackDetailsScreen by remember { mutableStateOf(false) }

    val onChangeCurrentPlaylist: (Int) -> Unit = { newIndex ->
        currentPlaylist = newIndex
    }

    val serviceConnection = remember { MusicServiceConnection(context) }
    LaunchedEffect(Unit) {
        serviceConnection.bindService()
    }

    val currentTime = serviceConnection.currentTime.collectAsState(initial = 0L).value
    val musicViewModel: MusicViewModel = viewModel()

    val trackDuration = serviceConnection.trackDuration.collectAsState(initial = 0L).value

    val buttonFromNot = serviceConnection.button.collectAsState(initial = 0L).value

    val isPlaying = serviceConnection.isPlaying.collectAsState(initial = false).value

    val playTrackFromServer = { track: Track, fileName: String ->
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val url = "http://192.168.254.172:8080/tracks/download/$fileName"
                Log.d("ExoPlayer", "Запуск воспроизведения: $fileName")
                serviceConnection.playTrack(url)
            } catch (e: Exception) {
                Log.e("NetworkError", "Ошибка загрузки потока: ${e.message}")
            }
        }
    }

    val onFavouriteToggle: (Track) -> Unit = { track ->
        Log.d("OnFavouriteToggle", "Toggling favourite for track: ${track.id}")
        Log.d("rotationnn", "track rotation in fav: ${track.rotation.value}")
        val updatedTrack = track.copy(
            favourite = !track.favourite,
            playlists = track.playlists ?: listOf(),
            genres = track.genres ?: listOf(),
            rotation = track.rotation
        )
        Log.d("OnFavouriteToggle", "Updated track: $updatedTrack")
        Log.d("rotationnn", "updtrack rotation in fav: ${updatedTrack.rotation.value}")
        val index = viewModel.sampleTracks.indexOfFirst { it.id == track.id }
        Log.d("OnFavouriteToggle", "Track index in sampleTracks: $index")

        if (index != -1) {
            viewModel.sampleTracks[index] = updatedTrack
            Log.d("OnFavouriteToggle", "Updated sampleTracks: ${viewModel.sampleTracks}")
        } else {
            Log.e("OnFavouriteToggle", "Track not found in sampleTracks.")
        }

        viewModel.samplePlaylists.forEachIndexed { playlistIndex, playlist ->
            Log.d("OnFavouriteToggle", "Updating playlist at index: $playlistIndex")
            val updatedTracks = playlist.tracks.map { playlistTrack ->
                if (playlistTrack.id == track.id) {
                    Log.d("OnFavouriteToggle", "Updating track in playlist: ${playlistTrack.id}")
                    playlistTrack.copy(favourite = !playlistTrack.favourite, playlists = playlistTrack.playlists ?: listOf(), genres = playlistTrack.genres ?: listOf(), rotation = playlistTrack.rotation ?: Animatable(0f)) // Обеспечьте, чтобы playlists не было null
                } else {
                    playlistTrack
                }
            }.toMutableList()

            val updatedPlaylist = playlist.copy(tracks = updatedTracks)
            viewModel.samplePlaylists[playlistIndex] = updatedPlaylist
            Log.d("OnFavouriteToggle", "Updated playlist: $updatedPlaylist")
        }

        favouriteTracks = viewModel.sampleTracks.filter { it.favourite }
        Log.d("OnFavouriteToggle", "Updated favouriteTracks: $favouriteTracks")
    }

    val onSeekTo: (Int) -> Unit = { newPosition ->
        serviceConnection.seekTo(newPosition)
    }

    val onTrackClick: (Track) -> Unit = { track ->
        val curTrackList = when (selectedItem) {
            0 -> viewModel.sampleTracks
            1 -> favouriteTracks
            else -> viewModel.samplePlaylists[currentPlaylist].tracks
        }
        onChangeCurTrackList(curTrackList)
        serviceConnection.setTrack(track)
        playTrackFromServer(track, track.fileName)
        onTrackClickMain(track)
        if (!playedTracks.contains(track)) {
            playedTracks.clear()
            playedTracks.add(track)
        }
    }

    var nextIndex by remember { mutableIntStateOf(0) }
    val onNextTrack: () -> Unit = {
        val curTrackList = when (selectedItem) {
            0 -> viewModel.sampleTracks
            1 -> favouriteTracks
            else -> viewModel.samplePlaylists[currentPlaylist].tracks
        }
        onChangeCurTrackList(curTrackList)

        if (curTrackList.isEmpty()) {
            Log.d("onNextTrack", "Список треков пуст, переключение невозможно")
//            return@onNextTrack
        }

        Log.d("currentTrack", "Текущий трек: ${currentTrack?.title}")

        when (isRepeatTrack) {
            0 -> {
                if (!isRandomMode) {
                    nextIndex = (playingTrackIndex + 1) % currentTrackList.size
                } else {
                    if (playedTracks.size == currentTrackList.size) {
                        playedTracks.clear()
                    }

                    val remainingTracks = currentTrackList.filter { it !in playedTracks }
                    val randomTrack = remainingTracks.randomOrNull()

                    randomTrack?.let {
                        playedTracks.add(it)
                        nextIndex = currentTrackList.indexOf(it)
                    } ?: run {
                        nextIndex = playingTrackIndex
                        Log.d("onNextTrack", "Не удалось найти случайный трек, остаёмся на текущем")
                    }
                    Log.d("onNextTrack", "Не удалось найти случайный трек, остаёмся на текущем")
                }
                Log.d("nextIndex", "Последовательный/случайный следующий индекс: $nextIndex")


                if (trackHistory.size > 20) {
                    trackHistory.removeAt(0)
                }
                trackHistory.add(playingTrackIndex)
            }
            1 -> {

                val artistTracks = currentTrackList.filter { track ->
                    currentTrack?.artist?.let { currentArtist ->
                        val mainCurrentArtist = currentArtist.split(Regex("[,;&]|\\s+feat\\.?\\s+", RegexOption.IGNORE_CASE))
                            .first().trim().lowercase()

                        val trackArtists = track.artist.split(Regex("[,;&]|\\s+feat\\.?\\s", RegexOption.IGNORE_CASE))
                            .map { it.trim().lowercase() }

                        Log.d("artistTracksDebug", "Текущий трек: ${track.title}, Артисты: $trackArtists, Текущий артист: $mainCurrentArtist")

                        trackArtists.any { it.contains(mainCurrentArtist, ignoreCase = true) }
                    } ?: false
                }

                Log.d("artistTracks", "Общее количество треков: ${currentTrackList.size}")
                Log.d("artistTracks", "Треки текущего артиста: ${artistTracks.size}")

                val currentArtistTrackIndex = artistTracks.indexOf(currentTrack)

                Log.d("currentArtistIndex", "текущий индекс: $currentArtistTrackIndex")

                val nextIndexInArtistsTracks = if (artistTracks.isNotEmpty()) {
                    (currentArtistTrackIndex + 1) % artistTracks.size
                } else {
                    playingTrackIndex // остаёмся на текущем треке, если нет других треков того же артиста
                }

                nextIndex = currentTrackList.indexOf(artistTracks[nextIndexInArtistsTracks])
                Log.d("nextIndex", "Следующий индекс артиста: $nextIndex")

                if (trackHistory.size > 20) {
                    trackHistory.removeAt(0)
                }
                trackHistory.add(playingTrackIndex)
            }
            2 -> {
                nextIndex = playingTrackIndex
                Log.d("nextIndex", "Повторение текущего трека: $nextIndex")
            }
            else -> {
                nextIndex = (playingTrackIndex + 1) % currentTrackList.size
                Log.d("nextIndex", "Следующий индекс по умолчанию: $nextIndex")
            }
        }

        onTrackClick(currentTrackList[nextIndex])
        Log.d("onNextTrack", "Переключен на индекс: $playingTrackIndex")
        Log.d("onNextTrack", "Переключен на трек: ${currentTrackList[nextIndex].title}")
    }

    val onPrevTrack: () -> Unit = {
        if (trackHistory.size >= 1) {

            val prevIndex = trackHistory.last()
            trackHistory.removeAt(trackHistory.size - 1)

            onTrackClick(viewModel.sampleTracks[prevIndex])
            Log.d("onPrevTrack", "Переключен на трек: ${currentTrackList[prevIndex].title}")
        } else {
            onTrackClick(viewModel.sampleTracks[playingTrackIndex])
            Log.d("onPrevTrack", "Переключен на трек: ${currentTrackList[playingTrackIndex].title}")
        }
    }

    serviceConnection.player?.addListener(object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                onNextTrack()
            }
        }
    })

    val onMixToggle: () -> Unit = {
        isRandomMode = !isRandomMode
    }

    val onExpandPlayer: () -> Unit = {
        isPlayerExpanded = !isPlayerExpanded
    }

    val onSettingsScreen: () -> Unit = {
        isSettingsScreen = !isSettingsScreen
    }

    val onRepeatTrackChange: () -> Unit = {
        isRepeatTrack++
        if (isRepeatTrack == 3)
            isRepeatTrack = 0
    }

    val onPlayPauseClick: () -> Unit = {
        serviceConnection.pausePlay()
    }

    var bufferedPosition by remember { mutableStateOf(0f) }

    LaunchedEffect(serviceConnection.player) {
        serviceConnection.player?.let { exoPlayer ->
            while (true) {
                bufferedPosition = exoPlayer.bufferedPercentage / 100f
                Log.d("loading", "loading - $bufferedPosition")
                delay(1000) // Обновляем каждые 500 мс
            }
        }
    }

    LaunchedEffect(buttonFromNot) {
        Log.d("MusicAppScreen", "Получено действие: $buttonFromNot")
        when (buttonFromNot) {
            1L -> onNextTrack()
            2L -> onPrevTrack()
            3L -> currentTrack?.let { onFavouriteToggle(it) }
        }
    }

    var trackInfo by remember { mutableStateOf<Track?>(null) }

    val onShowInfo: (Track) -> Unit = { track ->
        isTrackDetailsScreen = !isTrackDetailsScreen
        trackInfo = track
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        if(!isTrackDetailsScreen){
            if (!isSettingsScreen) {
                if (!isAddingPlaylistScreen) {
                    if (!isAddingTrackScreen) {
                        if (!isPlayerExpanded) {
                            Column(modifier = Modifier.weight(1f)) {
                                when (selectedItem) {
                                    0 -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp)
                                        ) {
                                            Spacer(modifier = Modifier.width(30.dp))
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "MUSIC",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color(0xFFCACDD2),
                                                fontSize = 26.sp,
                                                textAlign = TextAlign.Center,
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                onClick = onSettingsScreen,
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_settings),
                                                    contentDescription = "Settings",
                                                    modifier = Modifier.size(28.dp),
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                        TrackList(
                                            tracks = viewModel.sampleTracks,
                                            onTrackClick = onTrackClick,
                                            onFavouriteToggle = onFavouriteToggle,
                                            currentTrack = currentTrack,
                                            isPlaying = isPlaying,
                                            isAdding = false,
                                            isPlaylist = false,
                                            onAddToPlaylist = { track ->
                                                trackForAddToPlaylist = track
                                                Log.d(
                                                    "AddTrackToPlaylist",
                                                    "track for adding: $trackForAddToPlaylist"
                                                )
                                                isAddingTrackScreen = true
                                            },
                                            onRemoveFromPlaylist = {},
                                            onShowInfo = onShowInfo
                                        )
                                    }

                                    1 -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp)
                                        ) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "FAVOURITES",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color(0xFFCACDD2),
                                                fontSize = 26.sp,
                                                textAlign = TextAlign.Center,
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                        }

                                        TrackList(
                                            tracks = favouriteTracks,
                                            onTrackClick = onTrackClick,
                                            onFavouriteToggle = onFavouriteToggle,
                                            currentTrack = currentTrack,
                                            isPlaying = isPlaying,
                                            isAdding = false,
                                            isPlaylist = false,
                                            onAddToPlaylist = { track ->
                                                trackForAddToPlaylist = track
                                                Log.d(
                                                    "AddTrackToPlaylist",
                                                    "track for adding: $track"
                                                )
                                                isAddingTrackScreen = true
                                            },
                                            onRemoveFromPlaylist = {},
                                            onShowInfo = onShowInfo
                                        )
                                    }

                                    2 -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp)
                                        ) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "PLAYLISTS",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color(0xFFCACDD2),
                                                fontSize = 26.sp,
                                                textAlign = TextAlign.Center,
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                        PlaylistsScreen(
                                            playlists = viewModel.samplePlaylists,
                                            onAddPlaylist = {
                                                isAddingPlaylistScreen = true
                                            },
                                            onRemovePlaylist = { playlist: Playlist ->
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    try {
                                                        // Удаление плейлиста с сервера
                                                        val response = playlist.id?.let {
                                                            apiClient.playlistApiService.deletePlaylist(
                                                                it
                                                            ).execute()
                                                        }

                                                        if (response != null) {
                                                            if (response.isSuccessful) {
                                                                // Успешно удалено, теперь удаляем из локального списка
                                                                viewModel.samplePlaylists.remove(
                                                                    playlist
                                                                )
                                                            } else {
                                                                // Обработка ошибки, если удаление не удалось
                                                                Log.e(
                                                                    "RemovePlaylist",
                                                                    "Failed to delete playlist: ${
                                                                        response.errorBody()
                                                                            ?.string()
                                                                    }"
                                                                )
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e(
                                                            "RemovePlaylist",
                                                            "Error occurred while deleting playlist: ${e.message}"
                                                        )
                                                    }
                                                }
                                            },
                                            currentTrack = currentTrack,
                                            isPlaying = isPlaying,
                                            onTrackClick = onTrackClick,
                                            onFavouriteToggle = onFavouriteToggle,
                                            currentPlaylist = currentPlaylist,
                                            onChangeCurrentPlaylist = onChangeCurrentPlaylist,
                                            allTracks = viewModel.sampleTracks,
                                            onAddToPlaylist = { track ->
                                                trackForAddToPlaylist = track
                                                Log.d(
                                                    "AddTrackToPlaylist",
                                                    "track for adding: $track"
                                                )
                                                isAddingTrackScreen = true
                                            },
                                            viewModel = viewModel,
                                            apiClient = apiClient,
                                            onShowInfo = onShowInfo
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        if (currentTrack != null) {
                            MiniPlayer(
                                track = currentTrackList[playingTrackIndex],
                                isExpanded = isPlayerExpanded,
                                onExpandClick = onExpandPlayer,
                                onNextTrack = onNextTrack,
                                onPrevTrack = onPrevTrack,
                                isPlaying = isPlaying,
                                onPlayPauseClick = onPlayPauseClick,
                                currentTime = currentTime,
                                trackDuration = if (trackDuration != C.TIME_UNSET) {
                                    trackDuration / 1000L
                                } else {
                                    1000000L
                                },
                                onSeekTo = onSeekTo,
                                onFavouriteToggle = onFavouriteToggle,
                                onMixToggle = onMixToggle,
                                isRandomMode = isRandomMode,
                                isRepeatTrack = isRepeatTrack,
                                onRepeatTrackChange = onRepeatTrackChange,
                                bufferedPosition = bufferedPosition
                            )
                        }

                        BottomNavigationBar(
                            selectedItem = selectedItem,
                            onItemSelected = { selectedItem = it }
                        )
                    } else {
                        Log.d("AddTrackToPlaylist", "track for adding: $trackForAddToPlaylist")
                        AddTrackToPlaylistsScreen(
                            track = trackForAddToPlaylist,
                            playlists = viewModel.samplePlaylists,
                            onCollapse = {
                                isAddingTrackScreen = false
                                Log.d("AddTrackToPlaylist", "end of adding")
                                trackForAddToPlaylist = null
                            },
                            viewModel = viewModel,
                            apiClient = apiClient
                        )
                    }
                } else {
                    AddPlaylistScreen(
                        onCollapse = {
                            isAddingPlaylistScreen = false
                        },
                        onCreatePlaylist = { name ->
                            val description = ""

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val response =
                                        apiClient.playlistApiService.createPlaylist(
                                            name,
                                            description
                                        )
                                            .execute()

                                    if (response.isSuccessful) {
                                        response.body()?.let { createdPlaylist ->
                                            createdPlaylist.tracks = mutableListOf()
                                            viewModel.samplePlaylists.add(createdPlaylist)
                                            isAddingPlaylistScreen = false
                                        }
                                    } else {
                                        Log.e(
                                            "AddPlaylistScreen",
                                            "Error creating playlist: ${
                                                response.errorBody()?.string()
                                            }"
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e(
                                        "AddPlaylistScreen",
                                        "Error occurred while creating playlist: ${e.message}"
                                    )
                                }
                            }
                        }
                    )
                }
            } else {
                if (user != null) {
                    Log.d("SETTINGS", "settingsscreen")
                    SettingsScreen(
                        onLogoutSuccess = {
                            isSettingsScreen = false
                            onLogoutSuccess()
                        },
                        apiClient = apiClient,
                        onCollapse = { isSettingsScreen = false },
                        viewModel = viewModel,
                        user = user
                    )
                }
            }
        } else {
            trackInfo?.let { TrackDetailsScreen(track = it, onBack = { isTrackDetailsScreen = false }) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MusicPlatformTheme {
        val currentTrack by remember { mutableStateOf<Track?>(null) }
        val playingTrackIndex by remember { mutableIntStateOf(0) }
        val context = LocalContext.current

        MusicAppScreen(
            onTrackClickMain = { track ->
//                playTrack(track, currentTrack)
//                currentTrack = track
//                playingTrackIndex = currentTrackList.indexOfFirst { it.track == track.track }
            },
            currentTrack = currentTrack,
            playingTrackIndex = playingTrackIndex,
//            mediaPlayer = mediaPlayer,
            currentTrackList = mutableListOf(),
            onChangeCurTrackList = { curList ->
//                currentTrackList = curList
//                Log.d("current playlist", "playlist: $currentTrackList")
            },
            onLogoutSuccess = {
//                isLoggedIn = false
//                token = null
            },
            apiClient = ApiClient(context),
            context = context,
            user = null
        )
    }
}
