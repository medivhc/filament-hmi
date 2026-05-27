package com.example.filamenthmi.render

import org.junit.Assert.assertEquals
import org.junit.Test

class SceneQualityProfileTest {
    @Test
    fun defaultProfileMatchesQualityExpectation() {
        val profile = SceneQualityProfile()
        assertEquals(2048, profile.shadowMapSize)
        assertEquals(16, profile.softShadowStepCount)
        assertEquals(0.65f, profile.groundRoughness)
    }
}
