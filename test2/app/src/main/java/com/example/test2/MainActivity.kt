package com.example.test2

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView

class MainActivity : AppCompatActivity() {
    lateinit var player : SimpleExoPlayer
    lateinit var playerView : StyledPlayerView

    private fun initPlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        playerView.player = player
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById<StyledPlayerView>(R.id.video)
    }

    override fun onStart() {
        super.onStart()

        if(Build.VERSION.SDK_INT >= 24) {
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()

        if(Build.VERSION.SDK_INT < 24 || !::player.isInitialized) {
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }


}