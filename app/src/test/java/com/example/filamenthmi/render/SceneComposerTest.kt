package com.example.filamenthmi.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SceneComposerTest {
    @Test
    fun composeBuildsRoadLaneAndVehicles() {
        val state = InterpolatedSceneState(
            timestampMs = 1000,
            egoPose = InterpolationPose(0f, 0f, 0f),
            smoothedSpeedKmh = 20f,
            source = InterpolatedSceneState.Source.INTERPOLATED
        )

        val scene = SceneComposer.compose(state)
        assertEquals(12, scene.road.positions.size)
        assertTrue(scene.laneGuide.positions.isNotEmpty())
        assertTrue(scene.egoVehicle.positions.isNotEmpty())
        assertEquals(3, scene.trafficVehicles.size)
    }
}
