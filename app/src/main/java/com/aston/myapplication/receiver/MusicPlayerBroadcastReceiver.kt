package com.aston.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aston.myapplication.data.service.MusicService

class MusicPlayerBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MusicService.ACTION_PREVIOUS -> {
                val serviceIntent = MusicService.newIntentPrev(context)
                context.startForegroundService(serviceIntent)
            }

            MusicService.ACTION_NEXT -> {
                val serviceIntent = MusicService.newIntentNext(context)
                context.startForegroundService(serviceIntent)
            }

            MusicService.ACTION_START_PAUSE -> {
                val serviceIntent = MusicService.newIntentStartPause(context)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}