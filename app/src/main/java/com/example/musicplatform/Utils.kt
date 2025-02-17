package com.example.musicplatform

class Utils {
    companion object {
//         Загрузка библиотеки, которая содержит нативную функцию
        init {
            System.loadLibrary("format-time-lib") // Замените на имя вашей библиотеки
        }

//         Объявление нативной функции
        external fun formatTime(seconds: Long): String
    }
}