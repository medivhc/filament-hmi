package com.example.filamenthmi.render

import com.example.filamenthmi.scene.DrivingSceneFrameModel
import com.example.filamenthmi.scene.HudState
import com.example.filamenthmi.scene.Pose3
import com.example.filamenthmi.scene.VehicleState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SceneUpdateEngineTest {
    @Test
    fun ingestUpdatesHudAndComposesScene() {
        val engine = SceneUpdateEngine()
        engine.ingest(DrivingSceneFrameModel(1000, Pose3(0f,0f,0f,0f,0f,8f), listOf(VehicleState(1, Pose3())), HudState(10f,"D",true,130)))
        engine.ingest(DrivingSceneFrameModel(1100, Pose3(0f,0f,1f,0f,0f,8f), listOf(VehicleState(1, Pose3())), HudState(30f,"D",true,130)))
        assertNotNull(engine.composeAt(1050))
        assertEquals("D", engine.latestHudState.gear)
    }
}
