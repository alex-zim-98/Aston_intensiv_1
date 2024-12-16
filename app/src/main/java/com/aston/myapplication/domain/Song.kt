package com.aston.myapplication.domain

data class Song(
    val resourceId: Int,
    val title: String,
    val artist: String,
    val drawable: Int,
    val duration: Int = 0,
    val currentProgress: Int = 0
)
