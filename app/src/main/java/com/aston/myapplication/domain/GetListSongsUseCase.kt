package com.aston.myapplication.domain

import androidx.lifecycle.LiveData

class GetListSongsUseCase(private val repository: MusicRepository) {
    operator fun invoke(): LiveData<List<Song>> {
        return repository.getListSongs()
    }
}