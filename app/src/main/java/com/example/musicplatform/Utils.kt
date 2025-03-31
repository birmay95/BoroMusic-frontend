package com.example.musicplatform

class Utils {
    companion object {
        init {
            System.loadLibrary("format-time-lib")
        }
        external fun formatTime(seconds: Long): String
    }
}