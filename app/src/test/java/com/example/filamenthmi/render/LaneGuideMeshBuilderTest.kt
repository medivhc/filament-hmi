package com.example.filamenthmi.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LaneGuideMeshBuilderTest {
    @Test
    fun stripBuildsExpectedTopology() {
        val line = listOf(
            InterpolationPose(0f, 0f, 0f),
            InterpolationPose(0f, 0f, 10f),
            InterpolationPose(2f, 0f, 20f)
        )
        val mesh = LaneGuideMeshBuilder.buildStrip(line, 1.2f)
        assertEquals(18, mesh.positions.size) // 6 vertices * xyz
        assertEquals(12, mesh.indices.size) // 2 segments * 2 triangles * 3
    }

    @Test
    fun roadMeshIsTwoTriangles() {
        val road = RoadMeshBuilder.build(120f, 8f)
        assertEquals(12, road.positions.size)
        assertEquals(6, road.indices.size)
        assertTrue(road.indices.maxOrNull()!! <= 3)
    }
}
