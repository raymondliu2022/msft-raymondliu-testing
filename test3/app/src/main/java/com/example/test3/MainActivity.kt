package com.example.test3

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ReactiveGuide
import androidx.core.util.Consumer
import androidx.window.DisplayFeature
import androidx.window.FoldingFeature
import androidx.window.WindowLayoutInfo
import androidx.window.WindowManager
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var rootMotionLayout: MotionLayout
    private lateinit var leafMotionLayout: MotionLayout
    private lateinit var windowManager: WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private val mainThreadExecutor = Executor { r: Runnable -> handler.post(r) }
    private val stateContainer = StateContainer()

    private lateinit var playerView : StyledPlayerView
    private lateinit var player : SimpleExoPlayer
    private lateinit var chatEnableButton: FloatingActionButton

    private var chatToggle: Boolean = true
    private var spanToggle: Boolean = false
    private var spanOrientation: Int = FoldingFeature.ORIENTATION_VERTICAL
    private var spanValue1: Int = 0
    private var spanValue2: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatToggle = true
        spanToggle = false

        windowManager = WindowManager(this)

        setContentView(R.layout.activity_main)

        rootMotionLayout = findViewById<MotionLayout>(R.id.root)
        leafMotionLayout = findViewById<MotionLayout>(R.id.leaf)
        chatEnableButton = findViewById<FloatingActionButton>(R.id.chatEnableButton)

        playerView = findViewById(R.id.player_view);
        player = SimpleExoPlayer.Builder(this).build()
        playerView.player = player

    }

    override fun onStart() {
        super.onStart()
        windowManager.registerLayoutChangeCallback(mainThreadExecutor, stateContainer)

        var videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
        var mediaItem = MediaItem.fromUri(videoUrl)
        player.setMediaItem(mediaItem)
        player.prepare()

        chatEnableButton.setOnClickListener { view ->
            chatToggle = !chatToggle
            changeLayout()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        chatToggle = savedInstanceState.getBoolean(STATE_CHAT)
        spanToggle = savedInstanceState.getBoolean(STATE_SPAN)
        player.playWhenReady = savedInstanceState.getBoolean(STATE_PLAY_WHEN_READY)
        player.seekTo(savedInstanceState.getInt(STATE_CURRENT_WINDOW_INDEX), savedInstanceState.getLong(STATE_CURRENT_POSITION))
        player.prepare()
    }

    override fun onStop() {
        super.onStop()
        windowManager.unregisterLayoutChangeCallback(stateContainer)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run{
            putBoolean(STATE_CHAT, chatToggle)
            putBoolean(STATE_SPAN, spanToggle)
            putBoolean(STATE_PLAY_WHEN_READY, player.playWhenReady)
            putLong(STATE_CURRENT_POSITION, player.currentPosition)
            putInt(STATE_CURRENT_WINDOW_INDEX, player.currentWindowIndex)
        }

        super.onSaveInstanceState(outState)
    }

    fun setGuides(horizontal_top : Int, horizontal_bottom : Int, vertical_start : Int, vertical_end : Int) {
        with (ConstraintLayout.getSharedValues()) {
            fireNewValue(R.id.horizontal_top_guide, horizontal_top)
            fireNewValue(R.id.horizontal_bottom_guide, horizontal_bottom)
            fireNewValue(R.id.vertical_start_guide, vertical_start)
            fireNewValue(R.id.vertical_end_guide, vertical_end)
        }
    }

    fun changeLayout() {
        if (spanToggle) {
            if (spanOrientation == FoldingFeature.ORIENTATION_HORIZONTAL) {
                setGuides(spanValue1, spanValue2, 0, 0)
            }
            else {
                if (chatToggle) {
                    setGuides(0, 0, spanValue1, spanValue2)
                }
                else {
                    setGuides(0, 0, 0, 0)
                }
            }
        }
        else {
            if (chatToggle) {
                if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setGuides(0, 0, 300, 300)
                }
                else {
                    setGuides(300, 300, 0, 0)
                }
            }
            else {
                setGuides(0, 0, 0, 0)
            }
        }
    }

    companion object {
        val STATE_CHAT = "chatToggle"
        val STATE_SPAN = "spanToggle"
        val STATE_PLAY_WHEN_READY = "playerPlayWhenReady"
        val STATE_CURRENT_POSITION = "playerPlaybackPosition"
        val STATE_CURRENT_WINDOW_INDEX = "playerCurrentWindowIndex"
    }

    inner class StateContainer : Consumer<WindowLayoutInfo> {
        override fun accept(newLayoutInfo: WindowLayoutInfo) {
            // Add views that represent display features
            spanToggle = false
            for (displayFeature : DisplayFeature in newLayoutInfo.displayFeatures) {
                if (displayFeature is FoldingFeature){
                    spanToggle = true
                    spanOrientation = displayFeature.orientation
                    val point = IntArray(2)
                    rootMotionLayout.getLocationInWindow(point)
                    Log.d("REEEE", displayFeature.bounds.toString())
                    if (spanOrientation == FoldingFeature.ORIENTATION_HORIZONTAL) {
                        spanValue1 = displayFeature.bounds.bottom
                        spanValue2 = displayFeature.bounds.top
                    }
                    else {
                        spanValue1 = displayFeature.bounds.right
                        spanValue2 = displayFeature.bounds.left
                    }
                }
            }
            changeLayout()
        }
    }
}