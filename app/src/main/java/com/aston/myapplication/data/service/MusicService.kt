package com.aston.myapplication.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.aston.myapplication.application.MyApp
import com.aston.myapplication.R
import com.aston.myapplication.presentation.MusicViewModel
import com.aston.myapplication.receiver.MusicPlayerBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicService: Service() {
    private var isPlaying = false
    private lateinit var scope: CoroutineScope
    private lateinit var viewModel: MusicViewModel
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var notificationManager: NotificationManager
    private var progressObserver: Observer<Int>? = null
    private var isPlayingObserver: Observer<Boolean>? = null


    override fun onCreate() {
        super.onCreate()

        viewModel = (applicationContext as MyApp).musicViewModel

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        scope = CoroutineScope(Dispatchers.IO)

        createNotificationChannel()
        viewModel.setNotification(createNotification(0, 0))
        initializeMediaPlayer()

        startForeground(NOTIFY_ID, viewModel.notification.value)
    }
    private var lastNotificationUpdateTime = 0L
    private fun updateProgressValue() {
        val currentDuration = mediaPlayer?.currentPosition ?: 0
        val duration = mediaPlayer?.duration ?: 0

        viewModel.updateProgress(currentDuration, duration)

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNotificationUpdateTime > INTERVAL_UPDATE) {
            notificationManager.notify(
                NOTIFY_ID,
                createNotification(currentDuration, duration)
            )
            lastNotificationUpdateTime = currentTime
        }
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = null
        val trackList = viewModel.trackList.value ?: return
        val currentIndex = viewModel.currentSongIndex.value ?: 0

        val afd = resources.openRawResourceFd(trackList[currentIndex].resourceId) ?: return
        mediaPlayer = MediaPlayer().apply {
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            prepare()
            start()
            viewModel.setUpdatingProgress(true)
            afd.close()
        }

        mediaPlayer?.setOnCompletionListener {
            playNextTrack()
        }

        val _progressObserver = Observer<Int> { progress ->
            mediaPlayer?.let {
                if (viewModel.isUserSeeking.value == true) {
                    it.seekTo(progress)
                }
            }
        }
        val _isPlayingObserver = Observer<Boolean> { isPlaying ->
            this.isPlaying = isPlaying
        }

        progressObserver = _progressObserver
        isPlayingObserver = _isPlayingObserver

        viewModel.currentProgress.observeForever(_progressObserver)
        viewModel.isPlaying.observeForever(_isPlayingObserver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val command = intent?.getStringExtra(COMMAND)
        when (command) {
            COMMAND_NEXT -> playNextTrack()
            COMMAND_PREVIOUS -> playPreviousTrack()
            COMMAND_START_PAUSE -> togglePlayback()
        }

        scope.launch {
            mediaPlayer?.let {
                try {
                    if (isPlaying) {
                        while (viewModel.isUpdatingProgress.value == true && isPlaying) {
                            updateProgressValue()
                            delay(INTERVAL_UPDATE)
                        }
                    } else {}
                } catch (e: IllegalStateException) {
                    Log.e("MusicService", "MediaPlayer is in an invalid state", e)
                }
            } ?: run {
                Log.e("MusicService", "MediaPlayer is null")
            }

        }

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun playNextTrack() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        viewModel.playNextSong()
        initializeMediaPlayer()
    }

    private fun playPreviousTrack() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        viewModel.playPreviousSong()
        initializeMediaPlayer()
    }

    private fun togglePlayback() {
        if (isPlaying) {
            mediaPlayer?.pause()
            viewModel.changeIsPlaying(false)
        } else {
            if (mediaPlayer == null) initializeMediaPlayer()
            mediaPlayer?.start()?.also {
                viewModel.changeIsPlaying(true)
            }
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        viewModel.setUpdatingProgress(false)
        scope.cancel()
        progressObserver?.let {
            viewModel.currentProgress.removeObserver(it)
        }

        super.onDestroy()
    }


    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for music player notifications"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    private fun createNotification(start: Int, end: Int): Notification {
        val prevPendingIntent = getIntent(ACTION_PREVIOUS)
        val nextPendingIntent = getIntent(ACTION_NEXT)
        val startPausePendingIntent = getIntent(ACTION_START_PAUSE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Best player")
            .setContentText("Playing...")
            .setSilent(true)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(android.R.drawable.ic_media_previous, "Start/Pause", startPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
            .setProgress(end, start, false)
            .build()
    }

    private fun getIntent(action: String): PendingIntent? {
        val prevIntent = Intent(this, MusicPlayerBroadcastReceiver::class.java).apply {
            this.action = action
        }
        val prevPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return prevPendingIntent
    }

    companion object {
        private const val INTERVAL_UPDATE = 1000L

        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_START_PAUSE = "ACTION_START_PAUSE"
        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"

        const val COMMAND = "command"
        private const val COMMAND_NEXT = "next"
        private const val COMMAND_START_PAUSE = "start_pause"
        private const val COMMAND_PREVIOUS = "previous"

        private const val NOTIFY_ID = 1
        private const val CHANNEL_ID = "channel_id"
        private const val CHANNEL_NAME = "music_player"

        fun newIntentPrev(context: Context) = Intent(context, MusicService::class.java).apply {
            action = ACTION_PREVIOUS
            putExtra(COMMAND, COMMAND_PREVIOUS)
        }

        fun newIntentNext(context: Context) = Intent(context, MusicService::class.java).apply {
            action = ACTION_NEXT
            putExtra(COMMAND, COMMAND_NEXT)
        }

        fun newIntentStartPause(context: Context) = Intent(context, MusicService::class.java).apply {
            action = ACTION_START_PAUSE
            putExtra(COMMAND, COMMAND_START_PAUSE)
        }
    }
}