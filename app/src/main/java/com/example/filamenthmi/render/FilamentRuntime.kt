package com.example.filamenthmi.render

import android.view.Choreographer
import android.view.SurfaceView

class FilamentRuntime(
    private val surfaceView: SurfaceView,
    private val frameScheduler: FrameScheduler = ChoreographerScheduler()
) {
    private val rendererCore = RendererCore(surfaceView)
    private val stateMachine = RuntimeStateMachine()
    private var running = false

    private val callback = object : FrameScheduler.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return
            rendererCore.render(frameTimeNanos)
            frameScheduler.postFrameCallback(this)
        }
    }

    fun onResume() {
        if (running) return
        running = true
        stateMachine.onResume()
        rendererCore.onSurfaceAvailable()
        frameScheduler.postFrameCallback(callback)
    }

    fun onPause() {
        running = false
        stateMachine.onPause()
        frameScheduler.removeFrameCallback(callback)
    }

    fun onProtoFrame(frameBytes: ByteArray) {
        rendererCore.onProtoFrame(frameBytes)
    }

    fun currentHudState(): HudUiState = rendererCore.currentHudState()

    fun onDestroy() {
        onPause()
        stateMachine.onDestroy()
        rendererCore.destroy()
    }
}

interface FrameScheduler {
    interface FrameCallback {
        fun doFrame(frameTimeNanos: Long)
    }

    fun postFrameCallback(callback: FrameCallback)
    fun removeFrameCallback(callback: FrameCallback)
}

class ChoreographerScheduler(
    private val choreographer: Choreographer = Choreographer.getInstance()
) : FrameScheduler {
    private val wrappers = mutableMapOf<FrameScheduler.FrameCallback, Choreographer.FrameCallback>()

    override fun postFrameCallback(callback: FrameScheduler.FrameCallback) {
        val wrapper = wrappers.getOrPut(callback) {
            Choreographer.FrameCallback { callback.doFrame(it) }
        }
        choreographer.postFrameCallback(wrapper)
    }

    override fun removeFrameCallback(callback: FrameScheduler.FrameCallback) {
        wrappers.remove(callback)?.let(choreographer::removeFrameCallback)
    }
}
