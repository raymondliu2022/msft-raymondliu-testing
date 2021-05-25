package com.example.test3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            if (chatToggle) {
                leafMotionLayout.transitionToState(R.id.leafChat)
//                ConstraintLayout.getSharedValues().fireNewValue(R.id.fold, 500)
//                ConstraintLayout.getSharedValues().fireNewValue(R.id.split, 500)
            }
            else {
                leafMotionLayout.transitionToState(R.id.leafFull)
//                ConstraintLayout.getSharedValues().fireNewValue(R.id.fold, 0)
//                ConstraintLayout.getSharedValues().fireNewValue(R.id.split,0)

            }
        }
    }

    override fun onStop() {
        super.onStop()
        windowManager.unregisterLayoutChangeCallback(stateContainer)
    }

    inner class StateContainer : Consumer<WindowLayoutInfo> {

        override fun accept(newLayoutInfo: WindowLayoutInfo) {
            // Add views that represent display features
            for (displayFeature in newLayoutInfo.displayFeatures) {
                val foldFeature = displayFeature as? FoldingFeature
                if (foldFeature != null) {
                    if (foldFeature.isSeparating && // isSeparating not responding as expected on 7" & 8" emulators...
                        foldFeature.orientation == FoldingFeature.ORIENTATION_HORIZONTAL
                    ) {
                        //HACK:
                        if (foldFeature.state == FoldingFeature.STATE_HALF_OPENED) { // this replaces isSeparating (temporarily)
                            val fold = 0 //foldPosition(motionLayout, foldFeature)
//                            ConstraintLayout.getSharedValues().fireNewValue(R.id.fold, fold)
//                            playerView.useController = false // use other screen controls
                        } else
                        {
//                            ConstraintLayout.getSharedValues().fireNewValue(R.id.fold, 0);
//                            playerView.useController = true // use on-video controls
                        }
                        //ORIG:
                        // The foldable device is in tabletop mode
                        //val fold = foldPosition(motionLayout, foldFeature)
                        //ConstraintLayout.getSharedValues().fireNewValue(R.id.fold, fold)
                        //playerView.useController = false // use other screen controls
                    } else {
//                        ConstraintLayout.getSharedValues().fireNewValue(R.id.fold, 0);
//                        playerView.useController = true // use on-video controls
                    }
                }
            }
        }
    }
}