package com.example.helloworld

import android.icu.text.CaseMap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.window.WindowManager
import android.view.Menu
import android.view.MenuItem
import androidx.core.util.Consumer
import androidx.window.DisplayFeature
import androidx.window.FoldingFeature
import androidx.window.WindowLayoutInfo
import com.example.helloworld.databinding.ActivityMainBinding
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var wm: WindowManager

    private val layoutStateChangeCallback = LayoutStateChangeCallback()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        wm = WindowManager(this)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        wm.registerLayoutChangeCallback(
            runOnUiThreadExecutor(),
            layoutStateChangeCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        wm.unregisterLayoutChangeCallback(layoutStateChangeCallback)
    }

    private fun runOnUiThreadExecutor(): Executor {
        val handler = Handler(Looper.getMainLooper())
        return Executor() {
            handler.post(it)
        }
    }

    inner class LayoutStateChangeCallback : Consumer<WindowLayoutInfo> {
        override fun accept(newLayoutInfo: WindowLayoutInfo){
            binding.textView.text = "No display features detected"
            for (displayFeature : DisplayFeature in newLayoutInfo.displayFeatures) {
                if (displayFeature is FoldingFeature && displayFeature.occlusionMode == FoldingFeature.OCCLUSION_NONE){
                    binding.textView.text = "App is spanned across a fold"
                }
                if (displayFeature is FoldingFeature && displayFeature.occlusionMode == FoldingFeature.OCCLUSION_FULL){
                    binding.textView.text = "App is spanned across a hinge"
                }
            }
        }
    }
}