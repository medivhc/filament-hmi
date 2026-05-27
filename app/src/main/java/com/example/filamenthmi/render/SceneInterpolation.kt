package com.example.filamenthmi.render

import kotlin.math.max

/**
 * T3: 10Hz input frame to 60Hz render-state interpolation/extrapolation.
 */
data class InterpolationPose(
    val x: Float,
    val y: Float,
    val z: Float,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val vz: Float = 0f
)

data class InterpolationFrame(
    val timestampMs: Long,
    val egoPose: InterpolationPose,
    val speedKmh: Float
)

data class InterpolatedSceneState(
    val timestampMs: Long,
    val egoPose: InterpolationPose,
    val smoothedSpeedKmh: Float,
    val source: Source
) {
    enum class Source { INTERPOLATED, EXTRAPOLATED, CLAMPED }
}

class SceneFrameInterpolator(
    private val maxExtrapolationMs: Long = 150,
    private val speedSmoothingAlpha: Float = 0.25f
) {
    private val frames = ArrayDeque<InterpolationFrame>()
    private var smoothedSpeed: Float? = null

    fun push(frame: InterpolationFrame) {
        if (frames.isNotEmpty() && frame.timestampMs < frames.last().timestampMs) return
        frames.addLast(frame)
        while (frames.size > 20) frames.removeFirst()
    }

    fun sample(renderTimestampMs: Long): InterpolatedSceneState? {
        if (frames.isEmpty()) return null
        if (frames.size == 1) {
            val f = frames.first()
            return InterpolatedSceneState(f.timestampMs, f.egoPose, smoothSpeed(f.speedKmh), InterpolatedSceneState.Source.CLAMPED)
        }

        val pair = surroundingFrames(renderTimestampMs)
        return when {
            pair != null -> {
                val (a, b) = pair
                val t = ((renderTimestampMs - a.timestampMs).toFloat() / max(1L, b.timestampMs - a.timestampMs).toFloat()).coerceIn(0f, 1f)
                val pose = lerpPose(a.egoPose, b.egoPose, t)
                val speed = smoothSpeed(lerp(a.speedKmh, b.speedKmh, t))
                InterpolatedSceneState(renderTimestampMs, pose, speed, InterpolatedSceneState.Source.INTERPOLATED)
            }
            renderTimestampMs > frames.last().timestampMs -> {
                val last = frames.last()
                val dtMs = renderTimestampMs - last.timestampMs
                if (dtMs <= maxExtrapolationMs) {
                    val dt = dtMs / 1000f
                    val pose = InterpolationPose(
                        x = last.egoPose.x + last.egoPose.vx * dt,
                        y = last.egoPose.y + last.egoPose.vy * dt,
                        z = last.egoPose.z + last.egoPose.vz * dt,
                        vx = last.egoPose.vx,
                        vy = last.egoPose.vy,
                        vz = last.egoPose.vz
                    )
                    InterpolatedSceneState(renderTimestampMs, pose, smoothSpeed(last.speedKmh), InterpolatedSceneState.Source.EXTRAPOLATED)
                } else {
                    InterpolatedSceneState(last.timestampMs, last.egoPose, smoothSpeed(last.speedKmh), InterpolatedSceneState.Source.CLAMPED)
                }
            }
            else -> {
                val first = frames.first()
                InterpolatedSceneState(first.timestampMs, first.egoPose, smoothSpeed(first.speedKmh), InterpolatedSceneState.Source.CLAMPED)
            }
        }
    }

    private fun surroundingFrames(ts: Long): Pair<InterpolationFrame, InterpolationFrame>? {
        for (i in 0 until frames.size - 1) {
            val a = frames.elementAt(i)
            val b = frames.elementAt(i + 1)
            if (a.timestampMs <= ts && ts <= b.timestampMs) return a to b
        }
        return null
    }

    private fun smoothSpeed(current: Float): Float {
        val prev = smoothedSpeed
        val next = if (prev == null) current else speedSmoothingAlpha * current + (1 - speedSmoothingAlpha) * prev
        smoothedSpeed = next
        return next
    }

    private fun lerpPose(a: InterpolationPose, b: InterpolationPose, t: Float): InterpolationPose = InterpolationPose(
        x = lerp(a.x, b.x, t),
        y = lerp(a.y, b.y, t),
        z = lerp(a.z, b.z, t),
        vx = lerp(a.vx, b.vx, t),
        vy = lerp(a.vy, b.vy, t),
        vz = lerp(a.vz, b.vz, t)
    )

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}
