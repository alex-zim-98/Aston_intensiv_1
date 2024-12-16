package com.aston.myapplication.presentation

import android.app.Notification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aston.myapplication.data.MusicRepositoryImpl
import com.aston.myapplication.domain.GetListSongsUseCase
import com.aston.myapplication.domain.GetSongUseCase
import com.aston.myapplication.domain.Song

class MusicViewModel : ViewModel() {

    private val repository = MusicRepositoryImpl

    private val getListSongsUseCase = GetListSongsUseCase(repository)
    private val getSongUseCase = GetSongUseCase(repository)

    private val _isUserSeeking = MutableLiveData(false)
    val isUserSeeking: LiveData<Boolean> = _isUserSeeking

    private val _trackList = MutableLiveData<List<Song>>()
    val trackList: LiveData<List<Song>> get() =  _trackList

    private val _trackLD = MutableLiveData<Song>()
    val trackLD: LiveData<Song> get() =  _trackLD

    private val _currentSongIndex = MutableLiveData<Int>()
    val currentSongIndex: LiveData<Int> = _currentSongIndex

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _isUpdatingProgress = MutableLiveData<Boolean>()
    val isUpdatingProgress: LiveData<Boolean> = _isUpdatingProgress

    private val _currentProgress = MutableLiveData<Int>()
    val currentProgress: LiveData<Int> = _currentProgress

    private val _duration = MutableLiveData<Int>()
    val duration: LiveData<Int> = _duration

    fun setUserSeeking(isSeeking: Boolean) {
        _isUserSeeking.value = isSeeking
    }

    private val _notification = MutableLiveData<Notification?>()
    val notification: LiveData<Notification?> = _notification

    init {
        getListSongs()
        _currentSongIndex.value = DEFAULT_INDEX
        getShopItem(DEFAULT_INDEX)

        _isPlaying.value = false
        _isUpdatingProgress.value = false
    }

    private fun getShopItem(byId: Int) {
        val item = getSongUseCase(byId)
        _trackLD.value = item.value
    }

    fun getListSongs() {
        val item = getListSongsUseCase()
        _trackList.value = item.value
    }


    fun changeProgress(seek: Int) {
        _currentProgress.value = seek
    }

    fun playNextSong() {
        val currentIndex = _currentSongIndex.value ?: 0
        val newIndex = (currentIndex + 1) % (_trackList.value?.size ?: 1)
        _currentSongIndex.value = newIndex
        _isPlaying.value = true
        _currentProgress.value = 0
        getShopItem(newIndex)
    }

    fun playPreviousSong() {
        val currentIndex = _currentSongIndex.value ?: 0
        val newIndex = if (currentIndex - 1 < 0) _trackList.value!!.size - 1 else currentIndex - 1
        _currentSongIndex.value = newIndex
        _isPlaying.value = true
        _currentProgress.value = 0
        getShopItem(currentIndex)
    }

    fun changeIsPlaying(status: Boolean) {
        _isPlaying.value = status
    }

    fun updateProgress(current: Int, total: Int) {
        _currentProgress.postValue(current)
        _duration.postValue(total)
    }

    fun setNotification(notification: Notification?) {
        _notification.value = notification
    }

    fun setUpdatingProgress(isUpdating: Boolean) {
        _isUpdatingProgress.value = isUpdating
    }

    companion object {
        private const val DEFAULT_INDEX = 0
    }
}
