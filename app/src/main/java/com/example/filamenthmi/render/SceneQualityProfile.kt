package com.example.filamenthmi.render

/**
 * Central quality profile for white-model vehicles and environment lighting/shadows.
 */
data class SceneQualityProfile(
    val vehicleMaterial: WhiteModelMaterialProfile = WhiteModelMaterialProfile(),
    val ambientIntensity: Float = 25_000f,
    val groundRoughness: Float = 0.65f,
    val shadowMapSize: Int = 2048,
    val softShadowStepCount: Int = 16
) {
    init {
        require(ambientIntensity > 0f)
        require(shadowMapSize in setOf(1024, 2048, 4096))
        require(softShadowStepCount in 8..32)
    }
}
