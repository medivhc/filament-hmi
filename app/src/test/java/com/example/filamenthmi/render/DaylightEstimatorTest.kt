package com.example.filamenthmi.render

import org.junit.Assert.assertTrue
import org.junit.Test

class DaylightEstimatorTest {
    private val estimator = DaylightEstimator()

    @Test
    fun noonHasHigherIntensityThanDawn() {
        val dawn = estimator.estimate(0.05f)
        val noon = estimator.estimate(0.5f)
        assertTrue(noon.intensityLux > dawn.intensityLux)
        assertTrue(noon.colorTemperatureKelvin > dawn.colorTemperatureKelvin)
    }

    @Test
    fun outputsNormalizedDirection() {
        val state = estimator.estimate(0.3f)
        val d = state.direction
        val len = kotlin.math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2])
        assertTrue(len > 0.999f && len < 1.001f)
    }
}
