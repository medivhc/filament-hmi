package com.example.filamenthmi.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SceneFrameInterpolatorTest {
    @Test
    fun interpolatesBetweenTwoFrames() {
        val itp = SceneFrameInterpolator()
        itp.push(InterpolationFrame(1000, InterpolationPose(0f, 0f, 0f), 10f))
        itp.push(InterpolationFrame(1100, InterpolationPose(10f, 0f, 0f), 20f))

        val state = itp.sample(1050)!!
        assertEquals(5f, state.egoPose.x, 0.001f)
        assertEquals(InterpolatedSceneState.Source.INTERPOLATED, state.source)
    }

    @Test
    fun extrapolatesWithinBudgetThenClamps() {
        val itp = SceneFrameInterpolator(maxExtrapolationMs = 150)
        itp.push(InterpolationFrame(1000, InterpolationPose(1f, 0f, 0f, vx = 2f), 30f))
        itp.push(InterpolationFrame(1100, InterpolationPose(2f, 0f, 0f, vx = 2f), 30f))

        val extrapolated = itp.sample(1200)!!
        assertEquals(InterpolatedSceneState.Source.EXTRAPOLATED, extrapolated.source)
        assertTrue(extrapolated.egoPose.x > 2f)

        val clamped = itp.sample(1400)!!
        assertEquals(InterpolatedSceneState.Source.CLAMPED, clamped.source)
    }
}
