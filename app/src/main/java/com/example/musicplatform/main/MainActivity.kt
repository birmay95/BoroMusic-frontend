package com.example.musicplatform.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicplatform.tracks.RecommendationScreen
import com.example.musicplatform.api.ApiClient
import com.example.musicplatform.authorization.LoginScreen
import com.example.musicplatform.players.FullPlayerScreen
import com.example.musicplatform.players.MiniPlayer
import com.example.musicplatform.playlists.AddPlaylistScreen
import com.example.musicplatform.playlists.AddTrackToPlaylistsScreen
import com.example.musicplatform.playlists.PlaylistsScreen
import com.example.musicplatform.settings.SettingsScreen
import com.example.musicplatform.model.Playlist
import com.example.musicplatform.model.Track
import com.example.musicplatform.tracks.TrackDetailsScreen
import com.example.musicplatform.model.User
import com.example.musicplatform.ui.theme.MusicPlatformTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContent {
            MusicPlatformTheme {
                val navController = rememberNavController()

                var currentTrack by remember { mutableStateOf<Track?>(null) }
                var playingTrackIndex by remember { mutableIntStateOf(0) }
                var currentTrackList by remember { mutableStateOf<List<Track>>(emptyList()) }

                val context = applicationContext
                val apiClient = ApiClient(context)
                var token = apiClient.getJwtToken(context)
                var isLoggedIn by remember { mutableStateOf(token != null) }
                val startDestination = if (isLoggedIn) "musicApp" else "login"
                var user: User = apiClient.getUser(context)

                val granted = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                Log.i("Permissions", "POST_NOTIFICATIONS разрешение: $granted")

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { userLogged ->
                                user = userLogged
                                navController.navigate("musicApp") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            apiClient = apiClient
                        )
                    }
                    composable("musicApp") {
                        MusicAppScreen(
                            onTrackClickMain = { track ->
                                currentTrack = track
                                playingTrackIndex =
                                    currentTrackList.indexOfFirst { it.id == track.id }
                            },
                            currentTrack = currentTrack,
                            playingTrackIndex = playingTrackIndex,
                            currentTrackList = currentTrackList,
                            onChangeCurTrackList = { curList ->
                                currentTrackList = curList
                            },
                            onChangePlayingTrackIndex = { newIndex ->
                                playingTrackIndex = newIndex
                            },
                            onLogoutSuccess = {
                                token = null
                                navController.navigate("login") {
                                    popUpTo("musicApp") { inclusive = true }
                                }
                            },
                            apiClient = apiClient,
                            context = context,
                            user = user,
                            onLoadDataFailed = {
                                Toast
                                    .makeText(
                                        context,
                                        "The token has expired, you need to log in again.",
                                        Toast.LENGTH_LONG
                                    )
                                    .show()
                                navController.navigate("login") {
                                    popUpTo("musicApp") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
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
    onChangePlayingTrackIndex: (Int) -> Unit,
    onLogoutSuccess: () -> Unit,
    apiClient: ApiClient,
    context: Context,
    user: User?,
    onLoadDataFailed: () -> Unit
) {
    val viewModel: MyViewModel = viewModel()
    LaunchedEffect(Unit) {
        if (user != null) {
            user.id?.let {
                viewModel.loadSampleData(
                    apiClient = apiClient,
                    userId = it,
                    onLoadDataFailed = onLoadDataFailed
                )
            }
        }
    }

    val musicNavController = rememberNavController()

    val trackHistory = remember { mutableListOf<Int>() }
    var isRandomMode by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableIntStateOf(0) }
    var isRepeatTrack by remember { mutableIntStateOf(0) }

    var isPlayerExpanded by remember { mutableStateOf(false) }
    val playedTracks = remember { mutableStateListOf<Track>() }

    var currentPlaylist by remember { mutableIntStateOf(1) }
    var trackForAddToPlaylist by remember { mutableStateOf<Track?>(null) }

    val onChangeCurrentPlaylist: (Int) -> Unit = { newIndex ->
        currentPlaylist = newIndex
    }

    val serviceConnection = remember { MusicServiceConnection(context) }
    LaunchedEffect(Unit) {
        serviceConnection.bindService()
    }

    val currentTime = serviceConnection.currentTime.collectAsState(initial = 0L).value

    val trackDuration = serviceConnection.trackDuration.collectAsState(initial = 0L).value

    val buttonFromNot = serviceConnection.button.collectAsState(initial = 0L).value

    val isPlaying = serviceConnection.isPlaying.collectAsState(initial = false).value

    val playTrackFromServer = { _: Track, fileName: String ->
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val url = "http://192.168.117.15:8080/tracks/download/$fileName"
                serviceConnection.playTrack(url)
            } catch (e: Exception) {
                Log.e("NetworkError", "Error loading the stream: ${e.message}")
            }
        }
    }

    val onFavouriteToggle: (Track) -> Unit = { track ->
        CoroutineScope(Dispatchers.IO).launch {
            if (!track.favourite) {
                if (user != null) {
                    user.id?.let {
                        track.id?.let { it1 ->
                            apiClient.userApiService.addToFavourites(
                                it,
                                it1
                            )
                        }
                    }
                }
            } else {
                if (user != null) {
                    user.id?.let {
                        track.id?.let { it1 ->
                            apiClient.userApiService.removeFromFavourites(
                                it,
                                it1
                            )
                        }
                    }
                }
            }
        }

        val updatedTrack = track.copy(
            favourite = !track.favourite,
            playlists = track.playlists ?: listOf(),
            genres = track.genres ?: listOf(),
            rotation = track.rotation
        )
        val index = viewModel.sampleTracks.indexOfFirst { it.id == track.id }
        if (index != -1) {
            viewModel.sampleTracks[index] = updatedTrack
        } else {
            Log.e("OnFavouriteToggle", "Track not found in sampleTracks.")
        }

        viewModel.samplePlaylists.forEachIndexed { playlistIndex, playlist ->
            val updatedTracks = playlist.tracks.map { playlistTrack ->
                if (playlistTrack.id == track.id) {
                    playlistTrack.copy(
                        favourite = !playlistTrack.favourite,
                        playlists = playlistTrack.playlists ?: listOf(),
                        genres = playlistTrack.genres ?: listOf(),
                        rotation = playlistTrack.rotation ?: Animatable(0f)
                    )
                } else {
                    playlistTrack
                }
            }.toMutableList()

            val updatedPlaylist = playlist.copy(tracks = updatedTracks)
            viewModel.samplePlaylists[playlistIndex] = updatedPlaylist
        }

        viewModel.favouriteTracks = mutableStateListOf<Track>().apply {
            addAll(viewModel.sampleTracks.filter { it.favourite })
        }

        val recIndex = viewModel.recommendations.indexOfFirst { it.id == track.id }
        if (recIndex != -1) {
            viewModel.recommendations[recIndex] = updatedTrack
        }
    }

    val onSeekTo: (Int) -> Unit = { newPosition ->
        serviceConnection.seekTo(newPosition)
    }

    val onTrackClick: (Track) -> Unit = { track ->
        val curTrackList = when (selectedItem) {
            0 -> viewModel.sampleTracks
            1 -> viewModel.favouriteTracks
            2 -> viewModel.samplePlaylists[currentPlaylist].tracks
            else -> viewModel.recommendations
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
            1 -> viewModel.favouriteTracks
            2 -> viewModel.samplePlaylists[currentPlaylist].tracks
            else -> viewModel.recommendations
        }
        onChangeCurTrackList(curTrackList)

        if (curTrackList.isEmpty()) {
            Log.e("onNextTrack", "The track list is empty, switching is not possible.")
        }

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
                    }
                }

                if (trackHistory.size > 20) {
                    trackHistory.removeAt(0)
                }
                trackHistory.add(playingTrackIndex)
            }

            1 -> {
                val artistTracks = currentTrackList.filter { track ->
                    currentTrack?.artist?.let { currentArtist ->
                        val mainCurrentArtist = currentArtist.split(
                            Regex(
                                "[,;&]|\\s+feat\\.?\\s+",
                                RegexOption.IGNORE_CASE
                            )
                        )
                            .first().trim().lowercase()

                        val trackArtists = track.artist.split(
                            Regex(
                                "[,;&]|\\s+feat\\.?\\s",
                                RegexOption.IGNORE_CASE
                            )
                        )
                            .map { it.trim().lowercase() }

                        trackArtists.any { it.contains(mainCurrentArtist, ignoreCase = true) }
                    } ?: false
                }

                val currentArtistTrackIndex = artistTracks.indexOf(currentTrack)

                val nextIndexInArtistsTracks = if (artistTracks.isNotEmpty()) {
                    (currentArtistTrackIndex + 1) % artistTracks.size
                } else {
                    playingTrackIndex
                }

                nextIndex = currentTrackList.indexOf(artistTracks[nextIndexInArtistsTracks])

                if (trackHistory.size > 20) {
                    trackHistory.removeAt(0)
                }
                trackHistory.add(playingTrackIndex)
            }

            2 -> {
                nextIndex = playingTrackIndex
            }

            else -> {
                nextIndex = (playingTrackIndex + 1) % currentTrackList.size
            }
        }
        onChangePlayingTrackIndex(nextIndex)
        onTrackClick(currentTrackList[nextIndex])
    }

    val onPrevTrack: () -> Unit = {
        if (trackHistory.size >= 1) {

            val prevIndex = trackHistory.last()
            trackHistory.removeAt(trackHistory.size - 1)

            onTrackClick(viewModel.sampleTracks[prevIndex])
        } else {
            onTrackClick(viewModel.sampleTracks[playingTrackIndex])
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
        musicNavController.navigate("full_player")
    }

    val onRepeatTrackChange: () -> Unit = {
        isRepeatTrack++
        if (isRepeatTrack == 3)
            isRepeatTrack = 0
    }

    val onPlayPauseClick: () -> Unit = {
        serviceConnection.pausePlay()
    }

    val changeTrackForAddToPlaylist: (Track) -> Unit = { track ->
        trackForAddToPlaylist = track
    }

    var bufferedPosition by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(serviceConnection.player) {
        serviceConnection.player?.let { exoPlayer ->
            while (true) {
                bufferedPosition = exoPlayer.bufferedPercentage / 100f
                delay(1000)
            }
        }
    }

    LaunchedEffect(buttonFromNot) {
        when (buttonFromNot) {
            1L -> onNextTrack()
            2L -> onPrevTrack()
            3L -> currentTrack?.let { onFavouriteToggle(it) }
        }
    }

    var trackInfo by remember { mutableStateOf<Track?>(null) }

    val onShowInfo: (Track) -> Unit = { track ->
        musicNavController.navigate("track_detail_screen")
        trackInfo = track
    }

    val onShowRecs: (Track) -> Unit = { track ->
        musicNavController.navigate("track_recs_screen")
        trackInfo = track
    }
    val pagerState = rememberPagerState(pageCount = { 3 })
    LaunchedEffect(selectedItem) {
        pagerState.animateScrollToPage(selectedItem)
    }

    NavHost(navController = musicNavController, startDestination = "main") {
        composable("main") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0E1A))
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                if (!isPlayerExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 48.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (page) {
                                0 -> {
                                    HomeScreen(
                                        navController = musicNavController,
                                        tracks = viewModel.sampleTracks,
                                        onTrackClick = onTrackClick,
                                        onFavouriteToggle = onFavouriteToggle,
                                        currentTrack = currentTrack,
                                        isPlaying = isPlaying,
                                        onShowInfo = onShowInfo,
                                        changeTrackForAddToPlaylist = changeTrackForAddToPlaylist,
                                        text = "MUSIC",
                                        onShowRecs = onShowRecs
                                    )
                                }

                                1 -> {
                                    HomeScreen(
                                        navController = musicNavController,
                                        tracks = viewModel.favouriteTracks,
                                        onTrackClick = onTrackClick,
                                        onFavouriteToggle = onFavouriteToggle,
                                        currentTrack = currentTrack,
                                        isPlaying = isPlaying,
                                        onShowInfo = onShowInfo,
                                        changeTrackForAddToPlaylist = changeTrackForAddToPlaylist,
                                        text = "FAVOURITES",
                                        onShowRecs = onShowRecs
                                    )
                                }

                                2 -> {
                                    if (user != null) {
                                        PlaylistsScreen(
                                            playlists = viewModel.samplePlaylists,
                                            onAddPlaylist = {
                                                musicNavController.navigate("add_playlist_screen")
                                            },
                                            onRemovePlaylist = { playlist: Playlist ->
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    try {
                                                        val response =
                                                            playlist.id?.let {
                                                                apiClient.playlistApiService.deletePlaylist(
                                                                    it
                                                                ).execute()
                                                            }

                                                        if (response != null) {
                                                            if (response.isSuccessful) {
                                                                viewModel.samplePlaylists.remove(
                                                                    playlist
                                                                )
                                                                viewModel.sampleUserPlaylists.remove(
                                                                    playlist
                                                                )
                                                            } else {
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
                                                musicNavController.navigate("add_track_to_playlist_screen")
                                            },
                                            viewModel = viewModel,
                                            apiClient = apiClient,
                                            onShowInfo = onShowInfo,
                                            user = user,
                                            onShowRecs = onShowRecs
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (currentTrack != null) {
                    MiniPlayer(
                        track = currentTrackList[playingTrackIndex],
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
                        onFavouriteToggle = onFavouriteToggle,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }

                BottomNavigationBar(
                    selectedItem = pagerState.currentPage,
                    onItemSelected = {
                        selectedItem = it
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
        composable("add_track_to_playlist_screen") {
            if (user != null) {
                AddTrackToPlaylistsScreen(
                    track = trackForAddToPlaylist,
                    playlists = viewModel.samplePlaylists,
                    onCollapse = {
                        musicNavController.popBackStack()
                        trackForAddToPlaylist = null
                    },
                    viewModel = viewModel,
                    apiClient = apiClient,
                    user = user

                )
            }
        }
        composable("add_playlist_screen") {
            AddPlaylistScreen(
                onCollapse = {
                    musicNavController.popBackStack()
                },
                onCreatePlaylist = { name, description ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response =
                                user?.let { it1 ->
                                    it1.id?.let { it2 ->
                                        apiClient.playlistApiService.createPlaylist(
                                            name,
                                            description,
                                            it2
                                        )
                                            .execute()
                                    }
                                }
                            if (response != null) {
                                if (response.isSuccessful) {
                                    response.body()?.let { createdPlaylist ->
                                        createdPlaylist.tracks = mutableListOf()
                                        viewModel.samplePlaylists.add(createdPlaylist)
                                        viewModel.sampleUserPlaylists.add(createdPlaylist)
                                        CoroutineScope(Dispatchers.Main).launch {
                                            musicNavController.popBackStack()
                                        }
                                    }
                                } else {
                                    Log.e(
                                        "AddPlaylistScreen",
                                        "Error creating playlist: ${
                                            response.errorBody()?.string()
                                        }"
                                    )
                                }
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
        composable("settings_screen") {
            if (user != null) {
                SettingsScreen(
                    onLogoutSuccess = {
                        onLogoutSuccess()
                    },
                    apiClient = apiClient,
                    onCollapse = {
                        musicNavController.popBackStack()
                    },
                    viewModel = viewModel,
                    user = user
                )
            }
        }
        composable("track_detail_screen") {
            trackInfo?.let {
                TrackDetailsScreen(track = it, onBack = {
                    musicNavController.popBackStack()
                })
            }
        }
        composable("track_recs_screen") {
            selectedItem = 4
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0E1A))
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                if (!isPlayerExpanded) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        trackInfo?.let { it1 ->
                            RecommendationScreen(
                                viewModel = viewModel,
                                track = it1,
                                apiClient = apiClient,
                                onBack = {
                                    musicNavController.popBackStack()
                                },
                                onTrackClick = onTrackClick,
                                onFavouriteToggle = onFavouriteToggle,
                                currentTrack = currentTrack,
                                isPlaying = isPlaying,
                                isAdding = false,
                                isUserPlaylist = false,
                                isPlaylist = false,
                                onAddToPlaylist = { track ->
                                    changeTrackForAddToPlaylist(track)
                                    musicNavController.navigate("add_track_to_playlist_screen")
                                },
                                onRemoveFromPlaylist = {},
                                onShowInfo = onShowInfo,
                                onShowRecs = onShowRecs
                            )
                        }
                    }
                }
                if (currentTrack != null) {
                    MiniPlayer(
                        track = currentTrackList[playingTrackIndex],
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
                        onFavouriteToggle = onFavouriteToggle,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
        composable("full_player") {
            FullPlayerScreen(
                track = currentTrackList[playingTrackIndex],
                onCollapse = {
                    musicNavController.popBackStack()
                },
                onNextTrack = onNextTrack,
                onPrevTrack = onPrevTrack,
                isPlaying = isPlaying,
                onPlayPauseClick = onPlayPauseClick,
                currentPosition = currentTime,
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
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MusicPlatformTheme {
        val context = LocalContext.current

        MusicAppScreen(
            onTrackClickMain = {},
            currentTrack = null,
            playingTrackIndex = 0,
            currentTrackList = mutableListOf(),
            onChangeCurTrackList = {},
            onChangePlayingTrackIndex = {},
            onLogoutSuccess = {},
            apiClient = ApiClient(context),
            context = context,
            user = null,
            onLoadDataFailed = {}
        )
    }
}
