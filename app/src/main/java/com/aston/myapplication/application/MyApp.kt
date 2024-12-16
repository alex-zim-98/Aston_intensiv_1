package com.aston.myapplication.application

import android.app.Application
import com.aston.myapplication.presentation.MusicViewModel

class MyApp : Application() {
    val musicViewModel: MusicViewModel by lazy { MusicViewModel() }
}