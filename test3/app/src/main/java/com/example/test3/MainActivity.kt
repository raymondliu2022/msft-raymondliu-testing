package com.example.test3

import android.content.res.Configuration
import android.graphics.Rect
import android.hardware.camera2.params.MeteringRectangle
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ReactiveGuide
import androidx.core.util.Consumer
import androidx.core.view.WindowInsetsCompat
import androidx.window.DisplayFeature
import androidx.window.FoldingFeature
import androidx.window.WindowLayoutInfo
import androidx.window.WindowManager
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.Util
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var rootView: MotionLayout
    private lateinit var windowManager: WindowManager
    private lateinit var endChatView: View
    private lateinit var bottomChatView: View
    private val handler = Handler(Looper.getMainLooper())
    private val mainThreadExecutor = Executor { r: Runnable -> handler.post(r) }
    private val stateContainer = StateContainer()

    private lateinit var playerView : StyledPlayerView
    private lateinit var player : SimpleExoPlayer
    private lateinit var chatEnableButton: FloatingActionButton

    private var keyboardToggle: Boolean = false
    private var chatToggle: Boolean = true
    private var spanToggle: Boolean = false
    private var spanOrientation: Int = FoldingFeature.ORIENTATION_VERTICAL
    private var spanValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("CHANGE_LAYOUT", "on create")
        super.onCreate(savedInstanceState)

        windowManager = WindowManager(this)
        setContentView(R.layout.activity_main)

        rootView = findViewById<MotionLayout>(R.id.root)
        rootView.transitionToState(R.id.base)
        chatEnableButton = findViewById<FloatingActionButton>(R.id.chatEnableButton)
        endChatView = findViewById<ReactiveGuide>(R.id.end_chat_view)
        bottomChatView = findViewById<ReactiveGuide>(R.id.bottom_chat_view)

        playerView = findViewById(R.id.player_view);
        player = SimpleExoPlayer.Builder(this).build()
        playerView.player = player

    }

    override fun onStart() {
        Log.d("CHANGE_LAYOUT", "on start")
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

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            var tempKeyboardToggle = false
            if (Util.SDK_INT >= 30) {
                tempKeyboardToggle =
                    rootView.rootWindowInsets.isVisible(WindowInsets.Type.ime()) //TEST
            } else {
                tempKeyboardToggle = rootView.rootWindowInsets.systemWindowInsetBottom > 200 //TEST
            }
            if (tempKeyboardToggle) {
                if (!keyboardToggle) {
                    keyboardToggle = true
                    changeLayout()
                }
            } else {
                if (keyboardToggle) {
                    keyboardToggle = false
                    changeLayout()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("YEET", rootView.currentState.toString() + " == " + R.id.base.toString())
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
        player.stop()
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

    fun setGuides(mode: String, horizontal : Int, vertical: Int) {
        var constraintSet = rootView.getConstraintSet(R.id.base)
        constraintSet.setGuidelineEnd(R.id.horizontal_guide, horizontal)
        constraintSet.setGuidelineEnd(R.id.vertical_guide, vertical)
        rootView.updateStateAnimate(R.id.base, constraintSet, 500)
    }

    fun changeLayout() {
        //if app is spanned across a fold
        if (spanToggle) {
            //if fold is horizontal
            if (spanOrientation == FoldingFeature.ORIENTATION_HORIZONTAL) {
                if (keyboardToggle) {
                    Log.d("CHANGE_LAYOUT", "horizontal span, keyboard")
                    setGuides("keyboard",0, 800)
                }
                else if (chatToggle || this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Log.d("CHANGE_LAYOUT", "horizontal span, chat or landscape")
                    setGuides("chat", spanValue, 0)
                }
                else {
                    Log.d("CHANGE_LAYOUT", "horizontal span, no keyboard, no chat, portrait")
                    setGuides("fullscreen", 0, 0)
                }
            }
            else {
                if (chatToggle) {
                    Log.d("CHANGE_LAYOUT", "vertical span, chat")
                    setGuides("chat",0, spanValue)
                }
                else {
                    Log.d("CHANGE_LAYOUT", "vertical span")
                    setGuides("fullscreen",0, 0)
                }
            }
        }
        else {
            if (chatToggle) {
                if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Log.d("CHANGE_LAYOUT", "chat, landscape")
                    setGuides("chat", 0, 800)
                }
                else {
                    if (!keyboardToggle) {
                        Log.d("CHANGE_LAYOUT", "chat, portrait")
                        setGuides("chat", 800, 0)
                    }
                    else {
                        Log.d("CHANGE_LAYOUT", "chat, portrait, keyboard")
                        setGuides("keyboard",0, 0)
                    }
                }
            }
            else {
                Log.d("CHANGE_LAYOUT", "none")
                setGuides("fullscreen",0, 0)
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

            endChatView.setPadding(0,0,0,0)
            bottomChatView.setPadding(0,0,0,0)
            for (displayFeature : DisplayFeature in newLayoutInfo.displayFeatures) {
                if (displayFeature is FoldingFeature){
                    spanToggle = true
                    spanOrientation = displayFeature.orientation
                    if (spanOrientation == FoldingFeature.ORIENTATION_HORIZONTAL) {
                        spanValue = displayFeature.bounds.bottom
                        bottomChatView.setPadding(0,displayFeature.bounds.height(),0,0)
                    }
                    else {
                        spanValue = displayFeature.bounds.right
                        endChatView.setPadding(displayFeature.bounds.width(),0,0,0)
                    }
                }
            }
            changeLayout()
        }
    }
}