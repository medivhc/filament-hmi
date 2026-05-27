package com.example.filamenthmi.render

import com.example.filamenthmi.scene.DrivingSceneFrameModel
import com.example.filamenthmi.scene.DrivingSceneProtoParser

class SceneUpdateEngine {
    private val interpolator = SceneFrameInterpolator()
    var latestHudState: HudUiState = HudUiState()
        private set

    fun onProtoFrame(frameBytes: ByteArray) {
        val frame = DrivingSceneProtoParser.parse(frameBytes)
        ingest(frame)
    }

    fun ingest(frame: DrivingSceneFrameModel) {
        interpolator.push(
            InterpolationFrame(
                timestampMs = frame.timestampMs,
                egoPose = InterpolationPose(frame.egoPose.x, frame.egoPose.y, frame.egoPose.z, frame.egoPose.vx, frame.egoPose.vy, frame.egoPose.vz),
                speedKmh = frame.hud.speedKmh
            )
        )
        latestHudState = HudUiState(
            gear = frame.hud.gear,
            speedKmh = frame.hud.speedKmh,
            accEnabled = frame.hud.accEnabled,
            speedLimitKmh = frame.hud.speedLimitKmh,
            warningRingRed = true
        )
    }

    fun composeAt(renderTsMs: Long): SceneComposition? = interpolator.sample(renderTsMs)?.let(SceneComposer::compose)
}
