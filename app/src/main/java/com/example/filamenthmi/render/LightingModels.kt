package com.example.filamenthmi.render

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/** Physically-inspired white-clay material profile for ego and traffic vehicles. */
data class WhiteModelMaterialProfile(
    val baseColorLinear: FloatArray = floatArrayOf(0.92f, 0.92f, 0.92f),
    val roughness: Float = 0.42f,
    val metallic: Float = 0.0f,
    val reflectance: Float = 0.55f,
    val clearCoat: Float = 0.18f,
    val clearCoatRoughness: Float = 0.24f
)

data class SunLightingState(
    val direction: FloatArray,
    val intensityLux: Float,
    val colorTemperatureKelvin: Float,
    val shadowStrength: Float
)

class DaylightEstimator(
    private val minLux: Float = 800f,
    private val noonLux: Float = 100_000f,
    private val dawnDuskKelvin: Float = 3500f,
    private val noonKelvin: Float = 6500f
) {
    /**
     * @param dayProgress 0..1 where 0=start-of-day, 0.5=noon, 1=end-of-day
     */
    fun estimate(dayProgress: Float): SunLightingState {
        val clamped = dayProgress.coerceIn(0f, 1f)
        val elevationNorm = max(0f, sin((clamped * PI).toFloat()))
        val azimuth = (clamped * 2f * PI).toFloat()

        val x = cos(azimuth)
        val y = elevationNorm * 1.2f + 0.05f
        val z = sin(azimuth)
        val len = kotlin.math.sqrt(x * x + y * y + z * z)

        val intensity = minLux + (noonLux - minLux) * elevationNorm
        val kelvin = dawnDuskKelvin + (noonKelvin - dawnDuskKelvin) * elevationNorm
        val shadowStrength = (0.35f + 0.55f * elevationNorm).coerceAtMost(0.95f)

        return SunLightingState(
            direction = floatArrayOf(x / len, y / len, z / len),
            intensityLux = intensity,
            colorTemperatureKelvin = kelvin,
            shadowStrength = shadowStrength
        )
    }
}
