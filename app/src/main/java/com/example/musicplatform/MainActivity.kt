package com.example.musicplatform

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplatform.ui.theme.MusicPlatformTheme
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
//import com.google.android.exoplayer2.C


class MainActivity : ComponentActivity() {
//    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        setContent {
            MusicPlatformTheme {
                var currentTrack by remember { mutableStateOf<Track?>(null) }
                var playingTrackIndex by remember { mutableIntStateOf(0) }
                var currentTrackList by remember { mutableStateOf<List<Track>>(emptyList())}

                val context = LocalContext.current
                val apiClient = ApiClient(context)
                var token = apiClient.getJwtToken(context)
                var isLoggedIn by remember { mutableStateOf(token != null) }

                val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                Log.d("Permissions", "POST_NOTIFICATIONS разрешение: $granted")



                if (!isLoggedIn) {
                    LoginScreen(onLoginSuccess = {
                        isLoggedIn = true
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
//                        mediaPlayer = mediaPlayer,
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
                        context = context
                    )
                }
            }
        }
    }

//    private fun playTrackFromFile(file: File) {
//        mediaPlayer?.release()
//        mediaPlayer = MediaPlayer()
//
//        try {
//            mediaPlayer?.setDataSource(file.absolutePath)
//            mediaPlayer?.prepare()
//            mediaPlayer?.start()
//        } catch (e: IOException) {
//            Log.e("MediaPlayerError", "Ошибка воспроизведения: ${e.message}")
//        }
//    }



    override fun onDestroy() {
        super.onDestroy()
//        mediaPlayer?.release()
//        mediaPlayer = null
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

data class ServerTrack(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("artist")
    val artist: String,
    @SerializedName("album")
    val album: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("contentType")
    val contentType: String,
    @SerializedName("fileSize")
    val fileSize: Long,
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("genres")
    val genres: List<Genre> = emptyList(),
    @SerializedName("playlists")
    val playlists: List<Playlist> = emptyList()
)

data class Track(
    val id: Long? = null,
    val title: String,
    val artist: String,
    val album: String,
    val fileName: String,
    val contentType: String,
    val fileSize: Long,
    val duration: Long,
    val genres: List<Genre> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    var favourite: Boolean = false,
    var track: Int = 0,
    var rotation: Animatable<Float, AnimationVector1D> = Animatable(0f)
)

fun mapServerTrackToTrack(serverTrack: ServerTrack, favourite: Boolean, track: Int): Track {
    return Track(
        id = serverTrack.id,
        title = serverTrack.title,
        artist = serverTrack.artist,
        album = serverTrack.album,
        fileName = serverTrack.fileName,
        contentType = serverTrack.contentType,
        fileSize = serverTrack.fileSize,
        duration = serverTrack.duration,
        genres = serverTrack.genres ?: emptyList(), // Используйте пустой список по умолчанию
        playlists = serverTrack.playlists ?: emptyList(), // Используйте пустой список по умолчанию
        favourite = favourite,
        track = track,
        rotation = Animatable(0f)
    )
}

//var sampleTracks = listOf(
//    Track("Mockingbird", "Eminem", R.raw.eminem_mockingbird, true),
//    Track("Sweater Weather", "The Neighbourhood", R.raw.the_neighbourhood_sweater_weather, false),
//    Track("Superman", "Eminem feat. Dina Rae", R.raw.eminem_feat_dina_rae_superman, false),
//    Track("Можно я с тобой", "APSENT", R.raw.apsent_maybe_i_am_with_you, false)
//)

data class Playlist(
    @SerializedName("id")  // Сопоставляем с полем id в Java классе
    val id: Long? = null,  // Идентификатор плейлиста
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("tracks")
    var tracks: MutableList<Track> = mutableListOf() // Используем MutableSet для изменения треков
) {
    fun addTrack(track: Track) {
        tracks.add(track)
    }

    fun removeTrack(track: Track) {
        tracks.remove(track)
    }
}

class MusicServiceConnection(private val context: Context) {

    private var musicService: MusicService? = null
    private var isBound = false

    // Прямой доступ к плееру
    val player: ExoPlayer?
        get() = musicService?.getPlayer()

    // StateFlow для получения текущего времени из MusicService
    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> get() = _currentTime

    private val _trackDuration = MutableStateFlow(0L)
    val trackDuration: StateFlow<Long> get() = _trackDuration

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val musicBinder = binder as MusicService.MusicBinder
            musicService = musicBinder.getService() // Получаем сам MusicService

            // Подписка на обновления времени
            serviceScope.launch {
                musicService?.currentTime?.collect { time ->
                    _currentTime.value = time
                }
            }

            serviceScope.launch {
                musicService?.trackDuration?.collect { time ->
                    _trackDuration.value = time
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            serviceScope.cancel() // Отменяем корутину при отключении
        }
    }


    // Метод для привязки сервиса
    fun bindService() {
        if (!isBound) {
            val intent = Intent(context, MusicService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            isBound = true
        }
    }

    // Метод для отсоединения от сервиса
    fun unbindService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            serviceScope.cancel() // Отменяем все корутины
            isBound = false
        }
    }

    // Метод для воспроизведения трека
    fun playTrack(url: String) {
        musicService?.playTrack(url)
    }

    // Метод для паузы
    fun pausePlay() {
//        musicService?.pausePlay()
    }

    // Метод для перемотки
    fun seekTo(position: Long) {
//        musicService?.seekTo(position)
    }
}





//val samplePlaylists = mutableListOf(
//    Playlist("My Favorites", "cool", mutableListOf(
//        Track("Можно я с тобой", "APSENT", R.raw.apsent_maybe_i_am_with_you, false),
//        Track("Sweater Weather", "The Neighbourhood", R.raw.the_neighbourhood_sweater_weather, false)
//    )),
//    Playlist("Chill Vibes", "great", mutableListOf(
//        Track("Superman", "Eminem feat. Dina Rae", R.raw.eminem_feat_dina_rae_superman, false)
//    )),
//    Playlist("Workout", "great", mutableListOf())
//)

data class Genre(
    @SerializedName("id")  // Сопоставляем с полем id в Java классе
    val id: Long? = null,  // Идентификатор жанра
    @SerializedName("name")
    val name: String, // Название жанра
    @SerializedName("tracks")
    val tracks: Set<Track> = emptySet() // Предполагается, что Track - это отдельный класс
)

//var samplePlaylists: MutableList<Playlist> = mutableListOf()
//var sampleTracks: MutableList<Track> = mutableListOf()

@OptIn(UnstableApi::class)
@Composable
fun MusicAppScreen(
    onTrackClickMain: (Track) -> Unit,
    currentTrack: Track?,
    playingTrackIndex: Int,
//    mediaPlayer: MediaPlayer?,
    currentTrackList: List<Track>,
    onChangeCurTrackList: (List<Track>) -> Unit,
    onLogoutSuccess: () -> Unit,
    apiClient: ApiClient,
    context: Context
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
    var isPlaying by remember { mutableStateOf(true) }
//    var currentTime by remember { mutableLongStateOf(0) }

    var isPlayerExpanded by remember { mutableStateOf(false) }
    val playedTracks = remember { mutableStateListOf<Track>() }

    var currentPlaylist by remember { mutableIntStateOf(1) }
    var isAddingTrackScreen by remember { mutableStateOf(false) }
    var isAddingPlaylistScreen by remember { mutableStateOf(false) }
    var trackForAddToPlaylist by remember { mutableStateOf<Track?>(null) }

    var isSettingsScreen by remember { mutableStateOf(false) }

    val onChangeCurrentPlaylist: (Int) -> Unit = { newIndex ->
        currentPlaylist = newIndex
    }

//    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
//    var player by remember { mutableStateOf<ExoPlayer?>(null) }

    val serviceConnection = remember { MusicServiceConnection(context) }
    LaunchedEffect(Unit) {
        serviceConnection.bindService()
    }

    val currentTime = serviceConnection.currentTime.collectAsState(initial = 0L).value
    val musicViewModel: MusicViewModel = viewModel()

    val trackDuration = serviceConnection.trackDuration.collectAsState(initial = 0L).value

/*    fun playTrack(track: Track, currentTrack: Track?, file: File) {
        if (currentTrack?.track == track.track) {
            mediaPlayer?.seekTo(0)
            mediaPlayer?.start()
        } else {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer()
            try {
//                mediaPlayer = MediaPlayer.create(this, track.track)
//                mediaPlayer?.start()
                mediaPlayer?.setDataSource(file.absolutePath)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
            } catch (e: IOException) {
                Log.e("MediaPlayerError", "Ошибка воспроизведения трека: ${e.message}")
                e.printStackTrace()
            }
        }
    }*/

    val playTrackFromServer = { track: Track, fileName: String ->
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val url = "http://192.168.1.103:8080/tracks/download/$fileName"
                Log.d("ExoPlayer", "Запуск воспроизведения: $fileName")

                val intent = Intent(context, MusicService::class.java).apply {
                    action = MusicService.ACTION_PLAY
                    putExtra(MusicService.EXTRA_TRACK_URL, url)
                }
                context.startService(intent)

            } catch (e: Exception) {
                Log.e("NetworkError", "Ошибка загрузки потока: ${e.message}")
            }
        }
    }




// доделать с плейлистами, чтобы пробегаться по им всем и убирать оттуда сердечко
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

// Обновление текущего времени каждую секунду
//    LaunchedEffect(player) {
//        while (true) {
//            delay(1000L) // Обновляем каждую секунду
//            if (player?.isPlaying == true) {
//                try {
//                    currentTime =
//                        (player?.currentPosition ?: 0L) / 1000L // Получаем текущую позицию в секундах
//                } catch (e: IllegalStateException) {
//                    Log.e("MusicAppScreen", "ExoPlayer is in an invalid state: ${e.message}")
//                    break // Останавливаем цикл, если произошла ошибка
//                }
//            }
//        }
//    }

// Обработчик для перемотки
    val onSeekTo: (Int) -> Unit = { newPosition ->
//        player?.seekTo(newPosition * 1000L) // Переводим позицию в миллисекунды
//        currentTime = newPosition.toLong() // Обновляем состояние текущего времени
        val seekIntent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SEEK
            putExtra(MusicService.EXTRA_SEEK_POSITION, newPosition) // Переводим секунды в миллисекунды
        }
        context.startService(seekIntent)
    }

    val onTrackClick: (Track) -> Unit = { track ->
        val curTrackList = when (selectedItem) {
            0 -> viewModel.sampleTracks
            1 -> favouriteTracks
            else -> viewModel.samplePlaylists[currentPlaylist].tracks
        }
        onChangeCurTrackList(curTrackList)
        isPlaying = true
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

        isPlaying = true
        musicViewModel.setCurrentTrack(currentTrackList[nextIndex])
        onTrackClick(currentTrackList[nextIndex])
        Log.d("onNextTrack", "Переключен на индекс: $playingTrackIndex")
        Log.d("onNextTrack", "Переключен на трек: ${currentTrackList[nextIndex].title}")
    }

    val onPrevTrack: () -> Unit = {
//        Log.d("trackHistory", "Размер ${trackHistory.size}, prevINdex: ${trackHistory.last()}")

        if (trackHistory.size >= 1) {

//            Log.d("trackHistory", "Размер ${trackHistory.size}, prevINdex: ${trackHistory.last()}")

            val prevIndex = trackHistory.last()
            trackHistory.removeAt(trackHistory.size - 1)

            isPlaying = true
            onTrackClick(viewModel.sampleTracks[prevIndex])
            Log.d("onPrevTrack", "Переключен на трек: ${currentTrackList[prevIndex].title}")
        } else {
            isPlaying = true
            musicViewModel.setCurrentTrack(viewModel.sampleTracks[playingTrackIndex])
            onTrackClick(viewModel.sampleTracks[playingTrackIndex])
            Log.d("onPrevTrack", "Переключен на трек: ${currentTrackList[playingTrackIndex].title}")
        }
    }

//    player?.addListener(object : Player.Listener {
//        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//            if (playbackState == Player.STATE_ENDED) {
//                // Когда трек закончился, переключаемся на следующий
//                onNextTrack()  // Здесь вызываем метод для переключения на следующий трек
//            }
//        }
//    })

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

// Обработчик кнопки воспроизведения/паузы
    val onPlayPauseClick: () -> Unit = {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_PAUSE
        }
        context.startService(intent)
        isPlaying = !isPlaying
    }

    // Индикатор загрузки
//    var loadingProgress by remember { mutableStateOf(0f) }
//
//// Устанавливаем слушатель для отслеживания прогресса загрузки
//    player?.addListener(object : Player.Listener {
//        override fun onPlaybackStateChanged(state: Int) {
//            // Если трек проигрывается, проверяем прогресс загрузки
//            if (state == Player.STATE_BUFFERING) {
//                val bufferedPosition = player?.bufferedPosition ?: 0
//                val duration = player?.duration ?: 0L
//                loadingProgress = bufferedPosition.toFloat() / duration.toFloat()
//                Log.d("ExoPlayer", "load - $loadingProgress")
//            }
//        }
//    })

    var bufferedPosition by remember { mutableStateOf(0f) }

//    LaunchedEffect(player) {
//        player?.let { exoPlayer ->
//            while (true) {
//                bufferedPosition = exoPlayer.bufferedPercentage / 100f
//                Log.d("loading", "loading - $bufferedPosition")
//                delay(1000) // Обновляем каждые 500 мс
//            }
//        }
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        if(!isSettingsScreen) {
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
                                        onRemoveFromPlaylist = {}
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
                                            Log.d("AddTrackToPlaylist", "track for adding: $track")
                                            isAddingTrackScreen = true
                                        },
                                        onRemoveFromPlaylist = {}
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
                                                                    response.errorBody()?.string()
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
                                            Log.d("AddTrackToPlaylist", "track for adding: $track")
                                            isAddingTrackScreen = true
                                        },
                                        viewModel = viewModel,
                                        apiClient = apiClient
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
                                    apiClient.playlistApiService.createPlaylist(name, description)
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
                                        "Error creating playlist: ${response.errorBody()?.string()}"
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
            SettingsScreen(
                onLogoutSuccess = {
                    isSettingsScreen = false
                    onLogoutSuccess()
            },
                apiClient = apiClient,
                onCollapse = { isSettingsScreen = false }
            )
        }
    }
}

@Composable
fun SettingsScreen(
    onLogoutSuccess: () -> Unit,
    apiClient: ApiClient,
    onCollapse: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
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
                    text = "Settings",
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
                apiClient.logout(
                    onSuccess = {
                        onLogoutSuccess()
                    },
                    onError = { error ->
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Logout failed: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
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
//                .clickable(
//                    onClick = {},
//                    interactionSource = remember { MutableInteractionSource() },
//                    indication = rememberRipple(bounded = false, color = Color.Gray.copy(alpha = 0.2f)) // Прямоугольный эффект нажатия
//                ),
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
                    text = "Logout from the account",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }






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
    currentTime: Long,
    trackDuration: Long,
    onSeekTo: (Int) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    onMixToggle: () -> Unit,
    isRandomMode: Boolean,
    isRepeatTrack: Int,
    onRepeatTrackChange: () -> Unit,
    bufferedPosition: Float
) {
    if (isExpanded) {
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
            isRandomMode = isRandomMode,
            isRepeatTrack = isRepeatTrack,
            onRepeatTrackChange = onRepeatTrackChange,
            bufferedPosition = bufferedPosition
        )
    } else {
        val swipeThreshold = 40f
        var hasSwiped = false
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1D243D))
                .padding(8.dp)
                .clickable { onExpandClick() }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            hasSwiped = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!hasSwiped) {
                                if (dragAmount > swipeThreshold) {
                                    onPrevTrack()
                                    hasSwiped =
                                        true
                                } else if (dragAmount < -swipeThreshold) {
                                    onNextTrack()
                                    hasSwiped =
                                        true
                                }
                            }
                        }
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_music_note),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF0A0E1A)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = track.title, color = Color(0xFFC6CAEB), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = track.artist, color = Color(0xFFC6CAEB), fontSize = 12.sp)
            }
            Row {
                IconButton(onClick = {
                    onPlayPauseClick()
                }) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        Modifier.size(35.dp),
                        tint = Color(0xFF737BA5)
                    )
                }
                IconButton(onClick = {
                    onFavouriteToggle(track)
                }) {
                    Icon(
                        painter = painterResource(if (track.favourite) R.drawable.ic_favourite_true else R.drawable.ic_favorite), // Замените на ваш значок воспроизведения
                        contentDescription = if (track.favourite) "Like" else "Not like",
                        Modifier.size(30.dp),
                        tint = if (track.favourite) Color(0xFFA7D1DD) else Color(0xFF737BA5)
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { currentTime.toFloat() / trackDuration.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF8589AC)),
        )
//        // Индикатор прогресса загрузки
//        LinearProgressIndicator(
//            progress = { loadingProgress },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(1.dp)
//                .background(Color(0xFF00FF00)), // Цвет для прогресса загрузки (например, зеленый)
//        )
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
    currentPosition: Long,
    trackDuration: Long,
    onSeekTo: (Int) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    onMixToggle: () -> Unit,
    isRandomMode: Boolean,
    isRepeatTrack: Int,
    onRepeatTrackChange: () -> Unit,
    bufferedPosition: Float
) {
    val swipeThreshold = 60f
    var hasSwiped = false
    var lastDragAmount by remember { mutableFloatStateOf(0f) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D243D))
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
                    tint = Color(0xFF515A82)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {}) {
                Icon(
                    painterResource(id = R.drawable.ic_share),
                    contentDescription = "Share",
                    Modifier.size(32.dp),
                    tint = Color(0xFF515A82)
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
                        onDragStart = {
                            Log.d("SwipeGesture", "Начало свайпа")
                        },
                        onDragEnd = {
                            Log.d("SwipeGesture", "Завершение свайпа")
                            hasSwiped = false
                            lastDragAmount = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            lastDragAmount += dragAmount
                            Log.d(
                                "SwipeGesture",
                                "dragAmount: $dragAmount, lastDragAmount: $lastDragAmount"
                            )

                            if (!hasSwiped) {
                                if (lastDragAmount > swipeThreshold) {
                                    Log.d(
                                        "SwipeGesture",
                                        "Свайп вправо - переключаем на предыдущий трек"
                                    )
                                    onPrevTrack()
                                    hasSwiped = true
                                } else if (lastDragAmount < -swipeThreshold) {
                                    Log.d(
                                        "SwipeGesture",
                                        "Свайп влево - переключаем на следующий трек"
                                    )
                                    onNextTrack()
                                    hasSwiped = true
                                }
                            }
                            change.consume()
                        }
                    )
                }
,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .background(Color(0xFF353D60))
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_music_note),
                    contentDescription = "Music",
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.Center),
                    tint = Color(0xFF0A0E1A)
                )
            }
        }
        Row {
            Column {
                Text(
                    text = track.title,
                    color = Color(0xFFC6CAEB),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp)
                )
                Text(
                    text = track.artist,
                    color = Color(0xFFC6CAEB),
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
                    tint = if (track.favourite) Color(0xFFA7D1DD) else Color(0xFF737BA5)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

//        Slider(
//            value = currentPosition.toFloat(),
//            onValueChange = { onSeekTo(it.toInt()) },
//            valueRange = 0f..maxOf(trackDuration.toFloat(), 1f),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp),
//            colors = SliderDefaults.colors(
//                thumbColor = Color.White,
//                activeTrackColor = Color(0xFFA7D1DD),
//                inactiveTrackColor = Color(0xFF353D60)
//            )
//        )
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .padding(horizontal = 16.dp)
        ) {
            // Индикатор загрузки (буферизации)
            LinearProgressIndicator(
                progress = { bufferedPosition },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Gray), // Фон индикатора (фон можно убрать)
                color = Color(0xFF737BA5) // Цвет буферизации
            )

            // Основной ползунок для трека
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { onSeekTo(it.toInt()) },
                valueRange = 0f..maxOf(trackDuration.toFloat(), 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFFA7D1DD),  // Прогресс проигрывания
                    inactiveTrackColor = Color.Transparent // Делаем прозрачным, так как загрузка сверху
                )
            )
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val formattedCurrentPosition = Utils.formatTime(currentPosition)
            val formattedTrackDuration = Utils.formatTime(trackDuration)
            Log.d("loading", "time - $formattedTrackDuration")
            Log.d("loading", "time 2 - $trackDuration")
            Text(
                text = formattedCurrentPosition,
                color = Color(0xFFC6CAEB),
                fontSize = 12.sp
            )
            Text(
                text = formattedTrackDuration,
                color = Color(0xFFC6CAEB),
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
                    tint = if (isRandomMode) Color(0xFFA7D1DD) else Color(0xFF0A0E1A)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))

            IconButton(
                onClick = onPrevTrack,
                modifier = Modifier.size(50.dp)
            ){
                Icon(
                    painterResource(id = R.drawable.ic_prev),
                    contentDescription = "Prev",
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF0A0E1A)
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
                        .align(Alignment.CenterVertically),
                    tint = Color(0xFF0A0E1A)
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
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF0A0E1A)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            IconButton(
                onClick = onRepeatTrackChange,
                modifier = Modifier.size(50.dp)
            ){
                Icon(
                    painter = painterResource(
                        when(isRepeatTrack) {
                            0 -> R.drawable.ic_cycle
                            1 -> R.drawable.ic_cycle
                            2 -> R.drawable.ic_cycle_track
                            else -> { R.drawable.ic_cycle}
                        }
                    ),
                    contentDescription =
                    when(isRepeatTrack) {
                        0 -> "Not repeat"
                        1 -> "Repeat artist"
                        2 -> "Repeat track"
                        else -> { "Not repeat" }
                    },
                    modifier = Modifier.size(40.dp),
                    tint =
                    when(isRepeatTrack) {
                        0 -> Color(0xFF0A0E1A)
                        1 -> Color(0xFFA7D1DD)
                        2 -> Color(0xFFA7D1DD)
                        else -> { Color(0xFF0A0E1A) }
                    },
                )
            }
        }
    }
}

//fun formatTime(seconds: Int): String {
//    val minutes = seconds / 60
//    val remainingSeconds = seconds % 60
//    return String.format("%02d:%02d", minutes, remainingSeconds)
//}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text(text = "Search...", color = Color(0xFFC6CAEB)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFF303147), shape = RoundedCornerShape(8.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF515A82),
            unfocusedContainerColor = Color(0xFF353D60),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFFC6CAEB)
        )
    )
}

@Composable
fun TrackList(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    currentTrack: Track?,
    isPlaying: Boolean,
    isAdding: Boolean,
    isPlaylist: Boolean,
    onAddToPlaylist: (Track) -> Unit,
    onRemoveFromPlaylist: (Track) -> Unit,
    playlistTracks: List<Track> = emptyList()
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredTracks by remember(tracks, searchQuery, isAdding, playlistTracks) {
        derivedStateOf {
            tracks.filter { track ->
                val matchesSearch = track.title.contains(searchQuery, ignoreCase = true) ||
                        track.artist.contains(searchQuery, ignoreCase = true)
                val notInPlaylist = playlistTracks.none { it.id == track.id }
                matchesSearch && (!isAdding || notInPlaylist)
//                Log.d("equals", "playlistTracks: $playlistTracks")
//                var inPlaylist: Boolean = false
//                if(isAdding) {
//                    inPlaylist = playlistTracks.contains(track)
//                    Log.d("equals", "$inPlaylist of track $track")
//                }
//                matchesSearch && !inPlaylist
            }
        }
    }

    Column {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
//                .background(Color(0xFF0A0E1A))
        ) {
            items(filteredTracks) { track ->
                TrackItem(
                    track = track,
                    onClick = { onTrackClick(track) },
                    onFavouriteToggle = onFavouriteToggle,
                    isCurrent = currentTrack == track,
                    isPlaying = isPlaying,
                    isAdding = isAdding,
                    isPlaylist = isPlaylist,
                    onAddToPlaylist = onAddToPlaylist,
                    onRemoveFromPlaylist = onRemoveFromPlaylist
                )
            }
        }
    }
}

@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isAdding: Boolean,
    isPlaylist: Boolean,
    onAddToPlaylist: (Track) -> Unit,
    onRemoveFromPlaylist: (Track) -> Unit
) {

    var isMenuExpanded by remember { mutableStateOf(false) }
// Храним rotation отдельно, чтобы он не сбрасывался
    val rotation = remember { Animatable(track.rotation?.value ?: 0f) }

    LaunchedEffect(isCurrent, isPlaying) {
        if (isCurrent) {
            if (isPlaying) {
                if (!rotation.isRunning) {  // Запускаем, только если не работает
                    Log.d("rotationnn", "track rotation 1: ${rotation.value}")
                    rotation.animateTo(
                        targetValue = rotation.value + 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )
                }
            } else {
                rotation.stop()  // Останавливаем
            }
        } else {
            track.rotation = Animatable(0f)
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(500)
            )
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isCurrent) 1.3f else 1f,
        animationSpec = if (isCurrent) {
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(500)
        }, label = ""
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { isMenuExpanded = !isMenuExpanded }
                    )
                }
                .background(Color(0xFF1D243D), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_music_note),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF353D60), shape = CircleShape)
                    .padding(8.dp)
                    .rotate(rotation.value)
                    .scale(scale),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    color = Color(0xFFCACDD2),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = track.artist,
                    color = Color(0xFF8589AC),
                    fontSize = 12.sp
                )
            }
            if(!isAdding){
                IconButton(onClick = {
                    track.rotation = rotation
                    Log.d("rotationnn", "track rotation 2: ${track.rotation.value}")
                    Log.d("rotationnn", "track rotation 3: ${rotation.value}")
                    onFavouriteToggle(track)
                }) {
                    Icon(
                        painter = painterResource(if (track.favourite) R.drawable.ic_favourite_true else R.drawable.ic_favorite),
                        contentDescription = if (track.favourite) "Like" else "Not like",
                        modifier = Modifier.size(35.dp),
                        tint = if (track.favourite) Color(0xFFA7D1DD) else Color(0xFF737BA5)
                    )
                }
                IconButton(
                    onClick = { isMenuExpanded = !isMenuExpanded }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_threedots),
                        contentDescription = "Menu bar",
                        modifier = Modifier.size(30.dp),
                        tint = Color(0xFF737BA5)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .border(3.dp, Color(0xFFCACDD2), CircleShape)
                        .clickable(onClick = {
                            onAddToPlaylist(track)
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Add track to playlist",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFCACDD2)
                    )
                }
            }
        }
        if(isMenuExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(
                        Color(0xFF1D243D),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                TextButton(
                    onClick = {
                        onFavouriteToggle(track)
                        isMenuExpanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .background(
                            Color(0xFF353D60),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        if(!track.favourite) "Add to favourites" else "Remove from favourite",
                        color = Color(0xFFCACDD2),
                        fontSize = 16.sp
                    )
                }
                TextButton(
                    onClick = {
                        onAddToPlaylist(track)
                        isMenuExpanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .background(
                            Color(0xFF353D60),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        if(!isPlaylist) "Add to playlist" else "Add to another playlist",
                        color = Color(0xFFCACDD2),
                        fontSize = 16.sp
                    )
                }
                if(isPlaylist) {
                    TextButton(
                        onClick = {
                            onRemoveFromPlaylist(track)
                            isMenuExpanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                            .background(
                                Color(0xFF353D60),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            "Remove from playlist",
                            color = Color(0xFFCACDD2),
                            fontSize = 16.sp
                        )
                    }
                }
                TextButton(
                    onClick = {
//                        onShowInfo(track)
                        isMenuExpanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .background(
                            Color(0xFF353D60),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        "Get info",
                        color = Color(0xFFCACDD2),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onAddPlaylist: () -> Unit,
    onRemovePlaylist: (Playlist) -> Unit,
    currentTrack: Track?,
    isPlaying: Boolean,
    onTrackClick: (Track) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    currentPlaylist: Int,
    onChangeCurrentPlaylist: (Int) -> Unit,
    allTracks: List<Track>,
    onAddToPlaylist: (Track) -> Unit,
    viewModel: MyViewModel,
    apiClient: ApiClient
) {
    var currentScreen by remember { mutableIntStateOf(0) }

    if (currentScreen == 0) {
        var searchQuery by remember { mutableStateOf("") }

        val filteredPlaylists = playlists.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }

        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
        )

        Box(modifier = Modifier
            .fillMaxSize()
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {


                LazyColumn {
                    items(filteredPlaylists) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = {
                                currentScreen = 1
                                onChangeCurrentPlaylist(playlists.indexOf(playlist))
                            },
                            isAdding = false,
                            onRemovePlaylist = onRemovePlaylist
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { onAddPlaylist() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(50.dp),
                containerColor = Color(0xFF3F4F82)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Добавить плейлист",
                    tint = Color(0xFF020F17)
                )
            }
        }
    } else {
        PlaylistDetailScreen(
            playlist = playlists[currentPlaylist],
            onTrackClick = onTrackClick,
            onFavouriteToggle = onFavouriteToggle,
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            onCollapse = {
                currentScreen = 0
            },
            allTracks = allTracks,
            onAddToPlaylist = onAddToPlaylist,
            viewModel = viewModel,
            apiClient = apiClient
        )
    }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    isAdding: Boolean,
    onRemovePlaylist: (Playlist) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
        ,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF5D84A5)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_music_note),
                contentDescription = "Playlist Icon",
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF293A65)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF020F17)
                )
                Text(
                    text = "${playlist.tracks.size} треков",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF282B32)
                )
            }
            if(!isAdding){
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemovePlaylist(playlist) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Delete playlist",
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFF293A65)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    onTrackClick: (Track) -> Unit,
    onFavouriteToggle: (Track) -> Unit,
    currentTrack: Track?,
    isPlaying: Boolean,
    onCollapse: () -> Unit,
    allTracks: List<Track>,
    onAddToPlaylist: (Track) -> Unit,
    viewModel: MyViewModel,
    apiClient: ApiClient
) {
    var isAddingTracksScreen by remember { mutableStateOf(false) }

    if(!isAddingTracksScreen) {
        Column(
            modifier = Modifier.fillMaxSize()
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        painterResource(id = R.drawable.ic_roll_up),
                        contentDescription = "Roll up",
                        tint = Color(0xFF515A82)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(
                        painterResource(id = R.drawable.ic_share),
                        contentDescription = "Share",
                        Modifier.size(32.dp),
                        tint = Color(0xFF515A82)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFCACDD2),
                    fontSize = 28.sp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                playlist.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFCACDD2),
                        fontSize = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val isPressed = remember { mutableStateOf(false) }
            val scale by animateFloatAsState(targetValue = if (isPressed.value) 0.95f else 1f)
            val buttonColor by animateColorAsState(targetValue = if (isPressed.value) Color.Gray else Color.Transparent)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .width(160.dp)
                        .border(2.dp, Color(0xFFCACDD2), CircleShape)
                        .background(buttonColor, CircleShape)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed.value = true
                                    tryAwaitRelease()
                                    isPressed.value = false
                                },
                                onTap = { isAddingTracksScreen = true }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Add track to playlist",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFFCACDD2)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCACDD2),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            TrackList(
                tracks = playlist.tracks,
                onTrackClick = onTrackClick,
                onFavouriteToggle = onFavouriteToggle,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                isAdding = false,
                isPlaylist = true,
                onAddToPlaylist = onAddToPlaylist,
                onRemoveFromPlaylist = { track: Track ->
                    Log.d("RemoveTrackFromPlaylist", "Attempting to remove track with ID: ${track.id} from playlist with ID: ${playlist.id}")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (playlist.id == null || track.id == null) {
                                Log.e("RemoveTrackFromPlaylist", "Playlist ID or Track ID is null, cannot proceed with request")
                                return@launch
                            }

                            Log.d("RemoveTrackFromPlaylist", "Sending request to remove track...")
                            val response = apiClient.playlistApiService.removeTrackFromPlaylist(
                                playlist.id, track.id
                            ).execute()

                            Log.d("RemoveTrackFromPlaylist", "Received response for removing track from playlist")

                            if (response.isSuccessful) {
                                response.body()?.let { updatedPlaylist ->
                                    Log.d("RemoveTrackFromPlaylist", "Track successfully removed from playlist on server")
                                    val index = viewModel.samplePlaylists.indexOfFirst { it.id == playlist.id }
                                    if (index != -1) {
                                        viewModel.samplePlaylists[index] = updatedPlaylist
                                        Log.d("RemoveTrackFromPlaylist", "Updated local playlist by removing track")
                                    } else {
                                        Log.e("RemoveTrackFromPlaylist", "Failed to find playlist with ID ${playlist.id} in samplePlaylists")
                                    }
                                } ?: Log.e("RemoveTrackFromPlaylist", "Response body is null after successful removal")
                            } else {
                                Log.e("RemoveTrackFromPlaylist", "Error removing track from playlist: ${response.errorBody()?.string()}")
                            }
                        } catch (e: Exception) {
                            Log.e("RemoveTrackFromPlaylist", "Error occurred while removing track from playlist: ${e.message}")
                        }
                    }
                }
            )

        }
    } else {
        AddTracksToPlaylistScreen(
            allTracks = allTracks,
            playlist = playlist,
            onTrackClick = onTrackClick,
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            onCollapse = { isAddingTracksScreen = false },
            viewModel = viewModel,
            apiClient = apiClient
        )
    }
}

@Composable
fun AddTrackToPlaylistsScreen(
    track: Track?,
    playlists: List<Playlist>,
    onCollapse: () -> Unit,
    viewModel: MyViewModel,
    apiClient: ApiClient
) {
    Column(
        modifier = Modifier.fillMaxSize()
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
            }

            Text(
                text = "Add track to playlist",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFCACDD2),
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )
        }
        var searchQuery by remember { mutableStateOf("") }

        val filteredPlaylists = playlists.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }

        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
        )

        LazyColumn {
            items(filteredPlaylists) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    onClick = {
                        if (track != null) {
                            Log.d("AddTrackToPlaylist", "Attempting to add track with ID: ${track.id} to playlist with ID: ${playlist.id}")

                            // Запускаем корутину для добавления трека в плейлист на сервере
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    // Проверяем ID плейлиста и трека перед выполнением запроса
                                    if (playlist.id == null || track.id == null) {
                                        Log.e("AddTrackToPlaylist", "Playlist ID or Track ID is null, cannot proceed with request")
                                        return@launch
                                    }

                                    // Отправляем запрос на добавление трека в плейлист
                                    Log.d("AddTrackToPlaylist", "Sending request to add track...")
                                    val response = apiClient.playlistApiService.addTrackToPlaylist(
                                        playlist.id, track.id
                                    ).execute()

                                    Log.d("AddTrackToPlaylist", "Received response for adding track to playlist")

                                    if (response.isSuccessful) {
                                        // Если запрос успешен, обновляем локальный плейлист
                                        response.body()?.let { updatedPlaylist ->
                                            Log.d("AddTrackToPlaylist", "Track successfully added to playlist on server")
                                            val index = viewModel.samplePlaylists.indexOfFirst { it.id == playlist.id }
                                            if (index != -1) {
                                                viewModel.samplePlaylists[index] = updatedPlaylist
                                                Log.d("AddTrackToPlaylist", "Updated local playlist with new data")
                                            } else {
                                                Log.e("AddTrackToPlaylist", "Failed to find playlist with ID ${playlist.id} in samplePlaylists")
                                            }
                                        } ?: Log.e("AddTrackToPlaylist", "Response body is null after successful request")
                                    } else {
                                        // Обработка ошибки, если добавление трека не удалось
                                        Log.e("AddTrackToPlaylist", "Error adding track to playlist: ${response.errorBody()?.string()}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("AddTrackToPlaylist", "Error occurred while adding track to playlist: ${e.message}")
                                }
                            }
                        } else {
                            Log.e("AddTrackToPlaylist", "Track is null, cannot add to playlist")
                        }
                        onCollapse()
                    },
                    isAdding = true,
                    onRemovePlaylist = {}
                )
            }
        }


    }
}

@Composable
fun AddTracksToPlaylistScreen(
    allTracks: List<Track>,
    playlist: Playlist,
    onTrackClick: (Track) -> Unit,
    currentTrack: Track?,
    isPlaying: Boolean,
    onCollapse: () -> Unit,
    viewModel: MyViewModel,
    apiClient: ApiClient
) {
    val playlistTracks = remember {
        mutableStateListOf<Track>().apply {
            addAll(playlist.tracks.map { track ->
                if (track.playlists == null) {
                    track.copy(playlists = mutableListOf(), rotation = Animatable(0f))
                } else {
                    track
                }
            })
        }
    }


    Column(
        modifier = Modifier.fillMaxSize()
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
            }

            Text(
                text = "Add tracks",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFCACDD2),
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )
        }

        Log.d("Adding", "tracks in playlist: $playlistTracks")
        TrackList(
            tracks = allTracks,
            onTrackClick = onTrackClick,
            onFavouriteToggle = {},
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            isAdding = true,
            isPlaylist = false,
            onAddToPlaylist = { track: Track ->
                Log.d("AddTrackToPlaylist", "Attempting to add track with ID: ${track.id} to playlist with ID: ${playlist.id}")

                // Запускаем корутину для добавления трека в плейлист на сервере
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Проверяем ID плейлиста и трека перед выполнением запроса
                        if (playlist.id == null || track.id == null) {
                            Log.e("AddTrackToPlaylist", "Playlist ID or Track ID is null, cannot proceed with request")
                            return@launch
                        }

                        // Отправляем запрос на добавление трека в плейлист
                        Log.d("AddTrackToPlaylist", "Sending request to add track to playlist...")
                        val response = apiClient.playlistApiService.addTrackToPlaylist(
                            playlist.id, track.id
                        ).execute()

                        Log.d("AddTrackToPlaylist", "Received response for adding track to playlist")

                        if (response.isSuccessful) {
                            // Если запрос успешен, обновляем локальный плейлист
                            response.body()?.let { updatedPlaylist ->
                                Log.d("AddTrackToPlaylist", "Track successfully added to playlist on server")
                                val index = viewModel.samplePlaylists.indexOfFirst { it.id == playlist.id }
                                if (index != -1) {
                                    viewModel.samplePlaylists[index] = updatedPlaylist
                                    Log.d("AddTrackToPlaylist", "Updated local playlist by adding track")
                                } else {
                                    Log.e("AddTrackToPlaylist", "Failed to find playlist with ID ${playlist.id} in samplePlaylists")
                                }
                            } ?: Log.e("AddTrackToPlaylist", "Response body is null after successful addition")
                        } else {
                            // Обработка ошибки, если добавление трека не удалось
                            Log.e("AddTrackToPlaylist", "Error adding track to playlist: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e("AddTrackToPlaylist", "Error occurred while adding track to playlist: ${e.message}")
                    }
                }

                // Локальное добавление трека в плейлист для мгновенного обновления
                playlistTracks.add(track)
            },
            onRemoveFromPlaylist = {},
            playlistTracks = playlistTracks
        )

    }
}

@Composable
fun AddPlaylistScreen(
    onCollapse: () -> Unit,
    onCreatePlaylist: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
                    text = "Creating playlist",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFCACDD2),
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        var playlistName by remember { mutableStateOf("") }
        TextField(
            value = playlistName,
            onValueChange = { playlistName = it },
            placeholder = { Text("Enter playlist name") },
            modifier = Modifier
                .padding(horizontal = 48.dp)
                .fillMaxWidth(),
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

        Button(
            onClick = {
                if (playlistName.isNotBlank()) {
                    onCreatePlaylist(playlistName)
                    playlistName = ""
                }
            },
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
                .background(Color(0xFF515A82), shape = RoundedCornerShape(24.dp))
                .width(150.dp)
                .height(48.dp)
        ) {
            Text(
                "Create",
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}


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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val mediaPlayer: MediaPlayer? = null
    MusicPlatformTheme {
        var currentTrack by remember { mutableStateOf<Track?>(null) }
        var playingTrackIndex by remember { mutableIntStateOf(0) }
        val context = LocalContext.current

//        LoginScreen(onLoginSuccess =
//        {
////            MusicAppScreen(
////                onTrackClickMain = { track ->
////                    currentTrack = track
////                },
////                currentTrack = null,
////                playingTrackIndex = playingTrackIndex,
////                onPlayingTrackIndexChange = { newIndex ->
////                    playingTrackIndex = newIndex
////                },
////                mediaPlayer = mediaPlayer,
////                currentTrackList = mutableListOf(),
////                onChangeCurTrackList = {},
////                onLogoutSuccess = {},
////                apiClient = ApiClient(context)
////            )
//        },
//            apiClient = ApiClient(context)
//        )
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
            context = context
        )
    }
}
