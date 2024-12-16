// MusicActivity.kt
package com.aston.myapplication.presentation

import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aston.myapplication.application.MyApp
import com.aston.myapplication.data.service.MusicService
import com.aston.myapplication.databinding.ActivityMainBinding

class MusicActivity : AppCompatActivity() {

    private lateinit var viewModel: MusicViewModel
    private val viewBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewModel = (applicationContext as MyApp).musicViewModel

        viewBinding.playPauseButton.setOnClickListener {
            startForegroundService(MusicService.newIntentStartPause(this))
        }

        viewBinding.nextButton.setOnClickListener {
            startForegroundService(MusicService.newIntentNext(this))
        }

        viewBinding.previousButton.setOnClickListener {
            startForegroundService(MusicService.newIntentPrev(this))
        }

        viewModel.trackLD.observe(this) {
            val drawable = ContextCompat.getDrawable(this, it.drawable)
            viewBinding.ivSong.setImageDrawable(drawable)
            viewBinding.tvSongName.text = it.title
        }

        viewModel.duration.observe(this) {
            viewBinding.sbSong.max = it
        }

        viewModel.currentProgress.observe(this) {
            viewBinding.sbSong.progress = it
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            val drawable = ContextCompat.getDrawable(this,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
            viewBinding.playPauseButton.background = drawable
        }
        viewBinding.sbSong.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setUserSeeking(true)
                    viewModel.changeProgress(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                viewModel.setUserSeeking(true)
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                viewModel.setUserSeeking(false)
            }
        })
    }
}
