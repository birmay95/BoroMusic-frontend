package com.example.musicplatform

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicplatform.tracks.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val _trackAction = MutableLiveData<String>()
    val trackAction: LiveData<String> = _trackAction

    private val trackControlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.getStringExtra("ACTION") ?: return
            Log.d("MusicViewModel", "Получено действие: $action")
            _trackAction.postValue(action)
        }
    }

    init {
        val filter = IntentFilter("com.example.musicplatform.TRACK_CONTROL")
        application.registerReceiver(trackControlReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(trackControlReceiver)
    }

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    fun setCurrentTrack(track: Track) {
        _currentTrack.value = track
    }

    fun getCurrentTrack(): Track? {
        return _currentTrack.value
    }
}
