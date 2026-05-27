package com.example.filamenthmi

import android.os.Bundle
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.example.filamenthmi.render.FilamentRuntime
import com.example.filamenthmi.render.HudBinder
import com.example.filamenthmi.render.HudUiState

class MainActivity : AppCompatActivity() {
    private lateinit var runtime: FilamentRuntime
    private lateinit var hudBinder: HudBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val surface = findViewById<SurfaceView>(R.id.filamentSurface)
        runtime = FilamentRuntime(surface)
        hudBinder = HudBinder.fromActivity(this)
        hudBinder.bind(HudUiState())
        // In production, this should be fed at 10Hz from upstream proto stream.
        hudBinder.bind(runtime.currentHudState())
    }

    override fun onResume() {
        super.onResume()
        runtime.onResume()
    }

    override fun onPause() {
        runtime.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        runtime.onDestroy()
        super.onDestroy()
    }
}
