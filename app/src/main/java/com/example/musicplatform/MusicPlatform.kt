package com.example.musicplatform

import android.app.Application
import android.content.Context

class MusicPlatform : Application() {

    companion object {
        private var instance: MusicPlatform? = null

        // Статический метод для доступа к контексту
        fun getContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
