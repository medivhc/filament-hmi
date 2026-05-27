package com.example.filamenthmi.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VehiclePlaceholderFactoryTest {
    @Test
    fun defaultBoxMeshHasExpectedVertexAndTriangleCounts() {
        val mesh = VehiclePlaceholderFactory.boxMesh()
        assertEquals(24, mesh.positions.size) // 8 vertices * xyz
        assertEquals(36, mesh.indices.size) // 12 triangles * 3 indices
        assertTrue(mesh.indices.maxOrNull()!! <= 7)
    }

    @Test
    fun customDimensionsAffectBoundingExtent() {
        val mesh = VehiclePlaceholderFactory.boxMesh(lengthM = 6f, widthM = 2f, heightM = 2.5f)
        val xs = mesh.positions.filterIndexed { idx, _ -> idx % 3 == 0 }
        val ys = mesh.positions.filterIndexed { idx, _ -> idx % 3 == 1 }
        val zs = mesh.positions.filterIndexed { idx, _ -> idx % 3 == 2 }

        assertEquals(-1f, xs.minOrNull()!!, 0.0001f)
        assertEquals(1f, xs.maxOrNull()!!, 0.0001f)
        assertEquals(0f, ys.minOrNull()!!, 0.0001f)
        assertEquals(2.5f, ys.maxOrNull()!!, 0.0001f)
        assertEquals(-3f, zs.minOrNull()!!, 0.0001f)
        assertEquals(3f, zs.maxOrNull()!!, 0.0001f)
    }
}
