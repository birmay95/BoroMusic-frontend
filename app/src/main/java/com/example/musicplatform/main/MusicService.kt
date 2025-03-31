package com.example.musicplatform.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.Manifest
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.musicplatform.R
import com.example.musicplatform.model.Track


class MusicService : Service() {
    private var player: ExoPlayer? = null
    private val binder = MusicBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannelId: String

    private var _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime

    private val _trackDuration = MutableStateFlow(0L)
    val trackDuration: StateFlow<Long> = _trackDuration

    private val _button = MutableStateFlow(0L)
    val button: StateFlow<Long> = _button

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: MutableStateFlow<Boolean> = _isPlaying

    private var currentTrack: Track? = null

    private var updateJob: Job? = null

    companion object {
        const val ACTION_PLAY = "com.example.musicplatform.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.musicplatform.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.musicplatform.ACTION_NEXT"
        const val ACTION_PREV = "com.example.musicplatform.ACTION_PREV"
        const val ACTION_FAVORITE = "com.example.musicplatform.ACTION_FAVORITE"
        const val EXTRA_TRACK_URL = "com.example.musicplatform.EXTRA_TRACK_URL"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val trackUrl = intent.getStringExtra(EXTRA_TRACK_URL)
                if (trackUrl != null) {
                    playTrack(trackUrl)
                }
            }
            ACTION_PAUSE -> pausePlay()
            ACTION_NEXT -> nextTrack()
            ACTION_PREV -> prevTrack()
            ACTION_FAVORITE -> addToFavorites()
        }
        updateNotification()
        return START_STICKY
    }

    @OptIn(UnstableApi::class)
    fun playTrack(url: String) {
        _button.value = 0L

        player?.stop()
        player?.release()
        player = null
        player = ExoPlayer.Builder(applicationContext).build()
        val mediaSource = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(url))
        player?.setMediaSource(mediaSource)
        player?.prepare()
        player?.play()
        _isPlaying.value = true

        startUpdatingProgress()
        updateNotification()
    }

    fun setTrack(track: Track) {
        currentTrack = track
    }

    private fun nextTrack() {
        sendTrackControlBroadcast("NEXT")
        _button.value = 1L
        updateNotification()
    }

    private fun prevTrack() {
        sendTrackControlBroadcast("PREV")
        _button.value = 2L
        updateNotification()
    }

    private fun addToFavorites() {
        sendTrackControlBroadcast("FAVORITE")
        _button.value = 3L
        updateNotification()
    }

    private fun sendTrackControlBroadcast(action: String) {
        val intent = Intent("com.example.musicplatform.TRACK_CONTROL").apply {
            putExtra("ACTION", action)
        }
        applicationContext.sendBroadcast(intent)
    }

    private fun startUpdatingProgress() {
        updateJob?.cancel()
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(1000L)
                val pos = player?.currentPosition ?: 0L
                val duration = player?.duration ?: 0L
                _currentTime.value = pos / 1000L
                _trackDuration.value = duration
                updateNotification()
            }
        }
    }

    private fun sendTrackEndedBroadcast() {
        val intent = Intent("com.example.musicplatform.TRACK_ENDED")
        sendBroadcast(intent)
    }

    fun pausePlay() {
        player?.let {
            if (_isPlaying.value) it.pause() else it.play()
            _isPlaying.value = !_isPlaying.value
            updateNotification()
        }
    }

    fun seekTo(position: Int) {
        _currentTime.value = position.toLong()
        player?.seekTo(position * 1000L)
    }

    fun getPlayer(): ExoPlayer? {
        return player
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).build()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationChannelId = "music_player_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for music player"
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "There is no permission for notifications")
                return
            }
        }
    }

    private fun updateNotification() {
        val playPauseAction = if (_isPlaying.value) createPauseAction() else createPlayAction()
        val nextAction = createNextAction()
        val prevAction = createPrevAction()
        val favoriteAction = createFavoriteAction()

        val progress = _currentTime.value
        val duration =_trackDuration.value / 1000L

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle(currentTrack?.title)
            .setContentText(currentTrack?.artist)
            .setSmallIcon(R.drawable.ic_music_note)
            .setProgress(duration.toInt(), progress.toInt(), false)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(favoriteAction)
            .setOngoing(_isPlaying.value)
            .build()

        startForeground(1, notification)
    }

    private fun createPlayAction(): NotificationCompat.Action {
        val playIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_PAUSE
        }
        val playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_play_arrow, "Play", playPendingIntent)
    }

    private fun createPauseAction(): NotificationCompat.Action {
        val pauseIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_pause, "Pause", pausePendingIntent)
    }

    private fun createNextAction(): NotificationCompat.Action {
        val intent = Intent(this, MusicService::class.java).apply { action = ACTION_NEXT }
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Action(R.drawable.ic_next, "Next", pendingIntent)
    }

    private fun createPrevAction(): NotificationCompat.Action {
        val intent = Intent(this, MusicService::class.java).apply { action = ACTION_PREV }
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Action(R.drawable.ic_prev, "Previous", pendingIntent)
    }

    private fun createFavoriteAction(): NotificationCompat.Action {
        val intent = Intent(this, MusicService::class.java).apply { action = ACTION_FAVORITE }
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Action(R.drawable.ic_favorite, "Favorite", pendingIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        updateJob?.cancel()
    }
}



