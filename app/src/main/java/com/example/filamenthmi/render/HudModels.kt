package com.example.filamenthmi.render

data class HudUiState(
    val gear: String = "D",
    val speedKmh: Float = 19.9f,
    val accEnabled: Boolean = true,
    val speedLimitKmh: Int = 130,
    val warningRingRed: Boolean = true
)

object HudFormatter {
    fun speed(speedKmh: Float): String = String.format("%.1f", speedKmh)
    fun unit(): String = "KM/H"
}
