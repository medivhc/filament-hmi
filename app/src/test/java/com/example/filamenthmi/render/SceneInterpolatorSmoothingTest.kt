package com.example.filamenthmi.render

import org.junit.Assert.assertTrue
import org.junit.Test

class SceneInterpolatorSmoothingTest {
    @Test
    fun speedSmoothingReducesSuddenJump() {
        val itp = SceneFrameInterpolator(speedSmoothingAlpha = 0.25f)
        itp.push(InterpolationFrame(1000, InterpolationPose(0f, 0f, 0f), 0f))
        itp.push(InterpolationFrame(1100, InterpolationPose(0f, 0f, 1f), 100f))

        val s1 = itp.sample(1000)!!.smoothedSpeedKmh
        val s2 = itp.sample(1100)!!.smoothedSpeedKmh

        assertTrue(s1 < s2)
        assertTrue(s2 < 100f)
    }
}
