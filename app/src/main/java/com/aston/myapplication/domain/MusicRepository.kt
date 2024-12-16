package com.aston.myapplication.domain

import androidx.lifecycle.LiveData

interface MusicRepository {
    fun getListSongs(): LiveData<List<Song>>
    fun getSong(byIndex: Int): LiveData<Song>
}