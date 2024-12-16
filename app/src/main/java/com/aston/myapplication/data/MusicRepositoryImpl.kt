package com.aston.myapplication.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aston.myapplication.R
import com.aston.myapplication.domain.MusicRepository
import com.aston.myapplication.domain.Song

object MusicRepositoryImpl: MusicRepository {
    private val listSongs = listOf(
        Song(R.raw.coy, "Pachka", "Coy", R.drawable.coy),
        Song(R.raw.vinni, "Vhodit", "Vinni", R.drawable.vinni),
        Song(R.raw.man, "Kanai", "Some man", R.drawable.man),
    )
    private val listSongLD = MutableLiveData<List<Song>>()
    private val songLD = MutableLiveData<Song>()
    override fun getListSongs(): LiveData<List<Song>> {
        listSongLD.value = listSongs
        return listSongLD
    }

    override fun getSong(byIndex: Int): LiveData<Song> {
        val song = listSongLD.value?.get(byIndex) ?:
        throw RuntimeException("Song was not found")
        songLD.value = song
        return songLD
    }
}