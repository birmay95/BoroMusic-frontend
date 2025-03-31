package com.example.musicplatform.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplatform.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MusicServiceConnection(private val context: Context) {

    private var musicService: MusicService? = null
    private var isBound = false

    val player: ExoPlayer?
        get() = musicService?.getPlayer()

    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> get() = _currentTime

    private val _trackDuration = MutableStateFlow(0L)
    val trackDuration: StateFlow<Long> get() = _trackDuration

    private val _button = MutableStateFlow(0L)
    val button: StateFlow<Long> get() = _button

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val musicBinder = binder as MusicService.MusicBinder
            musicService = musicBinder.getService()

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

            serviceScope.launch {
                musicService?.button?.collect { time ->
                    _button.value = time
                }
            }

            serviceScope.launch {
                musicService?.isPlaying?.collect { time ->
                    _isPlaying.value = time
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            serviceScope.cancel()
        }
    }

    fun bindService() {
        if (!isBound) {
            val intent = Intent(context, MusicService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            isBound = true
        }
    }

    fun unbindService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            serviceScope.cancel()
            isBound = false
        }
    }

    fun playTrack(url: String) {
        musicService?.playTrack(url)
    }

    fun setTrack(track: Track) {
        musicService?.setTrack(track)
    }

    fun pausePlay() {
        musicService?.pausePlay()
    }

    fun seekTo(position: Int) {
        musicService?.seekTo(position)
    }
}