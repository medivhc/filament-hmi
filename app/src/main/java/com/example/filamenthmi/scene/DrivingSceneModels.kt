package com.example.filamenthmi.scene

data class Pose3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val vz: Float = 0f
)

data class VehicleState(
    val id: Long,
    val pose: Pose3,
    val lengthM: Float = 4.5f,
    val widthM: Float = 1.8f,
    val heightM: Float = 1.6f,
    val isEgo: Boolean = false
)

data class HudState(
    val speedKmh: Float = 0f,
    val gear: String = "D",
    val accEnabled: Boolean = true,
    val speedLimitKmh: Int = 130
)

data class DrivingSceneFrameModel(
    val timestampMs: Long,
    val egoPose: Pose3,
    val vehicles: List<VehicleState>,
    val hud: HudState
)
