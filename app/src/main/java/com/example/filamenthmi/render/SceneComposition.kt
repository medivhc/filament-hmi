package com.example.filamenthmi.render

/**
 * Runtime-composable scene payload derived from 60Hz interpolated state.
 */
data class SceneComposition(
    val road: MeshData,
    val laneGuide: MeshData,
    val egoVehicle: MeshData,
    val trafficVehicles: List<MeshData>
)

object SceneComposer {
    fun compose(state: InterpolatedSceneState): SceneComposition {
        val road = RoadMeshBuilder.build()
        val laneGuide = LaneGuideMeshBuilder.buildStrip(
            centerline = listOf(
                InterpolationPose(state.egoPose.x - 0.8f, 0.02f, state.egoPose.z + 2f),
                InterpolationPose(state.egoPose.x - 0.6f, 0.02f, state.egoPose.z + 10f),
                InterpolationPose(state.egoPose.x - 0.2f, 0.02f, state.egoPose.z + 22f)
            ),
            widthM = 1.2f
        )

        val ego = VehiclePlaceholderFactory.boxMesh(lengthM = 4.7f, widthM = 1.9f, heightM = 1.75f)
        val traffic = listOf(
            VehiclePlaceholderFactory.boxMesh(lengthM = 4.5f, widthM = 1.85f, heightM = 1.7f),
            VehiclePlaceholderFactory.boxMesh(lengthM = 5.2f, widthM = 2.1f, heightM = 2.2f),
            VehiclePlaceholderFactory.boxMesh(lengthM = 4.6f, widthM = 1.9f, heightM = 1.8f)
        )
        return SceneComposition(road, laneGuide, ego, traffic)
    }
}
