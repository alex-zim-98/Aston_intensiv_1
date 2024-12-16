package com.aston.myapplication.domain

import androidx.lifecycle.LiveData

class GetSongUseCase(private val repository: MusicRepository) {
    operator fun invoke(byId: Int): LiveData<Song> {
        return repository.getSong(byId)
    }
}